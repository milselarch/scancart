package com.example.charles_nfc;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.charles_nfc.databinding.ActivityProfileactivityBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    final UserAccount account = UserAccount.getAccount();

    private ActivityProfileactivityBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseHandler.getInstanceAuth();
        fStore = FirebaseHandler.getInstanceDatabase();
        // checkUserStatus();

        if (!account.isLoggedIn()) {
            this.logout();
            return;
        }

        //Pressed logout button, logout user
        binding.logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account.logout(getApplicationContext());
                firebaseAuth.signOut();

                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                checkUserStatus();
            }
        });

        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(
                    ProfileActivity.this, com.example.charles_nfc.EditProfile.class
                ));
            }
        });
    }

    private void logout() {
        account.logout(getApplicationContext());
        firebaseAuth.signOut();
        startActivity(new Intent(
            ProfileActivity.this, MainActivity.class
        ));
        finish();
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String uID = firebaseAuth.getUid();
        if (uID == null) {
            this.logout();
            return;
        }

        DocumentReference docRef = fStore.collection("users").document(uID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                        Intent register = new Intent(ProfileActivity.this, com.example.charles_nfc.register.class);
                        register.putExtra("UUID", uID);
                        startActivity(register);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        if (firebaseUser != null) {
            String phone = firebaseUser.getPhoneNumber();
            Log.d("PHIONE", phone);
            binding.phoneTv.setText(phone);
            //user is logged in
        } else {
            //user not logged in
            finish();
        }

    }

}