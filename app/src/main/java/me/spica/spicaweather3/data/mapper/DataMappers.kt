package me.spica.spicaweather3.data.mapper

import me.spica.spicaweather3.data.local.db.entity.CityEntity
import me.spica.spicaweather3.data.remote.api.model.Location
import me.spica.spicaweather3.data.remote.api.model.weather.*
import me.spica.spicaweather3.domain.model.*
import me.spica.spicaweather3.domain.model.LocationInfo as DomainLocationInfo
import me.spica.spicaweather3.domain.model.WeatherData as DomainWeatherData

/**
 * 数据映射器 - 在 data 层和 domain 层之间转换数据
 *
 * 注意：data 层的天气模型由 Gson 反序列化（Retrofit 与 Room TypeConverter 均使用 Gson）。
 * Gson 不遵守 Kotlin non-null 约束，会通过反射绕过构造函数及其空检查，因此被声明为 non-null
 * 的字段（尤其是 String、嵌套对象、List）在运行时可能为 null（典型场景：旧版本持久化的 JSON
 * 缺少新版本新增的字段）。故所有 data→domain 映射均按 nullable 处理：任一必需数据缺失时返回 null，
 * 由上层展示空状态并在刷新后自愈，而不是抛出 NPE 导致整个 Flow 崩溃。
 */

// ============ CityEntity <-> City ============

fun CityEntity.toDomain(): City = City(
    id = id,
    name = name,
    latitude = lat,
    longitude = lon,
    administrativeArea1 = adm1,
    administrativeArea2 = adm2,
    sortOrder = sort,
    isUserLocation = isUserLoc,
    weather = weather?.toDomain()
)

fun City.toEntity(): CityEntity = CityEntity(
    id = id,
    name = name,
    lat = latitude,
    lon = longitude,
    adm1 = administrativeArea1,
    adm2 = administrativeArea2,
    sort = sortOrder,
    isUserLoc = isUserLocation,
    weather = weather?.toDataModel()
)

// ============ Location -> SearchLocation ============

fun Location.toSearchLocation(): SearchLocation = SearchLocation(
    id = id,
    name = name,
    latitude = lat,
    longitude = lon,
    administrativeArea1 = adm1,
    administrativeArea2 = adm2,
    country = country,
    type = type,
    rank = rank
)

fun SearchLocation.toCity(): City = City(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    administrativeArea1 = administrativeArea1,
    administrativeArea2 = administrativeArea2,
    sortOrder = System.currentTimeMillis(),
    isUserLocation = false,
    weather = null
)

// ============ AggregatedWeatherData <-> WeatherData ============

fun AggregatedWeatherData?.toDomain(): DomainWeatherData? {
    if (this == null) return null
    // Gson 可能在运行时把 non-null 子对象置为 null，逐项校验，任一缺失则整体视为无天气数据
    val current = current.toDomain() ?: return null
    val forecast = forecast.toDomain() ?: return null
    val airQuality = airQuality.toDomain() ?: return null
    val location = location.toDomain() ?: return null
    return DomainWeatherData(
        generatedAt = generatedAt.orEmpty(),
        location = location,
        current = current,
        forecast = forecast,
        minutelyPrecip = minutelyPrecip?.toDomain(),
        airQuality = airQuality,
        weatherAlerts = weatherAlerts?.mapNotNull { it.toDomain() }
    )
}

fun DomainWeatherData.toDataModel(): AggregatedWeatherData = AggregatedWeatherData(
    generatedAt = generatedAt,
    location = location.toDataModel(),
    current = current.toDataModel(),
    forecast = forecast.toDataModel(),
    minutelyPrecip = minutelyPrecip?.toDataModel(),
    airQuality = airQuality.toDataModel(),
    weatherAlerts = weatherAlerts?.map { it.toDataModel() }
)

// ============ LocationInfo ============

fun me.spica.spicaweather3.data.remote.api.model.weather.LocationInfo?.toDomain(): DomainLocationInfo? {
    if (this == null) return null
    return DomainLocationInfo(
        name = name.orEmpty(),
        latitude = latitude.orEmpty(),
        longitude = longitude.orEmpty()
    )
}

fun DomainLocationInfo.toDataModel(): me.spica.spicaweather3.data.remote.api.model.weather.LocationInfo =
    me.spica.spicaweather3.data.remote.api.model.weather.LocationInfo(
        name = name,
        latitude = latitude,
        longitude = longitude
    )

// ============ CurrentWeather ============

