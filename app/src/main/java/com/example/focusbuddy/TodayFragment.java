package com.example.focusbuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayFragment extends Fragment {
    private TextView currentDateTextView, progressText, todayTaskCount;
    private CircularProgressIndicator progressIndicator;
    private RecyclerView recyclerView;
    private TodayTaskAdapter todayTaskAdapter;
    private List<Task> todayTasks;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProjectPrefs";
    private static final String PROJECT_COUNT_KEY = "ProjectCount";

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_today, container, false);

        currentDateTextView = view.findViewById(R.id.currentDate);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        progressText = view.findViewById(R.id.progressText);
        todayTaskCount = view.findViewById(R.id.todayTaskCount);
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setCurrentDate();
        loadTodayTasks();
        updateProgress();

        todayTaskAdapter = new TodayTaskAdapter(todayTasks);
        recyclerView.setAdapter(todayTaskAdapter);

        return view;
    }

    private void setCurrentDate() {
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        currentDateTextView.setText(currentDate);
    }


    private void loadTodayTasks() {
        try {
            todayTasks = new ArrayList<>();
            List<Project> projects = loadProjects();
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            for (Project project : projects) {
                for (Task task : project.getTasks()) {
                    String taskDate = task.getTaskDate();

                    // Ensure taskDate is checked and substring only applied if todayDate is not empty
                    if (!todayDate.isEmpty() && taskDate.length() >= 10) {
                        taskDate = taskDate.substring(0, 10);
                    }

                    if (taskDate.equals(todayDate)) {
                        todayTasks.add(task);
                    }
                }
            }

            todayTaskCount.setText(todayTasks.size() + " of " + projects.size());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to load today's tasks", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgress() {
        if (todayTasks.isEmpty()) {
            progressIndicator.setProgress(0);
            progressText.setText("0%");
            return;
        }

        int totalCompletion = 0;
        for (Task task : todayTasks) {
            totalCompletion += task.getCompletion();
        }

        int averageCompletion = totalCompletion / todayTasks.size();
        progressIndicator.setProgress(averageCompletion);
        progressText.setText(averageCompletion + "%");
    }

    private List<Project> loadProjects() {
        List<Project> projects = new ArrayList<>();
        int projectCount = sharedPreferences.getInt(PROJECT_COUNT_KEY, 0);

        for (int i = 0; i < projectCount; i++) {
            try (FileInputStream fis = requireContext().openFileInput("project_" + i + ".dat");
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                projects.add((Project) ois.readObject());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to load project " + i, Toast.LENGTH_SHORT).show();
            }
        }
        return projects;
    }
}