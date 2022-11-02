package kuzin.r.SpringZzzukinBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Data
@Embeddable
public class Sys {
    @Transient
    @JsonProperty("type")
    private int type;

    @Transient
    @JsonProperty("id")
    private int id;

    @JsonProperty("country")
    private String country;

    @JsonProperty("sunrise")
    private int sunrise;

    @JsonProperty("sunset")
    private int sunset;
}
