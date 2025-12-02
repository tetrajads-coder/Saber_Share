package com.example.saber_share;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.saber_share.util.local.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private NavController navController;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupNavigation();
    }

    @Override
    public void onClick(View view) {
        sessionManager.logoutUser();
    }
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mainContainer);

        if (navHostFragment == null) {
            navHostFragment = NavHostFragment.create(R.navigation.main_nav);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainContainer, navHostFragment)
                    .setPrimaryNavigationFragment(navHostFragment)
                    .commitNow();
        }

        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottomBar);
        NavigationUI.setupWithNavController(bottomNav, navController);

    }
}