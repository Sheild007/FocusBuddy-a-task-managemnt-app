// Task.java

package com.example.focusbuddy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String taskName;
    private String taskDescription;
    private String taskTime;
    private String taskDate;
    private int completion; // Completion percentage (0-100)
    private List<Task> subTasks; // Dynamic array for subtasks

    // Constructor
    public Task(String taskName, String taskDescription, String taskTime, String taskDate, int completion) {
        this.id = generateId();
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskTime = taskTime;
        this.taskDate = taskDate;
        this.completion = completion;
        this.subTasks = new ArrayList<>(); // Initialize the dynamic array
    }

    // Generate unique ID using timestamp and random number
    private String generateId() {
        long timestamp = System.currentTimeMillis();
        int randomNum = new Random().nextInt(1000);
        return timestamp + "_" + randomNum;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
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

    public List<Task> getSubTasks() {
        return subTasks;
    }

    // Methods to manage subtasks
    public void addSubTask(Task subTask) {
        subTasks.add(subTask);
    }

    public void removeSubTask(Task subTask) {
        subTasks.remove(subTask);
    }

    public Task getSubTask(int index) {
        return subTasks.get(index);
    }

    // toString() for easy debugging
    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskDescription='" + taskDescription + '\'' +
                ", taskTime='" + taskTime + '\'' +
                ", taskDate='" + taskDate + '\'' +
                ", completion=" + completion +
                ", subTasks=" + subTasks +
                '}';
    }
}