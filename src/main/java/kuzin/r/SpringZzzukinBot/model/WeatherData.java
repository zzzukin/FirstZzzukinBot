package kuzin.r.SpringZzzukinBot.model;

import lombok.Data;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="weather_data")
public class WeatherData {
    @Id
    private long timestamp;

    @Embedded
    OpenWeatherMap openWeatherMap;

    @Embedded
    WaterLevel waterLevel;

    String result;

    String resultAuthor;

    ResultLocation resultLocation;
}
