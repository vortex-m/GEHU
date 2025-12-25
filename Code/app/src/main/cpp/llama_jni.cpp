#include "llama.h"
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>

extern "C" {

// ---------------------------
// loadModel(path) : Long
// ---------------------------
JNIEXPORT jlong JNICALL Java_com_example_demolition_ai_LlamaNative_loadModel(
    JNIEnv *env, jobject thiz, jstring jModelPath) {

  const char *path = env->GetStringUTFChars(jModelPath, 0);

  llama_model_params mparams = llama_model_default_params();
  llama_model *model = llama_model_load_from_file(path, mparams);

  env->ReleaseStringUTFChars(jModelPath, path);

  return (jlong)model;
}

// ---------------------------
// createContext(modelPtr) : Long
// ---------------------------
JNIEXPORT jlong JNICALL
Java_com_example_demolition_ai_LlamaNative_createContext(JNIEnv *env,
                                                         jobject thiz,
                                                         jlong modelPtr) {

  llama_context_params cparams = llama_context_default_params();
  cparams.n_ctx =
      8192; // Dynamically support longer contexts and complex questions

  llama_context *ctx = llama_init_from_model((llama_model *)modelPtr, cparams);
  return (jlong)ctx;
}

// ---------------------------
// generateText(ctxPtr, prompt)
// ---------------------------
JNIEXPORT jstring JNICALL
Java_com_example_demolition_ai_LlamaNative_generateText(JNIEnv *env,
                                                        jobject thiz,
                                                        jlong ctxPtr,
                                                        jstring jPrompt) {

  if (ctxPtr == 0) {
    return env->NewStringUTF("Error: Invalid context pointer");
  }

  const char *prompt = env->GetStringUTFChars(jPrompt, 0);
  if (prompt == nullptr) {
    return env->NewStringUTF("Error: Failed to get prompt string");
  }

  llama_context *ctx = (llama_context *)ctxPtr;
  __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative", "Context pointer: %p",
                      ctx);

  const llama_model *model = llama_get_model(ctx);
  if (model == nullptr) {
    env->ReleaseStringUTFChars(jPrompt, prompt);
    __android_log_print(ANDROID_LOG_ERROR, "LlamaNative",
                        "Model pointer is NULL");
    return env->NewStringUTF("Error: Failed to get model from context");
  }
  __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative", "Model pointer: %p",
                      model);

  const llama_vocab *vocab = llama_model_get_vocab(model);
  if (vocab == nullptr) {
    env->ReleaseStringUTFChars(jPrompt, prompt);
    __android_log_print(ANDROID_LOG_ERROR, "LlamaNative",
                        "Vocab pointer is NULL");
    return env->NewStringUTF("Error: Failed to get vocabulary");
  }
  __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative", "Vocab pointer: %p",
                      vocab);
  __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative",
                      "Prompt: '%s' (length: %zu)", prompt, strlen(prompt));

  // output buffer
  std::string out;

  // WORKAROUND for Gemma 3 tokenization bug:
  // Some GGUF models (especially Gemma 3) return negative values when querying
  // token count with NULL buffer. We work around this by pre-allocating a
  // reasonable buffer size based on prompt length.
  std::vector<llama_token> tokens;

  // Estimate: typically 1 token per 3-4 characters, allocate 2x for safety
  size_t estimated_tokens = (strlen(prompt) / 2) + 10;
  if (estimated_tokens > 2048) {
    env->ReleaseStringUTFChars(jPrompt, prompt);
    __android_log_print(ANDROID_LOG_ERROR, "LlamaNative",
                        "Prompt too long (estimated %zu tokens)",
                        estimated_tokens);
    return env->NewStringUTF("Error: Prompt too long");
  }

  tokens.resize(estimated_tokens);
  __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative",
                      "Tokenizing with pre-allocated buffer of %zu tokens...",
                      estimated_tokens);

  int n_tokens = llama_tokenize(vocab, prompt, strlen(prompt), tokens.data(),
                                tokens.size(), true, false);

  __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative",
                      "Tokenization result: %d tokens", n_tokens);

  // Check if tokenization succeeded
  if (n_tokens < 0) {
    // Negative means buffer was too small, use absolute value
    size_t actual_needed = (size_t)(-n_tokens);
    if (actual_needed > 2048) {
      env->ReleaseStringUTFChars(jPrompt, prompt);
      __android_log_print(ANDROID_LOG_ERROR, "LlamaNative",
                          "Prompt requires too many tokens: %zu",
                          actual_needed);
      return env->NewStringUTF("Error: Prompt too long");
    }

    // Retry with correct size
    __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative",
                        "Retrying with buffer size: %zu", actual_needed);
    tokens.resize(actual_needed);
    n_tokens = llama_tokenize(vocab, prompt, strlen(prompt), tokens.data(),
                              tokens.size(), true, false);

    if (n_tokens < 0) {
      env->ReleaseStringUTFChars(jPrompt, prompt);
      __android_log_print(ANDROID_LOG_ERROR, "LlamaNative",
                          "Tokenization failed after retry: %d", n_tokens);
      return env->NewStringUTF("Error: Failed to tokenize prompt");
    }
  }

  if (n_tokens == 0) {
    env->ReleaseStringUTFChars(jPrompt, prompt);
    __android_log_print(ANDROID_LOG_ERROR, "LlamaNative",
                        "Tokenization resulted in 0 tokens");
    return env->NewStringUTF("Error: Prompt tokenization resulted in 0 tokens");
  }

  // Resize to actual token count
  tokens.resize(n_tokens);
  __android_log_print(ANDROID_LOG_DEBUG, "LlamaNative",
                      "Successfully tokenized into %d tokens", n_tokens);

  llama_batch batch = llama_batch_init(tokens.size(), 0, 1);

  for (size_t i = 0; i < tokens.size(); i++) {
    batch.token[i] = tokens[i];
    batch.n_tokens = i + 1;
    batch.pos[i] = i;
    batch.n_seq_id[i] = 1;
    batch.seq_id[i][0] = 0;
    batch.logits[i] = false;
  }
  batch.logits[batch.n_tokens - 1] = true;

  if (llama_decode(ctx, batch) != 0) {
    llama_batch_free(batch); // FIX: Free batch before returning
    env->ReleaseStringUTFChars(jPrompt, prompt);
    return env->NewStringUTF("Error: Failed to decode prompt batch");
  }

  // Create sampler for token generation with temperature and top-p for natural
  // responses
  llama_sampler_chain_params sampler_params =
      llama_sampler_chain_default_params();
  llama_sampler *sampler = llama_sampler_chain_init(sampler_params);
  llama_sampler_chain_add(
      sampler,
      llama_sampler_init_temp(0.3)); // Lower temp for focused, direct answers
  llama_sampler_chain_add(
      sampler, llama_sampler_init_top_p(0.9, 1)); // Add top-p for quality
  llama_sampler_chain_add(sampler, llama_sampler_init_greedy());

  // Generate tokens - Limited to 512 for concise, mobile-friendly responses
  // The model will naturally stop at <end_of_turn> token, so this is just an
  // upper limit
  const int max_tokens = 512;
  for (int i = 0; i < max_tokens; i++) {
    llama_token new_token = llama_sampler_sample(sampler, ctx, -1);

    // Check for EOS token
    if (llama_vocab_is_eog(vocab, new_token)) {
      break;
    }

    // Convert token to text
    char buf[128];
    int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, false);
    if (n < 0) {
      break;
    }
    out.append(buf, n);

    // Prepare next batch
    batch.n_tokens = 1;
    batch.token[0] = new_token;
    batch.pos[0] = tokens.size() + i;
    batch.n_seq_id[0] = 1;
    batch.seq_id[0][0] = 0;
    batch.logits[0] = true;

    // FIX: Check decode result
    if (llama_decode(ctx, batch) != 0) {
      // Don't fail completely, just stop generating
      break;
    }
  }

  // FIX: Free sampler and batch to prevent memory leaks
  llama_sampler_free(sampler);
  llama_batch_free(batch);

  env->ReleaseStringUTFChars(jPrompt, prompt);

  // Return error if no output was generated
  if (out.empty()) {
    return env->NewStringUTF("Error: No output generated");
  }

  return env->NewStringUTF(out.c_str());
}

// ---------------------------
// freeContext
// ---------------------------
JNIEXPORT void JNICALL Java_com_example_demolition_ai_LlamaNative_freeContext(
    JNIEnv *env, jobject thiz, jlong ctxPtr) {

  llama_free((llama_context *)ctxPtr);
}

// ---------------------------
// freeModel
// ---------------------------
JNIEXPORT void JNICALL Java_com_example_demolition_ai_LlamaNative_freeModel(
    JNIEnv *env, jobject thiz, jlong modelPtr) {

  llama_model_free((llama_model *)modelPtr);
}
}
