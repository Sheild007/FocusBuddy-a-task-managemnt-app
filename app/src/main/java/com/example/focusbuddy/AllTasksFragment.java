package com.example.focusbuddy;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class AllTasksFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Project> projectList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_all_tasks, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        projectList = loadProjects();
        taskList = loadAllTasks();
        taskAdapter = new TaskAdapter(taskList, projectList);
        recyclerView.setAdapter(taskAdapter);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        taskList = loadAllTasks();
        taskAdapter.updateTasks(taskList);
        taskAdapter.notifyDataSetChanged();
    }


    private List<Task> loadAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        for (Project project : projectList) {
            allTasks.addAll(project.getTasks());
        }
        return allTasks;
    }

    private List<Project> loadProjects() {
        List<Project> projects = new ArrayList<>();
        int projectCount = requireContext().getSharedPreferences("ProjectPrefs", Context.MODE_PRIVATE).getInt("ProjectCount", 0);

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