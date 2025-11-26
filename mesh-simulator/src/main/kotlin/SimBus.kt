import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

object SimBus {
    private val nodes = mutableListOf<SimNode>()

    fun register(node: SimNode) {
        nodes += node
    }

    suspend fun broadcast(from: Int, msg: String) {
        delay((5..30).random().toLong())

        for (n in nodes) {
            if (n.nodeId != from) {
                n.inbox.trySend(msg)
            }
        }
    }
}
