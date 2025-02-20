// CustomNotificationManager.java
package com.example.focusbuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomNotificationManager {
    private Context context;
    private AlarmManager alarmManager;

    // Constants for time thresholds
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private static final long ONE_HOUR = 60 * 60 * 1000;

    public CustomNotificationManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleNotifications(List<Project> projects) {
        List<Task> tasks = new ArrayList<>();
        for (Project project : projects) {
            tasks.addAll(project.getTasks());
        }

        // Sort tasks by deadline
        Collections.sort(tasks, Comparator.comparing(Task::getTaskDate));

        // Schedule notifications based on the algorithm
        for (Task task : tasks) {
            scheduleTaskNotifications(task);
        }
    }

    private void scheduleTaskNotifications(Task task) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date deadlineDate = dateFormat.parse(task.getTaskDate());
            if (deadlineDate == null) return;

            long deadlineTime = deadlineDate.getTime();
            long currentTime = System.currentTimeMillis();
            long timeRemaining = deadlineTime - currentTime;

            if (timeRemaining <= 0) return; // Skip past deadlines

            // Calculate notification times based on deadline proximity
            List<Long> notificationTimes = new ArrayList<>();

            if (timeRemaining > 7 * ONE_DAY) {
                // Early stage: Once every 2 days
                long interval = 2 * ONE_DAY;
                for (long time = currentTime; time < deadlineTime - 7 * ONE_DAY; time += interval) {
                    notificationTimes.add(time);
                }
            }

            if (timeRemaining > 3 * ONE_DAY) {
                // Midway stage: Once per day
                long startTime = Math.max(currentTime, deadlineTime - 7 * ONE_DAY);
                for (long time = startTime; time < deadlineTime - 3 * ONE_DAY; time += ONE_DAY) {
                    notificationTimes.add(time);
                }
            }

            if (timeRemaining > ONE_DAY) {
                // Close to deadline: Every 4 hours
                long startTime = Math.max(currentTime, deadlineTime - 3 * ONE_DAY);
                for (long time = startTime; time < deadlineTime - ONE_DAY; time += 4 * ONE_HOUR) {
                    notificationTimes.add(time);
                }
            }

            // Final day: Every hour
            long startTime = Math.max(currentTime, deadlineTime - ONE_DAY);
            for (long time = startTime; time < deadlineTime; time += ONE_HOUR) {
                notificationTimes.add(time);
            }

            // Add deadline time notification
            notificationTimes.add(deadlineTime);

            // Schedule all notifications
            for (Long time : notificationTimes) {
                scheduleNotification(task, time);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleNotification(Task task, long time) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskName", task.getTaskName());
        intent.putExtra("taskDeadline", task.getTaskDate());
        intent.putExtra("taskId", task.getId());
        
        // Use unique request code for each notification
        int requestCode = (task.getId() + String.valueOf(time)).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            requestCode, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        }
    }

    public void cancelAllNotifications(Task task) {
        // Cancel existing notifications when task is completed or deleted
        Intent intent = new Intent(context, NotificationReceiver.class);
        // Use the same extras as when scheduling
        intent.putExtra("taskId", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            task.getId().hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}