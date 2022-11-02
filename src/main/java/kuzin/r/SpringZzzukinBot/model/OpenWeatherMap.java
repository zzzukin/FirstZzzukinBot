package kuzin.r.SpringZzzukinBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Embeddable
public class OpenWeatherMap {

    @Embedded
    @JsonProperty("coord")
    private Coordinates coordinates;

    @ElementCollection
    @AttributeOverride(name = "id", column = @Column(name = "weather_id"))
    @CollectionTable(name = "weather", joinColumns = @JoinColumn(name = "timestamp"))
    @Column(name = "weather")
    @JsonProperty("weather")
    private List<Weather> weather;

    @Transient
    @JsonProperty("base")
    private String base;

    @Embedded
    @JsonProperty("main")
    private Main main;

    @JsonProperty("visibility")
    private int visibility;

    @Embedded
    @JsonProperty("wind")
    private Wind wind;

    @Embedded
    @JsonProperty("clouds")
    private Clouds clouds;

    @Embedded
    @JsonProperty("rain")
    private Rain rain;

    @Embedded
    @JsonProperty("snow")
    private Snow snow;

    @Transient
    @JsonProperty("dt")
    private int dt;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "sys_id"))
    @JsonProperty("sys")
    private Sys sys;

    @Transient
    @JsonProperty("timezone")
    private int timezone;

    @Transient
    @JsonProperty("id")
    private int id;

    @Column(name="city")
    @JsonProperty("name")
    private String name;

    @Transient
    @JsonProperty("cod")
    private int cod;

    @Override
    public String toString() {
        return "{" +
                ", coord=" + coordinates +
                ", weather=" + weather +
                ", base='" + base + '\'' +
                ", main=" + main +
                ", visibility=" + visibility +
                ", wind=" + wind +
                ", clouds=" + clouds +
                ", dt=" + dt +
                ", sys=" + sys +
                ", timezone=" + timezone +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", cod=" + cod +
                '}';
    }
}
