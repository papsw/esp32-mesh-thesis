package meshsim

import kotlinx.coroutines.channels.Channel

object CollectorPipe {
    val out = Channel<String>(Channel.BUFFERED)

    suspend fun send(s: String) = out.send(s)
}