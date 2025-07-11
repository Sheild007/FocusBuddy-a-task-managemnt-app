// ProjectAdapter.java

package com.example.focusbuddy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private Context context;
    private List<Project> projectList;

    public ProjectAdapter(Context context, List<Project> projectList) {
        this.context = context;
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.projectTitle.setText(project.getProjectName());
        holder.projectDate.setText(project.getStartDate() + " - " + project.getEndDate());
        holder.projectProgress.setText(project.getCompletion() + "%");
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, projectDetails.class);
            intent.putExtra("projectPosition", position); // Pass the project position
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView projectTitle, projectDate, projectProgress;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectTitle = itemView.findViewById(R.id.projectTitle);
            projectDate = itemView.findViewById(R.id.projectDate);
            projectProgress = itemView.findViewById(R.id.projectProgress);
        }
    }
}