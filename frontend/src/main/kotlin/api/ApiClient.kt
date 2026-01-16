import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import kotlin.js.json
import kotlinx.browser.window

object ApiClient {
    private val baseUrl = window.location.origin.replace("3000", "8080") + "/api"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    suspend fun get(endpoint: String): String {
        val response = window.fetch("$baseUrl$endpoint").await()
        return response.text().await()
    }
    
    suspend fun post(endpoint: String, body: Any): String {
        val response = window.fetch(
            "$baseUrl$endpoint",
            RequestInit(
                method = "POST",
                headers = json(
                    "Content-Type" to "application/json"
                ),
                body = JSON.stringify(body)
            )
        ).await()
        return response.text().await()
    }
}
