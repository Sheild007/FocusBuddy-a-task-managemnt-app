package com.example.focusbuddy;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;
    private List<Project> projectList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Load projects
        projectList = loadProjects();

        // Schedule notifications
        CustomNotificationManager  notificationManager = new CustomNotificationManager(this);
        notificationManager.scheduleNotifications(projectList);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not signed in, launch the Login activity
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Example usage

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the title to empty and ensure it remains hidden
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // Disable title display
            actionBar.setTitle(""); // Set title to an empty string
        }

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navAbout) {
                    Toast.makeText(getApplicationContext(), "About", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navSettings) {
                    Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tab_Layout);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Projects");
                    break;
                case 1:
                    tab.setText("All Tasks");
                    break;
                case 2:
                    tab.setText("Today");
                    break;
            }
        }).attach();


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Add a method to handle sign out
    private void signOut() {
        mAuth.signOut();
        startActivity(new Intent(this, Login.class));
        finish();
    }



    // In your activity or a utility class
    public void scheduleNotification(Context context, long timeInMillis) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
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


}