fun me.spica.spicaweather3.data.remote.api.model.weather.CurrentWeather?.toDomain(): me.spica.spicaweather3.domain.model.CurrentWeather? {
    if (this == null) return null
    return me.spica.spicaweather3.domain.model.CurrentWeather(
        obsTime = obsTime.orEmpty(),
        temperature = temperature,
        feelsLike = feelsLike,
        condition = condition.orEmpty(),
        icon = icon.orEmpty(),
        humidity = humidity,
        precipitation = precipitation,
        pressure = pressure,
        visibility = visibility,
        windDirection = windDirection,
        windDirectionText = windDirectionText.orEmpty(),
        windScale = windScale.orEmpty(),
        windSpeed = windSpeed,
        cloudCover = cloudCover
    )
}

fun me.spica.spicaweather3.domain.model.CurrentWeather.toDataModel(): me.spica.spicaweather3.data.remote.api.model.weather.CurrentWeather =
    me.spica.spicaweather3.data.remote.api.model.weather.CurrentWeather(
        obsTime = obsTime,
        temperature = temperature,
        feelsLike = feelsLike,
        condition = condition,
        icon = icon,
        humidity = humidity,
        precipitation = precipitation,
        pressure = pressure,
        visibility = visibility,
        windDirection = windDirection,
        windDirectionText = windDirectionText,
        windScale = windScale,
        windSpeed = windSpeed,
        cloudCover = cloudCover
    )

// ============ ForecastSummary <-> ForecastData ============

fun ForecastSummary?.toDomain(): ForecastData? {
    if (this == null) return null
    val today = today.toDomain() ?: return null
    val tomorrow = tomorrow.toDomain() ?: return null
    return ForecastData(
        today = today,
        tomorrow = tomorrow,
        next7Days = next7Days.orEmpty().mapNotNull { it.toDomain() },
        next24Hours = next24Hours?.mapNotNull { it.toDomain() }
    )
}

fun ForecastData.toDataModel(): ForecastSummary = ForecastSummary(
    today = today.toDataModel(),
    tomorrow = tomorrow.toDataModel(),
    next7Days = next7Days.map { it.toDataModel() },
    next24Hours = next24Hours?.map { it.toDataModel() }
)

// ============ DailyForecast ============

fun me.spica.spicaweather3.data.remote.api.model.weather.DailyForecast?.toDomain(): me.spica.spicaweather3.domain.model.DailyForecast? {
    if (this == null) return null
    return me.spica.spicaweather3.domain.model.DailyForecast(
        date = date.orEmpty(),
        tempMax = tempMax,
        tempMin = tempMin,
        dayCondition = dayCondition.orEmpty(),
        dayIcon = dayIcon.orEmpty(),
        nightCondition = nightCondition.orEmpty(),
        nightIcon = nightIcon.orEmpty(),
        precipitation = precipitation,
        humidity = humidity,
        uvIndex = uvIndex,
        sunrise = sunrise.orEmpty(),
        sunset = sunset.orEmpty(),
        visibility = vis.orEmpty(),
        cloud = cloud.orEmpty(),
        wind360Day = wind360Day,
        wind360Night = wind360Night,
        windDirDay = windDirDay.orEmpty(),
        windDirNight = windDirNight.orEmpty(),
        windSpeedDay = windSpeedDay.orEmpty(),
        windSpeedNight = windSpeedNight.orEmpty(),
        windScaleDay = windScaleDay.orEmpty(),
        windScaleNight = windScaleNight.orEmpty()
    )
}

fun me.spica.spicaweather3.domain.model.DailyForecast.toDataModel(): me.spica.spicaweather3.data.remote.api.model.weather.DailyForecast =
    me.spica.spicaweather3.data.remote.api.model.weather.DailyForecast(
        date = date,
        tempMax = tempMax,
        tempMin = tempMin,
        dayCondition = dayCondition,
        dayIcon = dayIcon,
        nightCondition = nightCondition,
        nightIcon = nightIcon,
        precipitation = precipitation,
        humidity = humidity,
        uvIndex = uvIndex,
        sunrise = sunrise,
        sunset = sunset,
        vis = visibility,
        cloud = cloud,
        wind360Day = wind360Day,
        wind360Night = wind360Night,
        windDirDay = windDirDay,
        windDirNight = windDirNight,
        windSpeedDay = windSpeedDay,
        windSpeedNight = windSpeedNight,
        windScaleDay = windScaleDay,
        windScaleNight = windScaleNight
    )

// ============ HourlyForecast ============

