package com.example.anistelegrambot.listener;

import com.example.anistelegrambot.entity.NotificationTask;
import com.example.anistelegrambot.repository.NotificationRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
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
    private final Logger logger = LoggerFactory.getLogger(com.example.anistelegrambot.listener.TelegramBotUpdatesListener.class);
    private static final String START_CMD = "/start";
    private static final String GREETING_TEXT = "Hello! It is a Telegram bot.";
    private static final String REGEX_BOT_MESSAGE = "([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)";
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    @Autowired
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
                    logger.info("Processing update: {}", update);
                    String messageText = update.message().text();
                    long chatId = update.message().chat().id();
                    if (START_CMD.equals(messageText)) {
                        logger.info(START_CMD + "command has been received");
                        sendMessage(chatId, GREETING_TEXT);
                    } else {
                        Pattern pattern = Pattern.compile(REGEX_BOT_MESSAGE);
                        Matcher matcher = pattern.matcher(messageText);
                        if (matcher.matches()) {
                            String date = matcher.group(1);
                            String message = matcher.group(3);
                            LocalDateTime localDateTime = LocalDateTime.parse(date, DATE_TIME_FORMATTER);
                            logger.info("Notification task message (date: {}, message: {})",localDateTime,message);
                            notificationRepository.save(new NotificationTask(chatId,message,localDateTime));
                        }
                    }
                }
        );
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationTasks() {
        Collection<NotificationTask> notificationTasks = notificationRepository.
                findAllTasksByDateTime(LocalDateTime.now().truncatedTo(
                        ChronoUnit.MINUTES));
        notificationTasks.forEach(task -> sendMessage(task.getChatId(), task.getMessage()));
    }
    private void sendMessage(Long chatId, String messageText) {
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        telegramBot.execute(sendMessage);
    }
}
