import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import java.time.Instant

fun main() = application {
    var dark by remember { mutableStateOf(true) }

    Window(onCloseRequest = ::exitApplication, title = "ESP32 Mesh Dashboard") {
        MaterialTheme(colors = if (dark) darkColors() else lightColors()) {
            UI(dark) { dark = !dark }
        }
    }
}

@Composable
fun UI(dark: Boolean, toggle: () -> Unit) {
    var readings by remember { mutableStateOf(DB.latest()) }
    var leader by remember { mutableStateOf(DB.currentLeader()) }
    var updated by remember { mutableStateOf(Instant.now()) }

    LaunchedEffect(Unit) {
        launch {
            ws { 
                readings = DB.latest()
                leader = it.leader_id
                updated = Instant.now()
            }
        }
    }

    Column(
        Modifier.fillMaxSize()
            .background(if (dark) Color(0xFF121212) else Color.White)
            .padding(16.dp)
    ) {
        Row {
            Text("Dashboard", Modifier.weight(1f))
            Switch(dark, { toggle() })
        }

        Text("Leader: $leader")
        Text("Updated: $updated")
        Spacer(Modifier.height(8.dp))

        val temps = readings.map { it.tempC }
        val hums  = readings.map { it.humidity }
        val light = readings.map { it.light }
        val gas   = readings.map { it.air }

        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text("Temperature")
            LineChart(avg(temps, 5), Color.Red)
            Spacer(Modifier.height(16.dp))

            Text("Humidity")
            LineChart(avg(hums, 5), Color.Blue)
            Spacer(Modifier.height(16.dp))

            Text("Light")
            LineChart(avg(light, 5), Color.Yellow)
            Spacer(Modifier.height(16.dp))

            Text("Air Quality")
            LineChart(avg(gas, 5), Color.Green)
        }
    }
}