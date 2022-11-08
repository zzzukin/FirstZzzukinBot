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

    String result = "";

    String resultAuthor = "";

    @Embedded
    OpenWeatherMap openWeatherMap = new OpenWeatherMap();

    @Embedded
    WaterLevel waterLevel = new WaterLevel();

    @Embedded
    ResultLocation resultLocation = new ResultLocation();
}
