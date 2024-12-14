package com.example.focusbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TodayTaskAdapter extends RecyclerView.Adapter<TodayTaskAdapter.TodayTaskViewHolder> {
    private List<Task> todayTaskList;

    public TodayTaskAdapter(List<Task> todayTaskList) {
        this.todayTaskList = todayTaskList;
    }

    @NonNull
    @Override
    public TodayTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TodayTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayTaskViewHolder holder, int position) {
        Task task = todayTaskList.get(position);
        holder.taskTitle.setText(task.getTaskName());
        holder.taskDate.setText(task.getTaskDate());
        holder.taskProgress.setText(task.getCompletion() + "%");
    }

    @Override
    public int getItemCount() {
        return todayTaskList.size();
    }

    public static class TodayTaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDate, taskProgress;

        public TodayTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskProgress = itemView.findViewById(R.id.taskProgress);
        }
    }
}