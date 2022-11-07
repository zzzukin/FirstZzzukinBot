package kuzin.r.SpringZzzukinBot.model;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class ResultLocation {
    private Double longitude;
    private Double latitude;

    public ResultLocation() {
    }

    public ResultLocation(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
