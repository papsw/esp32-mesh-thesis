package collector

import kotlin.test.*
import kotlinx.serialization.json.*

class JsonTest {
    @Test
    fun `decode AggregateMessage`() {
        val input = """
          {
            "msg_type":"aggregate",
            "leader_id":1,
            "round":5,
            "readings":{
              "3":{"role":"sensor","temp_c":20.5,"humidity_pct":55}
            }
          }
        """

        val msg = Json.decodeFromString<AggregateMessage>(input)

        assertEquals("aggregate", msg.msg_type)
        assertEquals(1, msg.leader_id)
        assertEquals(5, msg.round)
        assertEquals(20.5, msg.readings["3"]!!.temp_c)
    }
}