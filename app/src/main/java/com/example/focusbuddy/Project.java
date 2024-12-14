// Project.java

package com.example.focusbuddy;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Project implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String projectName;
    private String description;
    private String startDate; // Starting deadline
    private String endDate;   // Ending deadline
    private int completion;   // Completion percentage (0-100)
    private int completedTasks; // Number of tasks completed
    private int tasksPerDay;    // Number of tasks per day count
    private List<Task> tasks;   // Multiple tasks in the project

    // Constructor
    public Project(String projectName, String description, String endDate, int tasksPerDay) {
        this.id = generateId();
        this.projectName = projectName;
        this.description = description;
        this.startDate = getCurrentDate(); // Set start date to current date
        this.endDate = endDate;
        this.completion = 0; // Default completion is 0%
        this.completedTasks = 0;
        this.tasksPerDay = tasksPerDay;
        this.tasks = new ArrayList<>(); // Initialize dynamic task list
    }

    // Generate unique ID using timestamp and random number
    private String generateId() {
        long timestamp = System.currentTimeMillis();
        int randomNum = new Random().nextInt(1000);
        return timestamp + "_" + randomNum;
    }

    // Get current date in the desired format
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getCompletion() {
        return completion;
    }

    public void setCompletion(int completion) {
        if (completion < 0 || completion > 100) {
            throw new IllegalArgumentException("Completion must be between 0 and 100");
        }
        this.completion = completion;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
        updateCompletion();
    }

    public int getTasksPerDay() {
        return tasksPerDay;
    }

    public void setTasksPerDay(int tasksPerDay) {
        this.tasksPerDay = tasksPerDay;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    // Add a task to the project
    public void addTask(Task task) {
        tasks.add(task);
        updateCompletion();
    }

    // Remove a task from the project
    public void removeTask(Task task) {
        tasks.remove(task);
        updateCompletion();
    }

    // Update the project's completion percentage based on tasks
    private void updateCompletion() {
        if (tasks.isEmpty()) {
            this.completion = 0;
            return;
        }

        int totalCompletion = 0;
        for (Task task : tasks) {
            totalCompletion += task.getCompletion();
        }

        // Calculate average completion percentage
        this.completion = totalCompletion / tasks.size();
    }

    // toString() for debugging
    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", projectName='" + projectName + '\'' +
                ", description='" + description + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", completion=" + completion +
                ", completedTasks=" + completedTasks +
                ", tasksPerDay=" + tasksPerDay +
                ", tasks=" + tasks +
                '}';
    }
}