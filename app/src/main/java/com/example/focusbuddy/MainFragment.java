// MainFragment.java

package com.example.focusbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ProjectPrefs";
    private static final String PROJECT_COUNT_KEY = "ProjectCount";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_sec, container, false); // Inflates `activity_sec.xml`
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        projectList = loadProjects();
        projectAdapter = new ProjectAdapter(projectList);
        recyclerView.setAdapter(projectAdapter);

        v.findViewById(R.id.fabNewProject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddProjectDialog();
            }
        });

        return v;
    }

    private void showAddProjectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_project, null);
        TextInputEditText editTextProjectName = dialogView.findViewById(R.id.editTextProjectName);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String projectName = editTextProjectName.getText().toString();
                if (!projectName.isEmpty()) {
                    Project project = new Project(projectName, "", "", 0);
                    projectList.add(project);
                    saveProjects();
                    projectAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Project name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // MainFragment.java

    private void saveProjects() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PROJECT_COUNT_KEY, projectList.size());
        editor.apply();

        for (int i = 0; i < projectList.size(); i++) {
            try (FileOutputStream fos = requireContext().openFileOutput("project_" + i + ".dat", Context.MODE_PRIVATE);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(projectList.get(i));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to save project " + i, Toast.LENGTH_SHORT).show();
            }
        }
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

    private void clearProjectsFile() {
        try (FileOutputStream fos = requireContext().openFileOutput("projects.dat", Context.MODE_PRIVATE)) {
            // Opening the file in MODE_PRIVATE will clear its contents
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to clear projects file", Toast.LENGTH_SHORT).show();
        }
    }
}