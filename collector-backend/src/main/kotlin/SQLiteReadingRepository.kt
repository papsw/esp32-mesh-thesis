package collector

import java.sql.Connection

class SQLiteReadingRepository(conn: Connection) : ReadingRepository {

    private val stmt = conn.prepareStatement(
        """
        INSERT INTO readings (
            timestamp, leader_id, round, node_id,
            role, temp_c, humidity_pct, pressure_hpa,
            light_lux, air_quality_raw
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
    )

    override fun insert(
        leaderId: Int,
        round: Int,
        nodeId: Int,
        r: ReadingEntry,
        timestamp: String
    ) {
        stmt.setString(1, timestamp)
        stmt.setInt(2, leaderId)
        stmt.setInt(3, round)
        stmt.setInt(4, nodeId)
        stmt.setString(5, r.role)
        stmt.setObject(6, r.temp_c)
        stmt.setObject(7, r.humidity_pct)
        stmt.setObject(8, r.pressure_hpa)
        stmt.setObject(9, r.light_lux)
        stmt.setObject(10, r.air_quality_raw)
        stmt.executeUpdate()
    }
}