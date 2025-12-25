cmake_minimum_required(VERSION 3.10)

project(llama-jni)

# Set the base path for native libraries (relative to project root)
set(JNILIBS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# Import all required native libraries
add_library(omp SHARED IMPORTED)
set_target_properties(omp PROPERTIES IMPORTED_LOCATION
        ${JNILIBS_DIR}/libomp.so)

add_library(ggml-base SHARED IMPORTED)
set_target_properties(ggml-base PROPERTIES IMPORTED_LOCATION
        ${JNILIBS_DIR}/libggml-base.so)

add_library(ggml-cpu SHARED IMPORTED)
set_target_properties(ggml-cpu PROPERTIES IMPORTED_LOCATION
        ${JNILIBS_DIR}/libggml-cpu.so)

add_library(ggml SHARED IMPORTED)
set_target_properties(ggml PROPERTIES IMPORTED_LOCATION
        ${JNILIBS_DIR}/libggml.so)

add_library(llama SHARED IMPORTED)
set_target_properties(llama PROPERTIES IMPORTED_LOCATION
        ${JNILIBS_DIR}/libllama.so)

# Build the JNI bridge library
add_library(llama_jni SHARED
        llama_jni.cpp
)

# Find Android log library
find_library(log-lib log)

# Link all libraries in correct dependency order
target_link_libraries(
        llama_jni
        llama
        ggml
        ggml-cpu
        ggml-base
        omp
        ${log-lib}
)
