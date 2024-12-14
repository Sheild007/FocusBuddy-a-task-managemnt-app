package com.example.focusbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private List<Project> projectList;

    public TaskAdapter(List<Task> taskList, List<Project> projectList) {
        this.taskList = taskList;
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        Project project = findProjectByTask(task);

        holder.taskTitle.setText(task.getTaskName());
        holder.taskDate.setText(task.getTaskDate());
        holder.taskProgress.setText(task.getCompletion() + "%");
        holder.taskCategory.setText(project != null ? project.getProjectName() : "Unknown Project");
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private Project findProjectByTask(Task task) {
        for (Project project : projectList) {
            if (project.getTasks().contains(task)) {
                return project;
            }
        }
        return null;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDate, taskProgress, taskCategory;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskProgress = itemView.findViewById(R.id.taskProgress);
            taskCategory = itemView.findViewById(R.id.taskCategory);
        }
    }
}