package kuzin.r.SpringZzzukinBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
public class Rain {
    @Column(name="rain_1h")
    @JsonProperty("1h")
    private int lastOneHour;
    @Column(name="rain_3h")
    @JsonProperty("3h")
    private int lastThreeHour;
}
