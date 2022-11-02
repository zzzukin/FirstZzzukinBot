package kuzin.r.SpringZzzukinBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class Clouds {
    @Column(name="cloudiness")
    @JsonProperty("all")
    private int allClouds;
}
