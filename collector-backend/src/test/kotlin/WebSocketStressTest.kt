package collector

import io.ktor.server.testing.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.test.*

class WebSocketStressTest {

    @Test
    fun `ten clients receive message simultaneously`() = testApplication {
        val flow = MutableSharedFlow<String>(extraBufferCapacity = 100)

        application { wsModule(flow) }

        val clients = (1..10).map {
            createClient { install(WebSockets) }
                .webSocketSession("/ws")
        }

        flow.tryEmit("""{"msg":"hello"}""")

        runBlocking {
            clients.map { session ->
                async {
                    val frame = session.incoming.receive() as Frame.Text
                    assertEquals("""{"msg":"hello"}""", frame.readText())
                }
            }.awaitAll()
        }
    }
}