package kuzin.r.SpringZzzukinBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class Wind {
    @Column(name="wind_speed")
    @JsonProperty("speed")
    private double speed;

    @Column(name="wind_direction")
    @JsonProperty("deg")
    private int deg;

    @Column(name="wind_gust")
    @JsonProperty("gust")
    private int gust;
}
