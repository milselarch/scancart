package com.example.charles_nfc;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.charles_nfc.databinding.ActivityProfileactivityBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class Profile extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    final UserAccount account = UserAccount.getAccount();
    private ActivityProfileactivityBinding binding;
    private final FirebaseHandler firebaseManager = new FirebaseHandler();

    private TextView nameView;
    private TextView phoneView;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart() {
        super.onStart();
        try {
            loadUI();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            loadUI();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void loadUI() throws InterruptedException {
        if (binding == null) { return; }
        Activity activity = getActivity();
        if (activity == null) { return; }
        Context context = getActivity().getApplicationContext();

        String UUID = FirebaseHandler.getUID();
        if (UUID == null) { return; }

        nameView = activity.findViewById(R.id.name);
        phoneView = activity.findViewById(R.id.phoneTv);

        CompletableFuture<DocumentSnapshot> promise = firebaseManager.loadUserInfo(UUID);
        promise.thenAccept(user -> {
            if (!user.exists()) { return; }
            String name = user.getData().get("userName").toString();
            String phoneNumber = user.getData().get("userPhoneNumber").toString();

            nameView.setText(name);
            phoneView.setText(String.format(
                "(%s)", phoneNumber
            ));
        });

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