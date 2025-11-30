package dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import java.time.Instant

private val LightColors = lightColors(
    primary = Color(0xFF00897B),
    primaryVariant = Color(0xFF00695C),
    secondary = Color(0xFFFFA726),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A)
)

private val DarkColors = darkColors(
    primary = Color(0xFF80CBC4),
    primaryVariant = Color(0xFF4DB6AC),
    secondary = Color(0xFFFFCC80),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFEFEFEF),
    onSurface = Color(0xFFEFEFEF)
)

fun main() = application {
    var dark by remember { mutableStateOf(true) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ESP32 Mesh Dashboard"
    ) {
        MaterialTheme(colors = if (dark) DarkColors else LightColors) {
            DashboardUI(dark) { dark = !dark }
        }
    }
}

enum class TabPage(val label: String) {
    OVERVIEW("Overview"),
    TEMP("Temperature"),
    HUM("Humidity"),
    LIGHT("Light"),
    GAS("Air Quality")
}

@Composable
fun DashboardUI(dark: Boolean, toggleDark: () -> Unit) {
    var tempList by remember { mutableStateOf(listOf<Double>()) }
    var humList by remember { mutableStateOf(listOf<Double>()) }
    var lightList by remember { mutableStateOf(listOf<Double>()) }
    var gasList by remember { mutableStateOf(listOf<Double>()) }

    var leader by remember { mutableStateOf(0) }
    var updated by remember { mutableStateOf(Instant.now()) }
    var tab by remember { mutableStateOf(TabPage.OVERVIEW) }

    LaunchedEffect(Unit) {
        launch {
            ws { msg ->
                println("WS MESSAGE = " + msg)
                println("READINGS KEYS = " + msg.readings.keys)
                println("READINGS VALUES = " + msg.readings.values)

                val readings = msg.readings.values

                val avgTemp = readings.mapNotNull { it.tempC }.averageOrNull()
                val avgHum = readings.mapNotNull { it.humidityPct }.averageOrNull()
                val avgLight = readings.mapNotNull { it.lightLux }.averageOrNull()
                val avgGas = readings.mapNotNull { it.airQualityRaw }.averageOrNull()

                avgTemp?.let { tempList = (tempList + it).takeLast(200) }
                avgHum?.let { humList = (humList + it).takeLast(200) }
                avgLight?.let { lightList = (lightList + it).takeLast(200) }
                avgGas?.let { gasList = (gasList + it).takeLast(200) }

                leader = msg.leaderId
                updated = Instant.now()
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ESP32 Mesh Dashboard",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = toggleDark) {
                Icon(
                    if (dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Leader: $leader", color = MaterialTheme.colors.onBackground)
        Text("Updated: $updated", color = MaterialTheme.colors.onBackground)
        Spacer(Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = tab.ordinal,
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface
        ) {
            TabPage.entries.forEachIndexed { i, page ->
                Tab(
                    selected = tab.ordinal == i,
                    onClick = { tab = page },
                    text = { Text(page.label, color = MaterialTheme.colors.onSurface) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (tab) {
            TabPage.OVERVIEW ->
                OverviewTab(tempList, humList, lightList, gasList, dark)
            TabPage.TEMP ->
                SensorTab("Temperature (°C)", tempList, dark)
            TabPage.HUM ->
                SensorTab("Humidity (%)", humList, dark)
            TabPage.LIGHT ->
                SensorTab("Light (lx)", lightList, dark)
            TabPage.GAS ->
                SensorTab("Air Quality (ppm)", gasList, dark)
        }
    }
}

@Composable
fun OverviewTab(
    temp: List<Double>,
    hum: List<Double>,
    light: List<Double>,
    gas: List<Double>,
    dark: Boolean
) {
    val cT = if (dark) Color(0xFFFF8A80) else Color.Red
    val cH = if (dark) Color(0xFF80D8FF) else Color.Blue
    val cL = if (dark) Color(0xFFFFFF8D) else Color.Yellow
    val cG = if (dark) Color(0xFFA5D6A7) else Color.Green

    Column(Modifier.verticalScroll(rememberScrollState())) {
        OverviewCard("Temperature", temp, cT)
        OverviewCard("Humidity", hum, cH)
        OverviewCard("Light", light, cL)
        OverviewCard("Air Quality", gas, cG)
    }
}

@Composable
fun OverviewCard(label: String, data: List<Double>, color: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onBackground
        )
        LineChart(data.takeLast(50), color)
    }
}

@Composable
fun SensorTab(label: String, data: List<Double>, dark: Boolean) {
    val themedColor =
        if (dark) Color(0xFF80CBC4)
        else MaterialTheme.colors.primary

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(
            label,
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.onBackground
        )
        Spacer(Modifier.height(8.dp))
        LineChart(data.takeLast(150), themedColor)
    }
}

private fun List<Double>.averageOrNull(): Double? =
    if (isEmpty()) null else average()