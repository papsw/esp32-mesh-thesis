import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class WSReading(
    val role: String? = null,
    val temp_c: Double? = null,
    val humidity_pct: Double? = null,
    val pressure_hpa: Double? = null,
    val light_lux: Double? = null,
    val air_quality_raw: Double? = null
)

@Serializable
data class WSAggregate(
    val msg_type: String,
    val leader_id: Int,
    val round: Int,
    val readings: Map<String, WSReading>
)

suspend fun ws(onMsg: (WSAggregate) -> Unit) {
    val json = Json { ignoreUnknownKeys = true }
    val client = HttpClient(CIO) { install(WebSockets) }

    client.webSocket("ws://localhost:9090/ws") {
        for (f in incoming) {
            val t = (f as? Frame.Text)?.readText() ?: continue
            try {
                val msg = json.decodeFromString<WSAggregate>(t)
                if (msg.msg_type == "aggregate") onMsg(msg)
            } catch (_: Exception) {}
        }
    }
}

fun avg(x: List<Double?>, w: Int): List<Double?> {
    if (w < 2) return x
    val out = MutableList<Double?>(x.size) { null }
    for (i in x.indices) {
        val chunk = x.subList((i - w + 1).coerceAtLeast(0), i + 1)
            .filterNotNull()
        if (chunk.isNotEmpty()) out[i] = chunk.sum() / chunk.size
    }
    return out
}