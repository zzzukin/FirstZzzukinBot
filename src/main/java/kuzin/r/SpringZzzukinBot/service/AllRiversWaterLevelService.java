package kuzin.r.SpringZzzukinBot.service;

import kuzin.r.SpringZzzukinBot.model.WaterLevel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AllRiversWaterLevelService implements WaterLevelService<WaterLevel> {

    private final String url;

    public AllRiversWaterLevelService(
            @Value("${water.lvl.url}") String url) {
        this.url = url;
        log.info("Water level URL: {}", url);
    }

// Search html:
// <li class="list-item"> <span>Уровень воды: (<a href="/gauge/don-rostov-na-donu/waterlevel">график</a>)</span> <b>42 см (-8) </b> </li>
    @Override
    public WaterLevel getWaterLevel() {
        WaterLevel level = new WaterLevel();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("li.list-item").select("b:contains(см)");
            if(!elements.isEmpty()) {
                ArrayList<String> digits = Arrays.stream(elements.first().text()
                                .replaceAll("[()]", "")
                                .split(" "))
                        .filter(item -> item.matches("-?\\+?\\d+"))
                        .collect(Collectors.toCollection(ArrayList::new));

                level.setLevel(digits.get(0));
                level.setDiff(digits.get(1));
            }
            else {
                log.info("Water level not found: {}", url);
                throw new RuntimeException(String.format("Water level not found: %s", url));
            }
        } catch (IOException e) {
            log.info("Connection failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return level;
    }
}
