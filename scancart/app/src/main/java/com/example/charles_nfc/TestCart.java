package com.example.charles_nfc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class TestCart extends AppCompatActivity {
    private final Integer userID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_cart);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cart = db.collection("shopping_cart");
        Query query = cart.whereEqualTo("user_id", userID);
        StringBuilder cartInfo = new StringBuilder();
        TextView cartTextView = findViewById(R.id.test_cart_info);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();
                    Map<String, Object> documentData = document.getData();
                    cartInfo.append(documentId);
                    cartInfo.append(" = ");
                    cartInfo.append(documentData.toString());
                    cartInfo.append('\n');
                }

                cartTextView.setText(cartInfo.toString());
            }
        });
    }
}