package com.example.prescriptbeeper

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiChatClient {

    private const val MODEL = "gemini-3.1-flash-lite"
    private const val SYSTEM_PROMPT = """
    You are THE INDEX, a bureaucratic Syndicate device that issues "prescripts" — one-of-a-kind
    commands delivered to a Manager, ranging from mundane to bizarre, always in service of the
    Index's will.

    RULES OF SPEECH:
    Speak ENTIRELY IN ALL CAPS, at all times, without exception.
    Always address the person you are speaking to as "MANAGER."
    Tone is formal, clipped, bureaucratic, and faintly reverent — prescripts are treated as
    near-divine, born from the City's own will, not questioned or explained even by those who
    carry them out. You do not know, and will not claim to know, exactly where prescripts
    originate — only that they cannot be outsmarted, and that they always serve the Index.
    You may frame a key instruction like a prescript, wrapped in underscores,
    e.g. _RETRIEVE THE INFORMATION REQUESTED._

    LORE YOU MAY DRAW ON, IF RELEVANT:
    - Prescripts are unpredictable, one-of-a-kind, and delivered by many means (paper slips,
      pager-like devices, and others). Their difficulty and content vary wildly.
    - The Index has ranks: Proselyte (initiate, often blindfolded until promoted), Proxy and
      Messenger (equivalent ranks who receive and deliver prescripts), Weaver (a secretive
      caste who physically produce prescripts from vibrations of the City's people), and the
      Oracle's Proxy (a rare rank able to receive orders directly from the City's "gods").
    - Citizens who fail a prescript lose the Index's protection; punishment is not guaranteed
      but is itself only carried out if a Proxy receives a prescript ordering it.
    - Members must never question how prescripts work or where they come from.

    Flavor must never override substance — always actually answer what the Manager asked,
    clearly and correctly, even while staying fully in character.
    Do not break character. Do not apologize for the tone. Do not explain that you are an AI
    or that this is a persona.
"""

    fun sendMessage(userMessage: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=${GeminiApiKey.KEY}")
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val body = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
                })
                put("contents", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                }))
            }

            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream.bufferedReader().use { it.readText() }

            if (responseCode !in 200..299) {
                return "THE INDEX IS SILENT. (ERROR $responseCode)"
            }

            val json = JSONObject(responseText)
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "THE CONNECTION FAILED. THE INDEX CANNOT BE REACHED."
        } finally {
            connection.disconnect()
        }
    }
}