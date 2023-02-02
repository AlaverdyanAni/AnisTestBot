package com.example.anistelegrambot.repository;

import com.example.anistelegrambot.entity.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Collection;

public interface NotificationRepository extends JpaRepository<NotificationTask,Long> {
    @Query(value = "SELECT * FROM notification_task WHERE notification_date_time = :date_time", nativeQuery = true)
   Collection<NotificationTask> findAllTasksByDateTime(@Param("date_time") LocalDateTime date_time);
}

