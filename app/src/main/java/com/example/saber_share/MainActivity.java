package com.example.saber_share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // IDs correctos según activity_main.xml
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        BottomNavigationView bottomBar = findViewById(R.id.bottomBar);
        if (bottomBar != null && navController != null) {
            NavigationUI.setupWithNavController(bottomBar, navController);
        }

        handlePaypalIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handlePaypalIntent(intent);
    }

    private void handlePaypalIntent(Intent intent) {
        if (intent == null) return;

        Uri data = intent.getData();
        if (data != null
                && "sabersha".equals(data.getScheme())
                && "paypal-return".equals(data.getHost())) {

            if (navController != null) {
                navController.navigate(R.id.comprar);
            }
        }
    }
}