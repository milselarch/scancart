package com.example.charles_nfc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroceryList extends AppCompatActivity {
    RecyclerView recyclerView;
    GroceryAdapter groceryAdapter;
    Model.DataSource datasource;
    LinearLayoutManager llm;
    List<Model.Grocery> groceries = new ArrayList<>();
    private final FirebaseHandler firebaseManager = new FirebaseHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);
        this.loadUI();
    }

    public void loadUI() {
        Intent intent = this.getIntent();

        // disable auto dark mode across app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (intent == null) { return; }

        String order_id = intent.getStringExtra("order_id");
        FirebaseHandler.FireCallback callback = new FirebaseHandler.FireCallback() {
            @Override
            public void callback(Object result) {
                if (result instanceof Integer && (int) result == -1 ) {
                    Toast.makeText(
                            GroceryList.this,
                            "Failed to load completed orders",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                groceries = (List) result;
                onLoaded(groceries);
            }
        };
        firebaseManager.getCompletedOrder(order_id, callback);
    }

    void onLoaded(List groceries) {
        llm = new LinearLayoutManager(this);
        datasource = new Model.DataSource(groceries);

        groceryAdapter = new GroceryAdapter(this, datasource);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_holder);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(groceryAdapter);
        recyclerView.setLayoutManager(llm);
    }
}

