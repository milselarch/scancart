package com.example.charles_nfc;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.charles_nfc.databinding.ActivityProfileactivityBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    final UserAccount account = UserAccount.getAccount();
    private ActivityProfileactivityBinding binding;

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState
    ) {
        Log.d("FRAGMENT_CREATE", "cart");

        binding = ActivityProfileactivityBinding.inflate(
            getLayoutInflater(), container, false
        );
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadUI();
    }

    void loadUI() {
        if (binding == null) { return; }
        Activity activity = getActivity();
        if (activity == null) { return; }
        Context context = getActivity().getApplicationContext();

        firebaseAuth = FirebaseHandler.getInstanceAuth();
        fStore = FirebaseHandler.getInstanceDatabase();
        // checkUserStatus();
        if (!account.isLoggedIn()) {
            this.getActivity();
            Log.e("LOGOUT", "LOAD_UI_PROFILE");
            this.logout();
            return;
        }

        //Pressed logout button, logout user
        binding.logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("LOGOUT", "BUTTON_PRESS");
                account.logout(context);
                firebaseAuth.signOut();

                startActivity(new Intent(activity, MainActivity.class));
                checkUserStatus();
            }
        });

        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, EditProfile.class));
            }
        });
    }

    private void logout() {
        Activity activity = getActivity();
        if (activity == null) { return; }
        Context context = getActivity().getApplicationContext();

        account.logout(context);
        firebaseAuth.signOut();
        startActivity(new Intent(
            activity, MainActivity.class
        ));
    }

    private void checkUserStatus () {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String uID = firebaseAuth.getUid();
        if (uID == null) {
            this.logout();
            return;
        }

        Activity activity = getActivity();
        if (activity == null) { return; }

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
                        Intent register = new Intent(
                            activity, com.example.charles_nfc.register.class
                        );

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
        }
    }
}