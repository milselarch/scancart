package com.example.charles_nfc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class FragmentActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Shop shop = new Shop();
    com.example.charles_nfc.Profile profile = new com.example.charles_nfc.Profile();
    private final UserAccount account = UserAccount.getAccount();
    Delivery delivery = new Delivery();
    Cart cart = new Cart();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_holder);
        Log.d("ACCOUNT_CC", String.valueOf(account.getUserID()));

        // disable auto dark mode across app
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        );

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.container, shop)
            .commit();

        bottomNavigationView.setOnItemSelectedListener(
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemID = item.getItemId();
                    return selectFragment(itemID);
                }
            }
        );

        Intent callingIntent = getIntent();
        handleCallingIntent(callingIntent);
    }

    protected boolean selectFragment(int itemID) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Log.d("USER_ID", String.valueOf(account.getUserID()));

        switch (itemID) {
            case R.id.shop:
                transaction.replace(R.id.container, shop).commit();
                bottomNavigationView.getMenu().getItem(0).setChecked(true);
                return true;
            case R.id.cart:
                transaction.replace(R.id.container, cart).commit();
                bottomNavigationView.getMenu().getItem(1).setChecked(true);
                return true;
            case R.id.delivery:
                transaction.replace(R.id.container, delivery).commit();
                bottomNavigationView.getMenu().getItem(2).setChecked(true);
                return true;
            case R.id.profile:
                transaction.replace(R.id.container, profile).commit();
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                return true;
        }

        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        shop.onNewIntent(intent);
    }

    protected void handleCallingIntent(Intent callingIntent) {
        if (callingIntent == null) { return; }
        int fragmentID = callingIntent.getIntExtra(
            "fragment_id", -1
        );

        boolean result = this.selectFragment(fragmentID);
        Log.d("RESTORE_FRAGMENT", String.valueOf(result));
        Log.d("RESTORE_FRAGMENT", String.valueOf(fragmentID));
    }
}