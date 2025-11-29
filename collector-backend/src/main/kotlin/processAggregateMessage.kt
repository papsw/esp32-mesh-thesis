package collector

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import java.time.Instant

fun processAggregateMessage(
    jsonLine: String,
    repo: ReadingRepository,
    flow: MutableSharedFlow<String>,
    json: Json = Json { ignoreUnknownKeys = true }
) {
    val msg = try {
        json.decodeFromString<AggregateMessage>(jsonLine)
    } catch (_: Exception) {
        return  // malformed json
    }

    // Must be aggregate
    if (msg.msg_type != "aggregate") return

    // leader_id and round must be > 0
    if (msg.leader_id <= 0 || msg.round <= 0) return

    // readings must not be empty
    if (msg.readings.isEmpty()) return

    val ts = Instant.now().toString()

    for ((k, reading) in msg.readings) {
        val nodeId = k.toIntOrNull() ?: continue

        try {
            repo.insert(
                leaderId = msg.leader_id,
                round = msg.round,
                nodeId = nodeId,
                r = reading,
                timestamp = ts
            )
        } catch (_: Exception) {
            continue
        }
    }

    // Replace broadcaster.send(...) with SharedFlow emit
    flow.tryEmit(jsonLine)
}