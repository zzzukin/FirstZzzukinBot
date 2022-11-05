package kuzin.r.SpringZzzukinBot.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="weather_data")
public class WeatherData {
    @Id
    private long timestamp;
    @Embedded
    OpenWeatherMap openWeatherMap;
    @Embedded
    WaterLevel waterLevel;

    public WeatherData() {
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public OpenWeatherMap getOpenWeatherMap() {
        return this.openWeatherMap;
    }

    public WaterLevel getWaterLevel() {
        return this.waterLevel;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setOpenWeatherMap(OpenWeatherMap openWeatherMap) {
        this.openWeatherMap = openWeatherMap;
    }

    public void setWaterLevel(WaterLevel waterLevel) {
        this.waterLevel = waterLevel;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof WeatherData)) return false;
        final WeatherData other = (WeatherData) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getTimestamp() != other.getTimestamp()) return false;
        final Object this$openWeatherMap = this.getOpenWeatherMap();
        final Object other$openWeatherMap = other.getOpenWeatherMap();
        if (this$openWeatherMap == null ? other$openWeatherMap != null : !this$openWeatherMap.equals(other$openWeatherMap))
            return false;
        final Object this$waterLevel = this.getWaterLevel();
        final Object other$waterLevel = other.getWaterLevel();
        if (this$waterLevel == null ? other$waterLevel != null : !this$waterLevel.equals(other$waterLevel))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof WeatherData;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $timestamp = this.getTimestamp();
        result = result * PRIME + (int) ($timestamp >>> 32 ^ $timestamp);
        final Object $openWeatherMap = this.getOpenWeatherMap();
        result = result * PRIME + ($openWeatherMap == null ? 43 : $openWeatherMap.hashCode());
        final Object $waterLevel = this.getWaterLevel();
        result = result * PRIME + ($waterLevel == null ? 43 : $waterLevel.hashCode());
        return result;
    }

    public String toString() {
        return "WeatherData(timestamp=" + this.getTimestamp() + ", openWeatherMap=" + this.getOpenWeatherMap() + ", waterLevel=" + this.getWaterLevel() + ")";
    }
}
