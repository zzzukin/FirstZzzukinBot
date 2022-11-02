package kuzin.r.SpringZzzukinBot.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class WaterLevel {
    @Column(name="water_level")
    private String level;

    @Column(name="water_level_diff")
    private String diff;
}
