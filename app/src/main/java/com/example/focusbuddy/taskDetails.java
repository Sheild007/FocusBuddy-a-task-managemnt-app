package com.example.focusbuddy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class taskDetails extends AppCompatActivity {

    private List<Project> projectList;
    private int projectPosition, taskPosition;
    private Task currentTask;

    // UI Components
    private TextView taskNameTextView;
    private CircularProgressIndicator progressIndicator;
    private TextView progressTextView;
    private Slider completionSlider;
    private TextView deadlineTextView;
    private TextInputEditText descriptionEditText;
    private ImageView editButton;
    private ImageView menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        initializeViews();
        setupToolbar();
        loadTaskData();
        setupClickListeners();
        setupTextWatcher();
    }

    private void initializeViews() {
        taskNameTextView = findViewById(R.id.taskNameTextView);
        progressIndicator = findViewById(R.id.progressIndicator);
        progressTextView = findViewById(R.id.progressTextView);
        completionSlider = findViewById(R.id.completionSlider);
        deadlineTextView = findViewById(R.id.deadlineTextView);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        editButton = findViewById(R.id.editButton);
        menuButton = findViewById(R.id.menuButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void loadTaskData() {
        projectPosition = getIntent().getIntExtra("projectPosition", -1);
        taskPosition = getIntent().getIntExtra("taskPosition", -1);

        projectList = loadProjects();
        if (projectPosition != -1 && projectPosition < projectList.size()) {
            Project project = projectList.get(projectPosition);
            if (taskPosition != -1 && taskPosition < project.getTasks().size()) {
                currentTask = project.getTasks().get(taskPosition);
                updateUI();
            }
        }
    }

    private void updateUI() {
        if (currentTask != null) {
            taskNameTextView.setText(currentTask.getTaskName());

            // Update progress
            int progress = currentTask.getCompletion();
            progressIndicator.setProgress(progress);
            progressTextView.setText(progress + "%");
            completionSlider.setValue(progress);

            // Update deadline
            String deadline = currentTask.getTaskDate();
            if (deadline != null && !deadline.isEmpty()) {
                deadlineTextView.setText(deadline);
            } else {
                deadlineTextView.setText("No Deadline");
            }

            // Update description
            descriptionEditText.setText(currentTask.getTaskDescription());
        }
    }

    private void setupClickListeners() {
        editButton.setOnClickListener(v -> {
            // Enable description editing
            descriptionEditText.setEnabled(true);
            descriptionEditText.requestFocus();
        });

        menuButton.setOnClickListener(v -> {
            // Show menu options
            showMenuOptions();
        });

        completionSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                int progress = (int) value;
                currentTask.setCompletion(progress);
                progressIndicator.setProgress(progress);
                progressTextView.setText(progress + "%");
                saveProjects();
            }
        });

        deadlineTextView.setOnClickListener(v -> showDateTimePicker());
    }

    private void setupTextWatcher() {
        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (currentTask != null) {
                    currentTask.setTaskDescription(s.toString());
                    saveProjects();
                }
            }
        });
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            currentDate.set(Calendar.YEAR, year);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(taskDetails.this, (view1, hourOfDay, minute) -> {
                currentDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                currentDate.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String formattedDate = sdf.format(currentDate.getTime());
                currentTask.setTaskDate(formattedDate);
                deadlineTextView.setText(formattedDate);
                saveProjects();
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showMenuOptions() {
        // Implement menu options dialog/popup
        Toast.makeText(this, "Menu options", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Project> loadProjects() {
        List<Project> projects = new ArrayList<>();
        int projectCount = getSharedPreferences("ProjectPrefs", Context.MODE_PRIVATE)
                .getInt("ProjectCount", 0);

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
        SharedPreferences.Editor editor = getSharedPreferences("ProjectPrefs", Context.MODE_PRIVATE)
                .edit();
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

    @Override
    protected void onPause() {
        super.onPause();
        saveProjects();
    }
}