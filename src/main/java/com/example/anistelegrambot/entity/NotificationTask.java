package com.example.anistelegrambot.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notification_task")
public class NotificationTask {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long chatId;
    private String message;
    private LocalDateTime notificationDateTime;

    public NotificationTask(){
    }

    public NotificationTask(long chatId, String message, LocalDateTime notificationDateTime) {
        this.chatId = chatId;
        this.message = message;
        this.notificationDateTime = notificationDateTime;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public long getChatId() {
        return chatId;
    }
    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public LocalDateTime getNotificationDateTime() {
        return notificationDateTime;
    }
    public void setNotificationDateTime(LocalDateTime notificationDateTime) {
        this.notificationDateTime = notificationDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTask that = (NotificationTask) o;
        return chatId == that.chatId && Objects.equals(id, that.id) && Objects.equals(message, that.message) && Objects.equals(notificationDateTime, that.notificationDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, message, notificationDateTime);
    }
}
