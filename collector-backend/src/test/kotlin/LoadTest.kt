package collector

import kotlin.test.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest

class LoadTest {

    @Test
    fun `process 1000 messages under load`() = runTest {
        val repo = FakeRepo()
        val flow = MutableSharedFlow<String>(extraBufferCapacity = 100)

        val msg = """
        {
          "msg_type":"aggregate",
          "leader_id":1,
          "round":1,
          "readings":{"1":{"role":"node","temp_c":25.0}}
        }
        """.trimIndent()

        repeat(1000) {
            processAggregateMessage(msg, repo, flow)
        }

        assertEquals(1000, repo.inserts)
    }
}