package me.spica.spicaweather3.data.local.db.type_converter

import androidx.annotation.Keep
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.spica.spicaweather3.data.remote.api.model.weather.AggregatedWeatherData

@Keep
@Suppress("unused")
class RoomTypeConverters {

  private val gson = GsonBuilder().serializeNulls().create()


  @TypeConverter
  fun nowToString(data: AggregatedWeatherData?): String? {
    if (data == null) return ""
    return try {
      gson.toJson(data)
    } catch (e: Exception) {
      // 序列化失败时回退为空串，避免写入路径抛出导致刷新流程崩溃
      ""
    }
  }

  @TypeConverter
  fun stringToNow(json: String?): AggregatedWeatherData? {
    if (json.isNullOrEmpty()) return null
    return try {
      gson.fromJson<AggregatedWeatherData>(
        json,
        object : TypeToken<AggregatedWeatherData>() {}.type
      )
    } catch (e: Exception) {
      // 损坏或与新版本 schema 不兼容的 JSON（Gson 不遵守 Kotlin non-null 约束，
      // 旧数据缺少新增字段时会被置为 null）——视为无天气数据，由上层展示空状态并在刷新后自愈
      null
    }
  }

}