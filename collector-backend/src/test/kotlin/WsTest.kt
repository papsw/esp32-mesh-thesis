package collector

import io.ktor.server.testing.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.test.*

class WsTest {

    @Test
    fun `websocket receives broadcasted messages`() = testApplication {
        val flow = MutableSharedFlow<String>(extraBufferCapacity = 100)

        application { wsModule(flow) }

        val client = createClient { install(WebSockets) }
        val session = client.webSocketSession("/ws")

        flow.tryEmit("""{"hello":123}""")

        val frame = session.incoming.receive() as Frame.Text
        assertEquals("""{"hello":123}""", frame.readText())
    }
}