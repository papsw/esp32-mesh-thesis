import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.*

class SimNode(
    val nodeId: Int,
    val sensorType: String
) {
    val inbox = Channel<String>(Channel.BUFFERED)
    val state = ElectionState()

    suspend fun start() = coroutineScope {
        SimBus.register(this@SimNode)

        launch { receiverLoop() }
        launch { heartbeatLoop() }
        launch { sensorLoop() }

        startElection()
    }

    private suspend fun receiverLoop() {
        for (raw in inbox) {
            if (raw == null || raw.isBlank()) continue

            val json = try {
                Json.parseToJsonElement(raw).jsonObject
            } catch (e: Exception) {
                println("Node $nodeId: invalid JSON: $raw")
                continue
            }

            val type = json["msg_type"]?.jsonPrimitive?.content ?: continue

            when (type) {
                "heartbeat" -> {
                    val leaderId = json["leader_id"]?.jsonPrimitive?.int ?: continue
                    state.leader = leaderId
                    state.lastHeartbeat = System.currentTimeMillis()
                    state.role = NodeRole.FOLLOWER
                }

                "election" -> {
                    val from = json["from"]?.jsonPrimitive?.int ?: continue
                    if (nodeId > from) {
                        SimBus.broadcast(nodeId,
                            buildJsonObject {
                                put("msg_type", "election")
                                put("subtype", "im_alive")
                                put("from", nodeId)
                            }.toString()
                        )
                        startElection()
                    }
                }

                "reading" -> {
                    if (state.role == NodeRole.LEADER)
                        aggregateReading(json)
                }
            }
        }
    }


    private suspend fun heartbeatLoop() {
        while (true) {
            delay(1000)
            if (state.role == NodeRole.LEADER) {
                SimBus.broadcast(nodeId,
                    buildJsonObject {
                        put("msg_type", "heartbeat")
                        put("leader_id", nodeId)
                    }.toString()
                )
            } else {
                val diff = System.currentTimeMillis() - state.lastHeartbeat
                if (diff > 3000) startElection()
            }
        }
    }

    private suspend fun startElection() {
        state.role = NodeRole.IN_ELECTION
        SimBus.broadcast(nodeId,
            buildJsonObject {
                put("msg_type", "election")
                put("subtype", "alive?")
                put("from", nodeId)
            }.toString()
        )
        delay(600)
        if (state.role == NodeRole.IN_ELECTION) becomeLeader()
    }

    private suspend fun becomeLeader() {
        state.role = NodeRole.LEADER
        state.leader = nodeId
        SimBus.broadcast(nodeId,
            buildJsonObject {
                put("msg_type", "election")
                put("subtype", "iam_leader")
                put("leader_id", nodeId)
            }.toString()
        )
    }

    private suspend fun sensorLoop() {
        while (true) {
            delay(2000)
            val payload = when (sensorType) {
                "zone" -> SimSensors.zone()
                "light" -> SimSensors.light()
                "gas" -> SimSensors.gas()
                else -> buildJsonObject {}
            }

            val msg = buildJsonObject {
                put("msg_type", "reading")
                put("node_id", nodeId)
                put("round", (0..9999).random())
                put("payload", payload)
            }
            SimBus.broadcast(nodeId, msg.toString())
        }
    }

    private suspend fun aggregateReading(json: JsonObject) {
        val nodeId = json["node_id"]!!.jsonPrimitive.int
        val payload = json["payload"]!!.jsonObject
        val round = json["round"]!!.jsonPrimitive.int

        val agg = buildJsonObject {
            put("msg_type", "aggregate")
            put("leader_id", this@SimNode.nodeId)
            put("round", round)
            put("readings", buildJsonObject {
                put(nodeId.toString(), payload)
            })
        }

        CollectorPipe.send(agg.toString())
    }
}