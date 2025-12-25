package com.example.demolition.rag

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.util.UUID

/**
 * Chunks educational data from ai_data folder into searchable DocumentChunk objects.
 * Supports multiple JSON formats across different subjects.
 */
object DataChunker {

    private const val TAG = "DataChunker"
    private const val AI_DATA_FOLDER = "ai_data"

    /**
     * Loads and chunks all educational data from ai_data folder.
     * 
     * @param context Android context for asset access
     * @return List of DocumentChunk objects
     */
    fun chunkData(context: Context): List<DocumentChunk> {
        val chunks = mutableListOf<DocumentChunk>()
        
        try {
            val gson = Gson()
            
            // Load Science data
            chunks.addAll(loadScienceData(context, gson))
            
            // Load Maths data
            chunks.addAll(loadMathsData(context, gson))
            
            // Load English (Beehive) data
            chunks.addAll(loadBeehiveData(context, gson))
            
            // Load Social Science data
            chunks.addAll(loadSocialScienceData(context, gson))
            
            // Load Moments data
            chunks.addAll(loadMomentsData(context, gson))
            
            Log.d(TAG, "Created ${chunks.size} chunks from ai_data folder")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to chunk data", e)
            throw e
        }
        
        return chunks
    }

    /**
     * Load Science data from full-science.json
     */
    private fun loadScienceData(context: Context, gson: Gson): List<DocumentChunk> {
        val chunks = mutableListOf<DocumentChunk>()
        
        try {
            val json = context.assets.open("$AI_DATA_FOLDER/Science/full-science.json")
                .bufferedReader().use { it.readText() }
            
            val entries = gson.fromJson(json, JsonArray::class.java)
            
            for (entry in entries) {
                val obj = entry.asJsonObject
                val chapter = obj.get("chapter")?.asString ?: "Unknown"
                val content = obj.get("content")?.asString ?: continue
                val contentType = obj.get("content_type")?.asString ?: "concept"
                
                chunks.add(
                    DocumentChunk(
                        id = generateId(),
                        text = content,
                        subject = "Science",
                        chapterTitle = chapter,
                        chapterNumber = extractChapterNumber(chapter).toIntOrNull(),
                        contentType = contentType.lowercase()
                    )
                )
            }
            
            Log.d(TAG, "Loaded ${chunks.size} Science chunks")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Science data", e)
        }
        
        return chunks
    }

