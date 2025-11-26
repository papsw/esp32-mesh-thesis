import com.fazecast.jSerialComm.SerialPort
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

@Serializable
data class ReadingEntry(
    val role: String? = null,
    val temp_c: Double? = null,
    val humidity_pct: Double? = null,
    val pressure_hpa: Double? = null,
    val light_lux: Double? = null,
    val air_quality_raw: Double? = null
)

@Serializable
data class AggregateMessage(
    val msg_type: String,
    val leader_id: Int,
    val round: Int,
    val readings: Map<String, ReadingEntry>
)

fun openDb(): Connection {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection("jdbc:sqlite:mesh.db")
    conn.createStatement().use { st ->
        st.execute(
            """
            CREATE TABLE IF NOT EXISTS readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT NOT NULL,
                leader_id INTEGER NOT NULL,
                round INTEGER NOT NULL,
                node_id INTEGER NOT NULL,
                role TEXT,
                temp_c REAL,
                humidity_pct REAL,
                pressure_hpa REAL,
                light_lux REAL,
                air_quality_raw REAL
            );
            """.trimIndent()
        )
    }
    return conn
}

fun Application.wsModule(broadcastChannel: Channel<String>) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(20)
    }
    routing {
        webSocket("/ws") {
            for (msg in broadcastChannel) {
                send(msg)
            }
        }
    }
}

fun main() = runBlocking {
    //serial
    /*val portName =
        if (System.getProperty("os.name").startsWith("Windows")) "COM3"
        else "/dev/ttyUSB0"

    val serial = SerialPort.getCommPort(portName).apply { baudRate = 115200 }
    if (!serial.openPort()) return@runBlocking
    */
    // temp serial port rm cause of no hardware
    println("Backend in sim mode. Waiting for simulator...")
    val socket = java.net.ServerSocket(9091).accept()
    println("Simulator connected.")
    val reader = socket.getInputStream().bufferedReader()


    val json = Json { ignoreUnknownKeys = true }
    val conn = openDb()
    val broadcastChannel = Channel<String>(Channel.BUFFERED)

    embeddedServer(Netty, port = 9090) { wsModule(broadcastChannel) }
        .start(wait = false)

    //val reader = serial.inputStream.bufferedReader()

    while (true) {
        val line = reader.readLine() ?: continue
        try {
            val msg = json.decodeFromString<AggregateMessage>(line)
            if (msg.msg_type != "aggregate") continue

            val ts = Instant.now().toString()
            val ps = conn.prepareStatement(
                """
                INSERT INTO readings (
                    timestamp, leader_id, round, node_id,
                    role, temp_c, humidity_pct, pressure_hpa,
                    light_lux, air_quality_raw
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            )

            for ((k, r) in msg.readings) {
                val id = k.toInt()
                ps.setString(1, ts)
                ps.setInt(2, msg.leader_id)
                ps.setInt(3, msg.round)
                ps.setInt(4, id)
                ps.setString(5, r.role)
                ps.setObject(6, r.temp_c)
                ps.setObject(7, r.humidity_pct)
                ps.setObject(8, r.pressure_hpa)
                ps.setObject(9, r.light_lux)
                ps.setObject(10, r.air_quality_raw)
                ps.executeUpdate()
            }

            broadcastChannel.send(line)
        } catch (_: Exception) {}
    }
}