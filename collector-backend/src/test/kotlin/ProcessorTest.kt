package collector

import kotlin.test.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import java.sql.DriverManager

class ProcessorTest {

    @Test
    fun `full message processing inserts DB and broadcasts`() = runTest {
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
        val flow = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 100)

        val input = """
          {
            "msg_type":"aggregate",
            "leader_id":5,
            "round":1,
            "readings":{
                "1": {"role": "node", "temp_c": 19.8}
            }
          }
        """

        processAggregateMessage(input, repo, flow)

        val rs = conn.createStatement().executeQuery("SELECT * FROM readings")
        assertTrue(rs.next())
        assertEquals(5, rs.getInt("leader_id"))

      assertEquals(input.trim(), flow.replayCache.first().trim())
    }
}