package meshsim

import kotlinx.serialization.json.*

object SimSensors {
    fun zone(): JsonObject = buildJsonObject {
        put("role", "zone")
        put("temp_c", 20 + Math.random() * 5)
        put("humidity_pct", 50 + Math.random() * 10)
        put("pressure_hpa", 1000 + Math.random() * 15)
    }

    fun light(): JsonObject = buildJsonObject {
        put("role", "light_sensor")
        put("light_lux", 200 + Math.random() * 200)
    }

    fun gas(): JsonObject = buildJsonObject {
        put("role", "air_quality")
        put("air_quality_raw", (200..600).random())
    }
}