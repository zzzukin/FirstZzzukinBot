package kuzin.r.SpringZzzukinBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class Coordinates {
    @JsonProperty("lon")
    private double lon;

    @JsonProperty("lat")
    private double lat;
}
