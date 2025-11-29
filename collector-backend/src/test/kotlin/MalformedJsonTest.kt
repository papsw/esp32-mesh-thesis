package collector

import kotlin.test.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest

class FakeRepo : ReadingRepository {
    var inserts = 0
    override fun insert(
        leaderId: Int,
        round: Int,
        nodeId: Int,
        r: ReadingEntry,
        timestamp: String
    ) {
        inserts++
    }
}

class MalformedJsonTest {

    @Test
    fun `malformed json does not crash`() = runTest {
        val repo = FakeRepo()
        val flow = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 100)

        val badInputs = listOf(
            "{",
            "not json",
            """{"msg_type":123}""",
            """{"msg_type":"idk"}""",
            """{"readings":123}""",
            """{"msg_type":"aggregate"}""",
            ""
        )

        for (line in badInputs) {
            try {
                processAggregateMessage(line, repo, flow)
            } catch (e: Exception) {
                fail("Should not throw on '$line' but got: $e")
            }
        }

        assertEquals(0, repo.inserts)
        assertTrue(flow.replayCache.isEmpty())
    }
}