    /**
     * Load Maths data from multiple chapter files
     */
    private fun loadMathsData(context: Context, gson: Gson): List<DocumentChunk> {
        val chunks = mutableListOf<DocumentChunk>()
        val mathsFiles = listOf(
            "chapter 1-2.json", "chapter 3-4.json", "chapter 5.json", "chapter 6.json",
            "7.json", "8.json", "9.json", "10.json", "11.json", "12.json"
        )
        
        for (filename in mathsFiles) {
            try {
                val json = context.assets.open("$AI_DATA_FOLDER/maths/$filename")
                    .bufferedReader().use { it.readText() }
                
                val rootObj = gson.fromJson(json, JsonObject::class.java)
                val filesArray = rootObj.getAsJsonArray("files")
                
                for (fileElement in filesArray) {
                    val fileObj = fileElement.asJsonObject
                    val chaptersArray = fileObj.getAsJsonArray("chapters") ?: continue
                    
                    for (chapterElement in chaptersArray) {
                        val chapter = chapterElement.asJsonObject
                        val chapterTitle = chapter.get("chapter")?.asString ?: "Unknown"
                        val summary = chapter.get("summary")?.asString
                        
                        // Add summary chunk
                        if (summary != null && summary.isNotBlank()) {
                            chunks.add(
                                DocumentChunk(
                                    id = generateId(),
                                    text = summary,
                                    subject = "Mathematics",
                                    chapterTitle = chapterTitle,
                                    chapterNumber = extractChapterNumber(chapterTitle).toIntOrNull(),
                                    contentType = "summary"
                                )
                            )
                        }
                        
                        // Add key points
                        val keyPoints = chapter.getAsJsonArray("key_points")
                        if (keyPoints != null) {
                            for (point in keyPoints) {
                                chunks.add(
                                    DocumentChunk(
                                        id = generateId(),
                                        text = point.asString,
                                        subject = "Mathematics",
                                        chapterTitle = chapterTitle,
                                        chapterNumber = extractChapterNumber(chapterTitle).toIntOrNull(),
                                        contentType = "important_points"
                                    )
                                )
                            }
                        }
                        
                        // Add key definitions
                        val definitions = chapter.getAsJsonObject("key_definitions")
                        if (definitions != null) {
                            for ((term, def) in definitions.entrySet()) {
                                chunks.add(
                                    DocumentChunk(
                                        id = generateId(),
                                        text = "$term: ${def.asString}",
                                        subject = "Mathematics",
                                        chapterTitle = chapterTitle,
                                        chapterNumber = extractChapterNumber(chapterTitle).toIntOrNull(),
                                        contentType = "definition"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load maths file: $filename", e)
            }
        }
        
        Log.d(TAG, "Loaded ${chunks.size} Maths chunks")
        return chunks
    }

    /**
     * Load English (Beehive) data
     */
    private fun loadBeehiveData(context: Context, gson: Gson): List<DocumentChunk> {
        val chunks = mutableListOf<DocumentChunk>()
        val beehiveFiles = listOf("1-5.json", "6-10.json", "poems.json")
        
        for (filename in beehiveFiles) {
            try {
                val json = context.assets.open("$AI_DATA_FOLDER/beehive/$filename")
                    .bufferedReader().use { it.readText() }
                
                val chapters = gson.fromJson(json, JsonArray::class.java)
                
                for (chapterElement in chapters) {
                    val chapter = chapterElement.asJsonObject
                    val chapterTitle = chapter.get("chapter_title")?.asString ?: "Unknown"
                    val chapterNum = chapter.get("chapter_number")?.asInt?.toString() ?: "0"
                    val summary = chapter.get("summary")?.asString
                    
                    // Add summary
                    if (summary != null && summary.isNotBlank()) {
                        chunks.add(
                            DocumentChunk(
                                id = generateId(),
                                text = summary,
                                subject = "English",
                                chapterTitle = chapterTitle,
                                chapterNumber = chapterNum.toIntOrNull(),
                                contentType = "summary"
                            )
                        )
                    }
                    
                    // Add student explanation
                    val explanations = chapter.getAsJsonArray("student_explanation")
                    if (explanations != null) {
                        val combinedExp = explanations.joinToString(" ") { it.asString }
                        chunks.add(
                            DocumentChunk(
                                id = generateId(),
                                text = combinedExp,
                                subject = "English",
                                chapterTitle = chapterTitle,
                                chapterNumber = chapterNum.toIntOrNull(),
                                contentType = "explanation"
                            )
                        )
                    }
                    
                    // Add important points
                    val importantPoints = chapter.getAsJsonArray("important_points")
                    if (importantPoints != null) {
                        val combined = importantPoints.joinToString(" ") { it.asString }
                        chunks.add(
                            DocumentChunk(
                                id = generateId(),
                                text = combined,
                                subject = "English",
                                chapterTitle = chapterTitle,
                                chapterNumber = chapterNum.toIntOrNull(),
                                contentType = "important_points"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load beehive file: $filename", e)
            }
        }
        
        Log.d(TAG, "Loaded ${chunks.size} English chunks")
        return chunks
    }

    /**
     * Load Social Science data
     */
    private fun loadSocialScienceData(context: Context, gson: Gson): List<DocumentChunk> {
        val chunks = mutableListOf<DocumentChunk>()
        val sstFiles = listOf(
            "NCERT_History_Complete_Clean.json",
            "NCERT_Geography_Complete_Clean.json",
            "NCERT_Economics_Complete_Clean.json",
            "NCERT_PoliticalScience_Complete_Clean.json"
        )
        
        for (filename in sstFiles) {
            try {
                val subSubject = filename.replace("NCERT_", "").replace("_Complete_Clean.json", "")
                val json = context.assets.open("$AI_DATA_FOLDER/Social Science/$filename")
                    .bufferedReader().use { it.readText() }
                
                val entries = gson.fromJson(json, JsonArray::class.java)
                
                for (entry in entries) {
                    val obj = entry.asJsonObject
                    val chapter = obj.get("chapter")?.asString ?: "Unknown"
                    val content = obj.get("content")?.asString ?: continue
                    val contentType = obj.get("content_type")?.asString ?: "concept"
                    
                    chunks.add(
                        DocumentChunk(
                            id = generateId(),
                            text = content,
                            subject = "Social Science - $subSubject",
                            chapterTitle = chapter,
                            chapterNumber = extractChapterNumber(chapter).toIntOrNull(),
                            contentType = contentType.lowercase()
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load SST file: $filename", e)
            }
        }
        
        Log.d(TAG, "Loaded ${chunks.size} Social Science chunks")
        return chunks
    }

    /**
     * Load Moments (English supplementary) data
     */
    private fun loadMomentsData(context: Context, gson: Gson): List<DocumentChunk> {
        val chunks = mutableListOf<DocumentChunk>()
        val momentsFiles = listOf("1-5.json", "6-9.json")
        
        for (filename in momentsFiles) {
            try {
                val json = context.assets.open("$AI_DATA_FOLDER/moments/$filename")
                    .bufferedReader().use { it.readText() }
                
                val chapters = gson.fromJson(json, JsonArray::class.java)
                
                for (chapterElement in chapters) {
                    val chapter = chapterElement.asJsonObject
                    val chapterTitle = chapter.get("chapter_title")?.asString ?: "Unknown"
                    val chapterNum = chapter.get("chapter_number")?.asInt?.toString() ?: "0"
                    val summary = chapter.get("summary")?.asString
                    
                    if (summary != null && summary.isNotBlank()) {
                        chunks.add(
                            DocumentChunk(
                                id = generateId(),
                                text = summary,
                                subject = "English - Moments",
                                chapterTitle = chapterTitle,
                                chapterNumber = chapterNum.toIntOrNull(),
                                contentType = "summary"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load moments file: $filename", e)
            }
        }
        
        Log.d(TAG, "Loaded ${chunks.size} Moments chunks")
        return chunks
    }

    /**
     * Extract chapter number from chapter string like "Chapter 1:" or "1.2 Title"
     */
    private fun extractChapterNumber(chapter: String): String {
        val regex = """(?:Chapter\s+)?(\d+)""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(chapter)?.groupValues?.get(1) ?: "0"
    }

    /**
     * Generates a unique ID for a chunk
     */
    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}
