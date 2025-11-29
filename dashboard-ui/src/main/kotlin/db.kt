package dashboard

import java.sql.Connection
import java.sql.DriverManager

data class Reading(
    val timestamp: String,
    val leaderId: Int,
    val round: Int,
    val nodeId: Int,
    val role: String?,
    val tempC: Double?,
    val humidity: Double?,
    val pressure: Double?,
    val light: Double?,
    val air: Double?
)

object DB {
    private val conn: Connection

    init {
        Class.forName("org.sqlite.JDBC")
        conn = DriverManager.getConnection("jdbc:sqlite:../collector-backend/mesh.db")
    }

    fun latest(n: Int = 500): List<Reading> {
        val rs = conn.createStatement().executeQuery(
            """
            SELECT * FROM readings ORDER BY timestamp DESC LIMIT $n
            """
        )
        val out = mutableListOf<Reading>()
        while (rs.next()) {
            out += Reading(
                rs.getString("timestamp"),
                rs.getInt("leader_id"),
                rs.getInt("round"),
                rs.getInt("node_id"),
                rs.getString("role"),
                rs.getDouble("temp_c").takeUnless { rs.wasNull() },
                rs.getDouble("humidity_pct").takeUnless { rs.wasNull() },
                rs.getDouble("pressure_hpa").takeUnless { rs.wasNull() },
                rs.getDouble("light_lux").takeUnless { rs.wasNull() },
                rs.getDouble("air_quality_raw").takeUnless { rs.wasNull() }
            )
        }
        return out.reversed()
    }

    fun currentLeader(): Int? =
        conn.createStatement().executeQuery(
            "SELECT leader_id FROM readings ORDER BY timestamp DESC LIMIT 1"
        ).let { if (it.next()) it.getInt(1) else null }
}