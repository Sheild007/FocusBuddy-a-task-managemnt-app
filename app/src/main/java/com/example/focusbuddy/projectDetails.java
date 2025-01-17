package com.example.focusbuddy;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class projectDetails extends AppCompatActivity {

    private TextView percentageText, daysLeftValue, targetValue, taskCount, startDateText, endDateText, noteTextView;
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


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Retrieve project position from intent
        projectPosition = getIntent().getIntExtra("projectPosition", -1);

        // Load project list and select the project at the given position
        projectList = loadProjects();
        if (projectPosition != -1 && projectPosition < projectList.size()) {
            Project project = projectList.get(projectPosition);
            updateUI(project);

            // Set click listeners for date pickers
            startDateText.setOnClickListener(v -> openDatePickerDialog(true));
            endDateText.setOnClickListener(v -> openDatePickerDialog(false));

            // Set click listener for noteTextView
            noteTextView.setOnClickListener(v -> openDescriptionDialog());

            // Set click listener for addTaskButton
            addTaskButton.setOnClickListener(v -> openAddTaskDialog());

            // Set click listener for menuButton
            ImageView menuButton = findViewById(R.id.menuButton);
            menuButton.setOnClickListener(v -> showPopupMenu(v));
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showPopupMenu(View view) {
        // Create a ContextThemeWrapper with the custom style
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.CustomPopupMenu);

        // Create the PopupMenu with the custom style
        PopupMenu popup = new PopupMenu(contextThemeWrapper, view);
        popup.getMenuInflater().inflate(R.menu.menu_project_details, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_name) {
                // Handle edit name action
                openEditNameDialog();
                return true;
            } else if (item.getItemId() == R.id.action_delete_project) {
                // Handle delete project action
                deleteProject();
                return true;
            } else {
                return false;
            }
        });

        // Show the PopupMenu
        popup.show();
    }


    private void openEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Project Name");

        final EditText input = new EditText(this);
        input.setText(projectList.get(projectPosition).getProjectName());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString();
            projectList.get(projectPosition).setProjectName(newName);
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(newName);
            saveProjects();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteProject() {
        projectList.remove(projectPosition);
        saveProjects();
        finish(); // Close the activity
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
                updateUI(projectList.get(projectPosition));
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_add_description_dialog, null);
        TextInputEditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);

        // Set the current description in the EditText
        editTextDescription.setText(projectList.get(projectPosition).getDescription());

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String newDescription = editTextDescription.getText().toString();
            projectList.get(projectPosition).setDescription(newDescription);
            noteTextView.setText(newDescription);
            saveProjects();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
            updateUI(projectList.get(projectPosition));
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

        projectList.get(projectPosition).setCompletion(calculateAverageCompletion(projectList.get(projectPosition)));
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
           Date current = new Date();
           Date end = sdf.parse(endDate);
           if (end != null) {
               long differenceInMillis = end.getTime() - current.getTime();
               long daysLeft = differenceInMillis / (1000 * 60 * 60 * 24);

               if (daysLeft > 0) {
                   return daysLeft + " days left";
               } else if (daysLeft == 0) {
                   return "Deadline is today!";
               } else {
                   return "Deadline already passed";
               }
           }
       } catch (Exception e) {
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

        // Add click listener to the card
        taskCard.setOnClickListener(v -> {
            int taskIndex = tasksContainer.indexOfChild(taskCard);
            if (taskIndex != -1) {
                Intent intent = new Intent(this, taskDetails.class);
                intent.putExtra("projectPosition", projectPosition);
                intent.putExtra("taskPosition", taskIndex);
                startActivity(intent);
            }
        });

        return taskCard;
    }

    private void updateUI(Project project) {
        // Bind data to views
        percentageText.setText(calculateAverageCompletion(project) + "%");
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
        tasksContainer.removeAllViews();
        for (Task task : project.getTasks()) {
            MaterialCardView taskCard = createTaskCard(task.getTaskName(), task.getCompletion());
            tasksContainer.addView(taskCard);
        }
    }

    private int calculateAverageCompletion(Project project) {
        List<Task> tasks = project.getTasks();
        if (tasks.isEmpty()) {
            return 0;
        }
        int totalCompletion = 0;
        for (Task task : tasks) {
            totalCompletion += task.getCompletion();
        }
        return totalCompletion / tasks.size();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (projectPosition != -1 && projectPosition < projectList.size()) {
            projectList = loadProjects();
            Project project = projectList.get(projectPosition);
            updateUI(project);
        }
    }
}