fun me.spica.spicaweather3.data.remote.api.model.weather.HourlyForecast?.toDomain(): me.spica.spicaweather3.domain.model.HourlyForecast? {
    if (this == null) return null
    return me.spica.spicaweather3.domain.model.HourlyForecast(
        time = time.orEmpty(),
        temperature = temperature,
        condition = condition.orEmpty(),
        icon = icon.orEmpty(),
        precipProbability = precipProbability,
        precipitation = precipitation,
        windDirection = windDirection.orEmpty(),
        windScale = windScale.orEmpty(),
        humidity = humidity,
        wind360 = wind360,
        pop = pop,
        windSpeed = windSpeed
    )
}

fun me.spica.spicaweather3.domain.model.HourlyForecast.toDataModel(): me.spica.spicaweather3.data.remote.api.model.weather.HourlyForecast =
    me.spica.spicaweather3.data.remote.api.model.weather.HourlyForecast(
        time = time,
        temperature = temperature,
        condition = condition,
        icon = icon,
        precipProbability = precipProbability,
        precipitation = precipitation,
        windDirection = windDirection,
        windScale = windScale,
        humidity = humidity,
        wind360 = wind360,
        pop = pop,
        windSpeed = windSpeed
    )

// ============ MinutelyPrecipSummary <-> MinutelyPrecipData ============

fun MinutelyPrecipSummary?.toDomain(): MinutelyPrecipData? {
    if (this == null) return null
    return MinutelyPrecipData(
        summary = summary.orEmpty(),
        isPrecipitating = isPrecipitating,
        precipType = precipType,
        currentIntensity = currentIntensity,
        next2Hours = next2Hours.orEmpty().mapNotNull { it.toDomain() }
    )
}

fun MinutelyPrecipData.toDataModel(): MinutelyPrecipSummary = MinutelyPrecipSummary(
    summary = summary,
    isPrecipitating = isPrecipitating,
    precipType = precipType,
    currentIntensity = currentIntensity,
    next2Hours = next2Hours.map { it.toDataModel() }
)

// ============ MinutelyPrecip ============

fun me.spica.spicaweather3.data.remote.api.model.weather.MinutelyPrecip?.toDomain(): me.spica.spicaweather3.domain.model.MinutelyPrecip? {
    if (this == null) return null
    return me.spica.spicaweather3.domain.model.MinutelyPrecip(
        time = time.orEmpty(),
        precipitation = precipitation,
        type = type.orEmpty()
    )
}

fun me.spica.spicaweather3.domain.model.MinutelyPrecip.toDataModel(): me.spica.spicaweather3.data.remote.api.model.weather.MinutelyPrecip =
    me.spica.spicaweather3.data.remote.api.model.weather.MinutelyPrecip(
        time = time,
        precipitation = precipitation,
        type = type
    )

// ============ AirQualitySummary <-> AirQualityData ============

fun AirQualitySummary?.toDomain(): AirQualityData? {
    if (this == null) return null
    return AirQualityData(
        aqi = aqi,
        level = level,
        category = category.orEmpty(),
        primaryPollutant = primaryPollutant.orEmpty(),
        primaryPollutantName = primaryPollutantName.orEmpty(),
        healthEffect = healthEffect.orEmpty(),
        healthAdvice = healthAdvice.orEmpty(),
        pm25 = pm25,
        pm10 = pm10
    )
}

fun AirQualityData.toDataModel(): AirQualitySummary = AirQualitySummary(
    aqi = aqi,
    level = level,
    category = category,
    primaryPollutant = primaryPollutant,
    primaryPollutantName = primaryPollutantName,
    healthEffect = healthEffect,
    healthAdvice = healthAdvice,
    pm25 = pm25,
    pm10 = pm10
)

// ============ WeatherAlertSummary <-> WeatherAlert ============

fun WeatherAlertSummary?.toDomain(): WeatherAlert? {
    if (this == null) return null
    return WeatherAlert(
        id = id.orEmpty(),
        headline = headline.orEmpty(),
        eventType = eventType.orEmpty(),
        eventCode = eventCode.orEmpty(),
        severity = severity.orEmpty(),
        colorCode = colorCode.orEmpty(),
        description = description.orEmpty(),
        instruction = instruction.orEmpty(),
        issuedTime = issuedTime.orEmpty(),
        effectiveTime = effectiveTime.orEmpty(),
        expireTime = expireTime.orEmpty()
    )
}

fun WeatherAlert.toDataModel(): WeatherAlertSummary = WeatherAlertSummary(
    id = id,
    headline = headline,
    eventType = eventType,
    eventCode = eventCode,
    severity = severity,
    colorCode = colorCode,
    description = description,
    instruction = instruction,
    issuedTime = issuedTime,
    effectiveTime = effectiveTime,
    expireTime = expireTime
)
