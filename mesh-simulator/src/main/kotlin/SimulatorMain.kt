package meshsim
import kotlinx.coroutines.*
import java.io.PrintWriter
import java.net.Socket

fun retryUntilConnected(host: String, port: Int): Socket {
    while (true) {
        try {
            println("Simulator: trying to connect to backend on $host:$port...")
            return Socket(host, port)
        } catch (e: Exception) {
            Thread.sleep(500)
        }
    }
}

fun main() = runBlocking {

    val gw = launch {
        val socket = retryUntilConnected("localhost", 9091)
        println("Simulator: connected to backend.")
        val out = PrintWriter(socket.getOutputStream(), true)

        for (msg in CollectorPipe.out) out.println(msg)
    }

    val nodes = listOf(
        SimNode(1, "zone"),
        SimNode(2, "light"),
        SimNode(3, "gas"),
        SimNode(4, "zone")
    )

    nodes.map { async { it.start() } }.awaitAll()
    gw.join()
}