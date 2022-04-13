package com.example.charles_nfc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);
        db = FirebaseFirestore.getInstance();
        Intent intent = this.getIntent();

        if (intent != null) {
            String order_id = intent.getStringExtra("order_id");
            CollectionReference orders_completed = db.collection("completed_orders");
            //DocumentReference documentReference = orders_completed.document("order_id");
            List<Model.Grocery> groceries = new ArrayList<>();
            Query query = orders_completed.whereEqualTo("order_id", order_id);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.d("ITEM_STATUS", String.valueOf(task.isSuccessful()));
                    if (!task.isSuccessful()) {
                        Toast.makeText(
                            GroceryList.this,
                            "Failed to load completed orders",
                            Toast.LENGTH_SHORT
                        ).show();
                    };

                    QuerySnapshot snapshot = task.getResult();
                    Log.d("SNAPSHOT", snapshot.toString());

                    for (DocumentSnapshot document: task.getResult()) {
                        List<Map> list = (List<Map>) document.get("items");
                        Log.d("LIST_VALUES", list.toString());
                        Log.d("DOC_DATA", document.toString());

                        for (Map item : list) {
                            Log.d("ADD_ITEM", item.toString());
                            String cost = String.valueOf(item.get("cost"));
                            String image_url = (String) item.get("image_url");
                            String name = (String) item.get("name");
                            String quantity = String.valueOf(item.get("quantity"));
                            String tag_id = (String) item.get("tag_id");

                            Log.d("TAG", String.valueOf(item));
                            groceries.add(new Model.Grocery(
                                cost,image_url,name,quantity,tag_id
                            ));
                        }
                    }

                    datasource = new Model.DataSource(groceries);
                    recyclerView = findViewById(R.id.recyclerview);
                    groceryAdapter = new GroceryAdapter(
                        GroceryList.this, datasource
                    );

                    recyclerView.setAdapter(groceryAdapter);
                    recyclerView.setLayoutManager(
                        new LinearLayoutManager(GroceryList.this)
                    );
                }
            });
        }
    }
}

