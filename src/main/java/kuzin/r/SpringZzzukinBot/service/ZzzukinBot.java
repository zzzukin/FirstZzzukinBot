package kuzin.r.SpringZzzukinBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kuzin.r.SpringZzzukinBot.config.BotConfig;
import kuzin.r.SpringZzzukinBot.consts.Emoji;
import kuzin.r.SpringZzzukinBot.consts.Result;
import kuzin.r.SpringZzzukinBot.model.OpenWeatherMap;
import kuzin.r.SpringZzzukinBot.model.ResultLocation;
import kuzin.r.SpringZzzukinBot.model.WaterLevel;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.transaction.Transactional;
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
    private Date waitLocationTimer = new Date();

    public ZzzukinBot(BotConfig config) throws TelegramApiException {
        this.config = config;
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/help", "Помощь"));
        botCommands.add(new BotCommand("/start", "Спрогнозировать доброту рек"));
        botCommands.add(new BotCommand("/weather", "Узнать погоду"));
        botCommands.add(new BotCommand("/result", "Рассказать о результатах"));
        botCommands.add(new BotCommand("/about", "Узнать обо мне"));
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
        Message message = update.getMessage();

        if (update.hasMessage() && update.getMessage().hasText()) {

            switch (message.getText()) {
                case "/start":
                    startCommandHandler(message);
                    break;
                case "/help":
                    helpCommandHandler(message);
                    break;
                case "/weather":
                    weatherCommandHandler(message);
                    break;
                case "/result":
                    resultCommandHandler(message);
                    break;
                case "/about":
                    aboutCommandHandler(message);
                    break;
                default:
                    unsupportedCommandHandler(message);
            }
        }

        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackQueryData = callbackQuery.getData();
            for (Result result : Result.values()) {
                if (callbackQueryData.equals(result.name())) {
                    EditMessageText editMessageText = getEditMessageText(callbackQuery.getMessage());
                    editMessageText.setText(String.format("Твой результат сегодня - %s! " +
                                    "Спасибо тебе мой друг за ответ%s\n" +
                                    "Теперь укажи пожалуйста свое место положение.\n" +
                                    "Если не знаешь как это сделать нажми /location_help",
                            result.getText(),
                            Emoji.WINKING_FACE
                    ));

                    updateWeather();

                    WeatherData data = getWeather();
                    log.info("Timestamp for adding result author: {}", data.getTimestamp());
                    data.setResult(result.getText());
                    data.setResultAuthor(callbackQuery.getMessage().getChat().getFirstName());

                    log.info("Add result author and save data to DB");
                    saveWeather(data);

                    waitLocationTimer = new Date();

                    execute(editMessageText);
                }
            }
        }

        if (update.hasMessage() && message.getLocation() != null) {
            if((new Date()).getTime() - waitLocationTimer.getTime() < (1000 * 60 * 2)) {

                Location location = message.getLocation();
                WeatherData data = getWeather();
                log.info("Timestamp for adding result location: {}", data.getTimestamp());
                ResultLocation resultLocation = new ResultLocation(location.getLongitude(), location.getLongitude());
                data.setResultLocation(resultLocation);

                log.info("Add result location and save data to DB");
                saveWeather(data);

                SendMessage sendMessage = getSendMessage(message);
                sendMessage.setText(String.format("Спасибо, %s, я запомнил%s",
                        message.getChat().getFirstName(),
                        Emoji.WINKING_FACE
                ));

                execute(sendMessage);
            }
        }
    }

    @Scheduled(cron = "* */${bot.update.data.time} * * * *", zone = "Europe/Moscow")
    public void updateWeatherBySchedule() throws IOException {
        log.info("Update weather data {}", new Date());
        updateWeather();
    }

    @Transactional
    private void updateWeather() throws IOException {
        log.info("Get weather data from server");
        JSONObject json = weatherService.getWeather();
        log.info("Weather data: {}", json);
        ObjectMapper mapper = new ObjectMapper();
        OpenWeatherMap openWeatherMap = mapper.readValue(json.toString(), OpenWeatherMap.class);

        log.info("Get water level from server");
        WaterLevel waterLevel = waterLevelService.getWaterLevel();
        log.info("Received water level: {}({})", waterLevel.getLevel(), waterLevel.getDiff());

        WeatherData data = new WeatherData();
        data.setTimestamp(new Date().getTime());
        data.setOpenWeatherMap(openWeatherMap);
        data.setWaterLevel(waterLevel);

        OpenWeatherMap lastOpenWeatherMap = lastSavedData.getOpenWeatherMap();
        WaterLevel lastWaterLevel = lastSavedData.getWaterLevel();

        if (!openWeatherMap.equals(lastOpenWeatherMap) || !waterLevel.equals(lastWaterLevel)) {
            log.info("Save data to DB");
            saveWeather(data);
            lastSavedData = data;
        }
    }

    private void saveWeather(WeatherData data) {
        weatherRepository.save(data);

        if (weatherRepository.count() > config.getDbRecordsNum()) {
            log.info("Delete oldest data from DB");
            long timestamp = weatherRepository.findTopByOrderByTimestampAsc().getTimestamp();
            log.info("Top timestamp: {}", timestamp);
            weatherRepository.deleteByTimestamp(timestamp);
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

    private EditMessageText getEditMessageText(Message message) {
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        return editMessageText;
    }

    private void helpCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("%s, я с удовольствием поделюсь с " +
                        "тобой своими знаниями%s\n" +
                        "Используй, пожалуйста:\n\n" +
                        "/start - Спрогнозировать доброту рек\n" +
                        "/weather - Узнать погоду\n" +
                        "/result - Рассказать о результатах\n" +
                        "/about - Узнать обо мне",
                message.getChat().getFirstName(),
                Emoji.SLIGHTLY_SMILING_FACE
        ));
        execute(sendMessage);
    }

    private void startCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("Ммм... как бы это сказать то %s. " +
                        "В общем есть небольшое обстоятельство. " +
                        "На данный момент Дминтрий Кузин @da_kuzin%s " +
                        "все еще обучает меня делать предсказания. " +
                        "Но я уверен, что скоро научусь этому и обязательно " +
                        "дам тебе знать об этом. Мы обязательно еще это отметим%s%s",
                message.getChat().getFirstName(),
                Emoji.SMIRKING_FACE,
                Emoji.CLINKING_GLASSES,
                Emoji.PARTYING_FACE
        ));
        execute(sendMessage);
    }

    private void weatherCommandHandler(Message message) throws TelegramApiException {
        WeatherData data = getWeather();
        OpenWeatherMap openWeatherMap = data.getOpenWeatherMap();
        WaterLevel waterLevel = data.getWaterLevel();
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("А погода нынче такая, %s%s\n\n" +
                        "Страна: %s\n" +
                        "Город: %s\n" +
                        "Температура: %sC (ощущается %sC)\n" +
                        "Влажность: %s%%\n" +
                        "Давление: %sгПа\n" +
                        "Направление ветра: %s\n" +
                        "Скорость ветра: %sм/с\n" +
                        "Уровень воды: %sсм(%s)\n",
                message.getChat().getFirstName(),
                Emoji.SUN_BEHIND_CLOUD,
                openWeatherMap.getSys().getCountry(),
                openWeatherMap.getName(),
                openWeatherMap.getMain().getTemp(),
                openWeatherMap.getMain().getFeelsLike(),
                openWeatherMap.getMain().getHumidity(),
                openWeatherMap.getMain().getPressure(),
                openWeatherMap.getWind().getDeg(),
                openWeatherMap.getWind().getSpeed(),
                waterLevel.getLevel(),
                waterLevel.getDiff()
        ));
        execute(sendMessage);
    }

    private void resultCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("И так %s, расскажи, мне пожалуйста, " +
                        "насколько река была добра к тебе и плодородна сегодня?%s",
                message.getChat().getFirstName(),
                Emoji.THINKING_FACE
        ));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttonsRows = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();

        buttonRow.add(Result.UNSATISFACTORY.getButton());
        buttonRow.add(Result.BAD.getButton());
        buttonRow.add(Result.SATISFACTORY.getButton());
        buttonRow.add(Result.GOOD.getButton());
        buttonRow.add(Result.EXCELLENT.getButton());

        buttonsRows.add(buttonRow);
        keyboardMarkup.setKeyboard(buttonsRows);
        sendMessage.setReplyMarkup(keyboardMarkup);

        execute(sendMessage);
    }

    private void aboutCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("Привет%s, я Херишеф - древнеегипетский бог, " +
                        "покровитель Гераклеополя, бог плодородия и воды, покровитель " +
                        "охоты и рыболовства%s%s%s https://en.wikipedia.org/wiki/Heryshaf " +
                        "Кеххе, кеххе... да, так, вот. Я спал примерно 2000 лет пока " +
                        "меня не разбудил Ммм... некто Андрей Скляров%s%s Но похоже, что за " +
                        "время сна, я все еще не растерял своих навыков и могу предсказывать " +
                        "насколько реки будут плодородны и богаты рыбой. Ты можешь попросить " +
                        "меня об этом, командой /start. В обмен на свои предсказания я хочу, " +
                        "чтобы ты делился со мной своми результатами /result, насколько река была " +
                        "добра к тебе и плодородна, ок?%s Видишь там в нижнем левом углу " +
                        "синяя кнопка меню? Она поможет тебе, удачи%s\n\n" +
                        "P.S. Кстати у мнея так же есть создатель @zzzukin\n" +
                        "Для любознательных он оставил информацию о том как я устроен:\n" +
                        "github: https://github.com/zzzukin/FirstZzzukinBot\n" +
                        "А так же информацию о том, чё хранит мой разум (PostgreSQL):\n" +
                        "Host: ec2-54-228-32-29.eu-west-1.compute.amazonaws.com\n" +
                        "Database: dbb51r25o3g2dm\n" +
                        "User: ksdajtrjehfehl\n" +
                        "Port: 5432\n" +
                        "Password: 3115791733bc6525c8f7af6dae083aa7456dee4d2a6e7cc8034ab477d2dc3fdf"
                ,
                Emoji.WAVING_HAND,
                Emoji.SMILING_FACE_WITH_SUNGLASSES,
                Emoji.FISH,
                Emoji.FISHING_POLE_AND_FISH,
                Emoji.DISGUISED_FACE,
                Emoji.RAGE,
                Emoji.WINKING_FACE,
                Emoji.SLIGHTLY_SMILING_FACE
        ));
        execute(sendMessage);
    }

    private void unsupportedCommandHandler(Message message) throws TelegramApiException {
        SendMessage sendMessage = getSendMessage(message);
        sendMessage.setText(String.format("Привет, %s, %s, %s",
                message.getChat().getFirstName(),
                message.getText(),
                Emoji.UPSIDE_DOWN_FACE));
        execute(sendMessage);
    }
}
