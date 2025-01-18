// CustomNotificationManager.java
package com.example.focusbuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

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
        // Calculate time intervals and schedule notifications
        // This is a simplified version of the algorithm
        long currentTime = System.currentTimeMillis();
        if(task.getTaskDate().equals("")) {
            return;
        }
        //task date is string, convert it to long 2025-01-14 00:00
        long deadlineTime = 0;
        try
        {
            // Define the date format that matches the input string
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            // Parse the task date string into a Date object
            Date parsedDate = dateFormat.parse(task.getTaskDate());
            // Convert the Date object into a long timestamp
            deadlineTime = parsedDate.getTime();


        } 
        catch (Exception e) {
            // Handle parsing exceptions
            e.printStackTrace();
        }
        long timeRemaining = deadlineTime - currentTime;

        if (timeRemaining > 0) {
            // Schedule notifications based on priority and time remaining
            int priority = task.getPriority();
            long interval = calculateInterval(priority, timeRemaining);

            for (long time = currentTime; time < deadlineTime; time += interval) {
                scheduleNotification(task, time);
            }
        }
    }

    private long calculateInterval(int priority, long timeRemaining) {
        // Calculate interval based on priority and time remaining
        // Higher priority tasks get more frequent notifications
        long baseInterval = 24 * 60 * 60 * 1000; // 1 day in milliseconds
        return baseInterval / priority;
    }

    private void scheduleNotification(Task task, long time) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskName", task.getTaskName());
        intent.putExtra("taskDeadline", task.getTaskDate());
        intent.putExtra("taskId", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getId().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }
}