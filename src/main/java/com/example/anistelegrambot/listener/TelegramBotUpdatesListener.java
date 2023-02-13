package com.example.anistelegrambot.listener;
import com.example.anistelegrambot.entity.NotificationTask;
import com.example.anistelegrambot.repository.NotificationRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
    public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger LOGGER = LoggerFactory.getLogger(com.example.anistelegrambot.listener.TelegramBotUpdatesListener.class);
    private static final String START_CMD = "/start";
    private static final String GREETING_TEXT = "Hello! It is a Telegram bot.";
    private static final String OK_TEXT = "Job scheduled !";
    private static final Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;
    private NotificationRepository notificationRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationRepository notificationRepository) {
        this.telegramBot = telegramBot;
        this.notificationRepository = notificationRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
                    LOGGER.info("Processing update: {}", update);
                    String messageText = update.message().text();
                    long chatId = update.message().chat().id();
                    if (START_CMD.equals(messageText)) {
                        LOGGER.info(START_CMD + "command has been received");
                        sendMessage(chatId, GREETING_TEXT);
                    } else {
                        Matcher matcher = PATTERN.matcher(messageText);
                        if (matcher.matches()) {
                            String dateTime = matcher.group(1);
                            String message = matcher.group(3);
                            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
                            LOGGER.info("Notification task message (date: {}, message: {})",localDateTime,message);
                            sendMessage(chatId,OK_TEXT);
                            notificationRepository.save(new NotificationTask(chatId,message,localDateTime));
                        }
                    }
                }
        );
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *") //fixedRate=1,timeUnit=TimeUnit.MINUTES
    public void sendNotificationTasks() {
        Collection<NotificationTask> notificationTasks = notificationRepository.
                findAllTasksByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        notificationTasks.forEach(task -> sendMessage(task.getChatId(), task.getMessage()));
    }
    private void sendMessage(Long chatId, String messageText) {
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()){
           LOGGER.error("Message was not sent: {}, error code: {}", sendMessage,sendResponse.errorCode());
        }
    }
}
