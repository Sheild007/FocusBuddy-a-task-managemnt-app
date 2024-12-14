package com.example.focusbuddy;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.textfield.TextInputEditText;

public class projectDetails extends AppCompatActivity {

    private TextView percentageText, changeText, daysLeftValue, targetValue, taskCount, startDateText, endDateText, noteTextView;
    private LinearLayout tasksContainer;
    private List<Project> projectList;
    private int projectPosition;
    private ExtendedFloatingActionButton addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        // Initialize views
        percentageText = findViewById(R.id.percentageText);
        daysLeftValue = findViewById(R.id.daysLeftValue);
        targetValue = findViewById(R.id.targetValue);
        taskCount = findViewById(R.id.taskCount);
        tasksContainer = findViewById(R.id.tasksContainer);
        startDateText = findViewById(R.id.startDateText);
        endDateText = findViewById(R.id.endDateText);
        noteTextView = findViewById(R.id.noteTextView);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Retrieve project position from intent
        projectPosition = getIntent().getIntExtra("projectPosition", -1);

        // Load project list and select the project at the given position
        projectList = loadProjects();
        if (projectPosition != -1 && projectPosition < projectList.size()) {
            Project project = projectList.get(projectPosition);

            // Bind data to views
            percentageText.setText(project.getCompletion() + "%");
            daysLeftValue.setText(calculateDaysLeft(project.getStartDate(), project.getEndDate()));
            targetValue.setText(project.getTasksPerDay() + " / day");
            taskCount.setText(project.getCompletedTasks() + " of " + project.getTasks().size());
            noteTextView.setText(project.getDescription());

            // Set project name, start date, and end date
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(project.getProjectName());
            startDateText.setText(project.getStartDate());
            endDateText.setText(project.getEndDate().equals("") ? "No Deadline" : project.getEndDate());

            // Display tasks dynamically
            for (Task task : project.getTasks()) {
                MaterialCardView taskCard = createTaskCard(task.getTaskName(), task.getCompletion());
                tasksContainer.addView(taskCard);
            }

            // Set click listeners for date pickers
            startDateText.setOnClickListener(v -> openDatePickerDialog(true));
            endDateText.setOnClickListener(v -> openDatePickerDialog(false));

            // Set click listener for noteTextView
            noteTextView.setOnClickListener(v -> openDescriptionDialog());

            // Set click listener for addTaskButton
            addTaskButton.setOnClickListener(v -> openAddTaskDialog());
        }
    }

    private void openAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        TextInputEditText editTextTaskName = dialogView.findViewById(R.id.editTextProjectName);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnOk).setOnClickListener(v -> {
            String taskName = editTextTaskName.getText().toString();
            if (!taskName.isEmpty()) {
                Task newTask = new Task(taskName, "", "", "", 0);
                projectList.get(projectPosition).addTask(newTask);
                MaterialCardView taskCard = createTaskCard(newTask.getTaskName(), newTask.getCompletion());
                tasksContainer.addView(taskCard);
                saveProjects();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openDescriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Description");

        final EditText input = new EditText(this);
        input.setText(projectList.get(projectPosition).getDescription());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newDescription = input.getText().toString();
            projectList.get(projectPosition).setDescription(newDescription);
            noteTextView.setText(newDescription);
            saveProjects();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void openDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            if (isStartDate) {
                startDateText.setText(selectedDate);
                projectList.get(projectPosition).setStartDate(selectedDate);
            } else {
                endDateText.setText(selectedDate);
                projectList.get(projectPosition).setEndDate(selectedDate);
            }
            daysLeftValue.setText(calculateDaysLeft(startDateText.getText().toString(), endDateText.getText().toString()));
            saveProjects();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private List<Project> loadProjects() {
        List<Project> projects = new ArrayList<>();
        int projectCount = getSharedPreferences("ProjectPrefs", Context.MODE_PRIVATE).getInt("ProjectCount", 0);

        for (int i = 0; i < projectCount; i++) {
            try (FileInputStream fis = openFileInput("project_" + i + ".dat");
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                projects.add((Project) ois.readObject());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load project " + i, Toast.LENGTH_SHORT).show();
            }
        }
        return projects;
    }

    private void saveProjects() {
        SharedPreferences.Editor editor = getSharedPreferences("ProjectPrefs", Context.MODE_PRIVATE).edit();
        editor.putInt("ProjectCount", projectList.size());
        editor.apply();

        for (int i = 0; i < projectList.size(); i++) {
            try (FileOutputStream fos = openFileOutput("project_" + i + ".dat", Context.MODE_PRIVATE);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(projectList.get(i));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save project " + i, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String calculateDaysLeft(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (start != null && end != null) {
                long differenceInMillis = end.getTime() - start.getTime();
                long daysLeft = differenceInMillis / (1000 * 60 * 60 * 24);
                return String.valueOf(daysLeft);
            }
        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        return "-";
    }

    private MaterialCardView createTaskCard(String taskName, int completion) {
        MaterialCardView taskCard = new MaterialCardView(this);
        taskCard.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        taskCard.setRadius(8f);

        LinearLayout taskLayout = new LinearLayout(this);
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setPadding(16, 16, 16, 16);

        TextView taskNameText = new TextView(this);
        taskNameText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        taskNameText.setText(taskName);
        taskNameText.setTextColor(getResources().getColor(R.color.white));

        TextView taskCompletionText = new TextView(this);
        taskCompletionText.setText(completion + "%");
        taskCompletionText.setTextColor(getResources().getColor(R.color.green));

        taskLayout.addView(taskNameText);
        taskLayout.addView(taskCompletionText);
        taskCard.addView(taskLayout);

        return taskCard;
    }
}