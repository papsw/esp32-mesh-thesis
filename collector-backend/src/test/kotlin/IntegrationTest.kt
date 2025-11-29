package collector

import io.ktor.server.testing.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.test.*
import java.sql.DriverManager

class IntegrationTest {

    @Test
    fun `end to end - message arrives, saved to DB, pushed to WS`() = testApplication {
        val flow = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 100)

        application { wsModule(flow) }

        // Setup DB
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        conn.createStatement().execute(
            """
            CREATE TABLE readings (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              timestamp TEXT,
              leader_id INTEGER,
              round INTEGER,
              node_id INTEGER,
              role TEXT,
              temp_c REAL,
              humidity_pct REAL,
              pressure_hpa REAL,
              light_lux REAL,
              air_quality_raw REAL
            );
            """.trimIndent()
        )

        val repo = SQLiteReadingRepository(conn)

        val client = createClient { install(WebSockets) }
        val session = client.webSocketSession("/ws")

        val jsonInput = """
        {
          "msg_type": "aggregate",
          "leader_id": 9,
          "round": 3,
          "readings": {
            "2": {
              "role": "sensor",
              "temp_c": 30.0
            }
          }
        }
        """.trimIndent()

        runBlocking {
            processAggregateMessage(jsonInput, repo, flow)
        }

        val frame = session.incoming.receive() as Frame.Text
        assertEquals(jsonInput, frame.readText())

        val rs = conn.createStatement().executeQuery("SELECT * FROM readings")
        assertTrue(rs.next())
        assertEquals(9, rs.getInt("leader_id"))
        assertEquals(3, rs.getInt("round"))
        assertEquals(2, rs.getInt("node_id"))
        assertEquals(30.0, rs.getDouble("temp_c"))
    }
}