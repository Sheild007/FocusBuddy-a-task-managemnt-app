// NotificationReceiver.java
package com.example.focusbuddy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.startsWith("UPDATE_COMPLETION_")) {
            int completion = Integer.parseInt(action.split("_")[2]);
            String taskId = intent.getStringExtra("taskId");
            int notificationId = intent.getIntExtra("notificationId", 1);
            updateTaskCompletion(context, taskId, completion);

            // Cancel the notification using the specific notification ID
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            return;
        }

        String channelId = "focusbuddy_channel";
        String channelName = "FocusBuddy Notifications";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        String taskName = intent.getStringExtra("taskName");
        String taskDeadline = intent.getStringExtra("taskDeadline");
        String taskId = intent.getStringExtra("taskId");
        // Get the notification ID from the intent
        int notificationId = intent.getIntExtra("notificationId", 1);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews customContentView = getCustomContentView(context, taskId, notificationId);
        customContentView.setTextViewText(R.id.taskNameTextView, taskName);
        customContentView.setTextViewText(R.id.deadlineTextView, taskDeadline);

        // Load motivational quotes
        String quote = getMotivationalQuote(context, taskDeadline);
        customContentView.setTextViewText(R.id.quoteTextView, quote);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Task Reminder")
                .setContentText("Tap to view details")
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(customContentView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Use the specific notification ID when showing the notification
        notificationManager.notify(notificationId, builder.build());
    }

    private RemoteViews getCustomContentView(Context context, String taskId, int notificationId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_progress);

        // Set up button click intents with the notification ID
        remoteViews.setOnClickPendingIntent(R.id.button25, getPendingIntent(context, taskId, 25, notificationId));
        remoteViews.setOnClickPendingIntent(R.id.button50, getPendingIntent(context, taskId, 50, notificationId));
        remoteViews.setOnClickPendingIntent(R.id.button75, getPendingIntent(context, taskId, 75, notificationId));
        remoteViews.setOnClickPendingIntent(R.id.button100, getPendingIntent(context, taskId, 100, notificationId));

        return remoteViews;
    }

    private PendingIntent getPendingIntent(Context context, String taskId, int completion, int notificationId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction("UPDATE_COMPLETION_" + completion);
        intent.putExtra("taskId", taskId);
        intent.putExtra("notificationId", notificationId);
        return PendingIntent.getBroadcast(
            context, 
            (taskId + completion + notificationId).hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void updateTaskCompletion(Context context, String taskId, int completion) {
        List<Project> projects = loadProjects(context);
        for (Project project : projects) {
            for (Task task : project.getTasks()) {
                if (task.getId().equals(taskId)) {
                    task.setCompletion(completion);
                    if (completion == 100) {
                        // Cancel notifications when task is completed
                        CustomNotificationManager notificationManager = new CustomNotificationManager(context);
                        notificationManager.cancelAllNotifications(task);
                    }
                    saveProjects(context, projects);
                    return;
                }
            }
        }
    }

    private List<Project> loadProjects(Context context) {
        List<Project> projects = new ArrayList<>();
        int projectCount = context.getSharedPreferences("ProjectPrefs", Context.MODE_PRIVATE)
                .getInt("ProjectCount", 0);

        for (int i = 0; i < projectCount; i++) {
            try (FileInputStream fis = context.openFileInput("project_" + i + ".dat");
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                projects.add((Project) ois.readObject());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to load project " + i, Toast.LENGTH_SHORT).show();
            }
        }
        return projects;
    }

    private void saveProjects(Context context, List<Project> projects) {
        SharedPreferences.Editor editor = context.getSharedPreferences("ProjectPrefs", Context.MODE_PRIVATE)
                .edit();
        editor.putInt("ProjectCount", projects.size());
        editor.apply();

        for (int i = 0; i < projects.size(); i++) {
            try (FileOutputStream fos = context.openFileOutput("project_" + i + ".dat", Context.MODE_PRIVATE);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(projects.get(i));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to save project " + i, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getMotivationalQuote(Context context, String taskDeadline) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.motivational_quotes);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray quotesArray = jsonObject.getJSONArray("quotes");

            long currentTime = System.currentTimeMillis();
            long deadlineTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(taskDeadline).getTime();
            long timeRemaining = deadlineTime - currentTime;

            String quoteType;
            if (timeRemaining > 24 * 60 * 60 * 1000) {
                quoteType = "early_reminder";
            } else if (timeRemaining > 12 * 60 * 60 * 1000) {
                quoteType = "midway_reminder";
            } else if (timeRemaining > 0) {
                quoteType = "close_to_deadline";
            } else {
                quoteType = "on_deadline";
            }

            for (int i = 0; i < quotesArray.length(); i++) {
                JSONObject quoteObject = quotesArray.getJSONObject(i);
                if (quoteObject.getString("type").equals(quoteType)) {
                    JSONArray messagesArray = quoteObject.getJSONArray("messages");
                    return messagesArray.getString(new Random().nextInt(messagesArray.length()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Stay motivated!";
    }
}