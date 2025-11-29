package collector

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.MutableSharedFlow
import java.sql.DriverManager

class DbTest {

    @Test
    fun `insert readings into database`() = runTest {
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

        val json = """
          {
            "msg_type":"aggregate",
            "leader_id":2,
            "round":6,
            "readings":{
              "7": {"role":"node","temp_c":21.3}
            }
          }
        """

        processAggregateMessage(json, repo, flow)

        val rs = conn.createStatement().executeQuery("SELECT * FROM readings")
        assertTrue(rs.next())
        assertEquals(2, rs.getInt("leader_id"))
        assertEquals(6, rs.getInt("round"))
        assertEquals(7, rs.getInt("node_id"))
        assertEquals(21.3, rs.getDouble("temp_c"))
    }
}