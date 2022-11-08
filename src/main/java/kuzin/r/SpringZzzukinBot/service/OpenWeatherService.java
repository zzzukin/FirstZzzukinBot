package kuzin.r.SpringZzzukinBot.service;


import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class OpenWeatherService implements WeatherService {

    private final String key;
    private final String city;
    private final String url;
    private final String lang;

    public OpenWeatherService(
            @Value("${weather.url}") String url,
            @Value("${weather.city}") String city,
            @Value("${weather.key}") String key,
            @Value("${weather.lang}") String lang) {
        this.key = key;
        this.city = city;
        this.url = url;
        this.lang = lang;
    }

    @Override
    public JSONObject getWeather() {
        String requestedUrl = String.format(url, city, key, lang);
        String response = getUrlContent(requestedUrl);
        return new JSONObject(response);
    }

    @Override
    public String getResource() {
        return url;
    }

    private String getUrlContent(String urlAddress) {
        String content = "";
        try {
            URL url = new URL(urlAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(1000 * 180);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                content = parseResponse(connection.getInputStream());
            } else {
                log.info("Connection failed. Response code: {}", responseCode);
                throw new RuntimeException("Connection failed status code: " + responseCode);
            }
        } catch (Exception e) {
            log.info("Get URL content failed: {}", e.getMessage());
        }

        return content;
    }

    private String parseResponse(InputStream stream) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error("Parse URL content fail: {}", e.getMessage());
        }

        return content.toString();
    }
}
