package com.example.yourscancart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Shop shop = new Shop();
    Profile profile = new Profile();
    Delivery delivery = new Delivery();
    Cart cart = new Cart();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // disable auto dark mode across app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, shop).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.shop:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, shop).commit();
                        return true;
                    case R.id.cart:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, cart).commit();
                        return true;
                    case R.id.delivery:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, delivery).commit();
                        return true;
                    case R.id.profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, profile).commit();
                        return true;
                }
                return false;
            }
        });
    }
}