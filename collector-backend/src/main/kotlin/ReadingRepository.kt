package collector

interface ReadingRepository {
    fun insert(
        leaderId: Int,
        round: Int,
        nodeId: Int,
        r: ReadingEntry,
        timestamp: String
    )
}