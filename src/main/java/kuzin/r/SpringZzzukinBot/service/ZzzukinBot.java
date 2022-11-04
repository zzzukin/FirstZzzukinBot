package kuzin.r.SpringZzzukinBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;
import kuzin.r.SpringZzzukinBot.config.BotConfig;
import kuzin.r.SpringZzzukinBot.model.WaterLevel;
import kuzin.r.SpringZzzukinBot.model.OpenWeatherMap;
import kuzin.r.SpringZzzukinBot.model.WeatherData;
import kuzin.r.SpringZzzukinBot.repository.WeatherRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class ZzzukinBot extends TelegramLongPollingBot {

    @Autowired
    WeatherRepository weatherRepository;
    @Autowired
    WeatherService weatherService;
    @Autowired
    WaterLevelService waterLevelService;

    private final BotConfig config;

    private WeatherData lastSavedData = new WeatherData();

    public ZzzukinBot(BotConfig config) throws TelegramApiException {
        this.config = config;
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/help", "What this bot can do"));
        botCommands.add(new BotCommand("/start", "Predict if the fishing will be good"));
        botCommands.add(new BotCommand("/update", "Update weather"));
        botCommands.add(new BotCommand("/weather", "Show weather"));
        execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            Long chatId = message.getChatId();

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));

            switch (messageText) {
                case "/start":
                    startCommandHandler(message);
                    break;
                case "/help":
                    helpCommandHandler(message);
                    break;
                case "/update":
                    updateCommandHandler(message);
                    break;
                case "/weather":
                    weatherCommandHandler(message);
                    break;
                default:
                    unsupportedCommandHandler(message);
            }
        }
    }

    @Scheduled(cron = "*/${bot.update.data.time} * * * * *", zone = "Europe/Moscow")
    public void updateWeatherBySchedule() throws IOException {
        log.info("Update weather data {}", new Date());
        updateWeather();
    }

    private void updateCommandHandler(Message message) throws IOException, TelegramApiException {
        updateWeather();
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("Ok, so, %s, the weather has been updated!",
                message.getChat().getFirstName()));
        execute(sendMessage);
    }

    private void updateWeather() throws IOException {
        log.info("Get weather data from server");
        JSONObject json = weatherService.getWeather();
        log.info("Weather data: {}", json);
        ObjectMapper mapper = new ObjectMapper();
        OpenWeatherMap openWeatherMap = mapper.readValue(json.toString(), OpenWeatherMap.class);

        log.info("Get water level from server");
        WaterLevel level = waterLevelService.getWaterLevel();
        log.info("Received water level: {}({})", level.getLevel(), level.getDiff());

        WeatherData data = new WeatherData();
        data.setTimestamp(new Date().getTime());
        data.setOpenWeatherMap(openWeatherMap);
        data.setWaterLevel(level);

        if(!data.getOpenWeatherMap().equals(lastSavedData.getOpenWeatherMap())) {
            log.info("Save data to DB");
            weatherRepository.save(data);
            lastSavedData = data;

            if (weatherRepository.count() > config.getDbRecordsNum()) {
                log.info("Delete oldest data from DB");
                long timestamp = weatherRepository.findTopByOrderByTimestampAsc().getTimestamp();
                log.info("Top timestamp: {}", timestamp);
                weatherRepository.deleteByTimestamp(timestamp);
            }
        }
    }

    private WeatherData getWeather() {
        return weatherRepository.findTopByOrderByTimestampDesc();
    }

    private SendMessage getSendMessage(Message message) {
        Long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        return sendMessage;
    }

    private void helpCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
//        sendMessage.setText(String.format("Ok, so, what can i do:\n" +
//                        "I keep a close eye on the weather%s" +
//                        "and can predict how good the fishing " +
//                        "will be if you tell me about the result%s",
//                parseEmoji(":eye:"), parseEmoji(":smile:")));

        sendMessage.setText(String.format("Привет, я Херишеф - древнеегипетский бог, " +
                        "покровитель Гераклеополя, бог плодородия и воды, покровитель " +
                        "охоты и рыболовства%s%s%s https://en.wikipedia.org/wiki/Heryshaf. " +
                        "Кеххе, кеххе... да, так, вот. Я спал примерно 2000 тысячи лет пока " +
                        "меня не разбудил Ммм... некто Андрей Скляров%s%s Похоже, что за " +
                        "время сна, я все еще не растерял своих навыков и могу предсказывать " +
                        "насколько реки будут плодородны и богаты рыбой. Ты можешь попросить " +
                        "меня об этом, командой /start. В обмен на свои предсказания я хочу, " +
                        "чтобы ты делился со мной своми результатами, насколько река была " +
                        "добра к тебе и плодородна, ок?%s Видишь там в нижнем левом углу " +
                        "синяя кнопка меню? Она поможет тебе, удачи%s"
                        ,
                parseEmoji("\uD83D\uDE0E"),
                parseEmoji("\uD83D\uDC1F"),
                parseEmoji("\uD83C\uDFA3"),
                parseEmoji("\uD83E\uDD78"),
                parseEmoji("\uD83D\uDE21"),
                parseEmoji("\uD83D\uDE09"),
                parseEmoji("\uD83D\uDE42")

        ));

        execute(sendMessage);
    }

    private void startCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("So, %s, there is one small circumstance. " +
                        "At the moment Dmitry Kuzin @da_kuzin%s is teaching me how to do it. " +
                        "I'll be sure to let you know when I find out and we'll " +
                        "celebrate it together%s",
                message.getChat().getFirstName(), parseEmoji(":smirk:"), parseEmoji(":champagne:")));
        execute(sendMessage);
    }

    private void weatherCommandHandler(Message message) throws TelegramApiException {
        WeatherData data = getWeather();
        OpenWeatherMap openWeatherMap = data.getOpenWeatherMap();
        WaterLevel waterLevel = data.getWaterLevel();
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("Weather today, %s%s \n" +
                        "Country: %s\n" +
                        "City: %s\n" +
                        "Temperature: %sC\n" +
                        "Humidity: %s%%\n" +
                        "Pressure: %shPa\n" +
                        "Wind Direction: %s\n" +
                        "Wind Speed: %sm/s\n" +
                        "Water Level: %scm(%s)\n",
                message.getChat().getFirstName(),
                parseEmoji(":cloud:"),
                openWeatherMap.getSys().getCountry(),
                openWeatherMap.getName(),
                openWeatherMap.getMain().getTemp(),
                openWeatherMap.getMain().getHumidity(),
                openWeatherMap.getMain().getPressure(),
                openWeatherMap.getWind().getDeg(),
                openWeatherMap.getWind().getSpeed(),
                waterLevel.getLevel(),
                waterLevel.getDiff()
        ));
        execute(sendMessage);
    }

    private void unsupportedCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("Hello, %s, nice to meet you!%s",
                message.getChat().getFirstName(), parseEmoji(":smile:")));
        execute(sendMessage);
    }

    private String parseEmoji(String kod) {
        return EmojiParser.parseToUnicode(kod);
    }
}
