package kuzin.r.SpringZzzukinBot.repository;


import kuzin.r.SpringZzzukinBot.model.WeatherData;
import org.springframework.data.repository.CrudRepository;

public interface WeatherRepository extends CrudRepository<WeatherData, Long> {
    WeatherData findTopByOrderByTimestampDesc();
    WeatherData findTopByOrderByTimestamp();
}
