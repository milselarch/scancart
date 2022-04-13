package com.example.charles_nfc;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FirebaseHandler {
    protected static FirebaseFirestore fStore;
    protected static FirebaseAuth fAuth;
    protected FirebaseHandler() {

    };

    public static FirebaseFirestore getInstanceDatabase(){
        if (fStore == null){
            fStore = FirebaseFirestore.getInstance();
        }
        return fStore;
    }
    public static FirebaseAuth getInstanceAuth(){
        if (fAuth == null){
            fAuth = FirebaseAuth.getInstance();
        }
        return fAuth;
    }

    public static String getUID(){
        return fAuth.getUid();
    }

    public static String getPhone() {
        return fAuth.getCurrentUser().getPhoneNumber();
    }

    class User{
        String userUID;
        String userName;
        String userPhoneNumber;
        String userStreetAddress;
        String userPostalCode;
        String userFloorAndUnit;
        String userHealthConditions;
        Long user_id;

        public User(){}

        public User(
            String UUID, String Name, String phoneNumber, String streetAddress, String postalCode,
            String floorAndUnit, String healthConditions, Long userID
        ) {
            this.userUID = UUID;
            this.userName = Name;
            this.userPhoneNumber = phoneNumber;
            this.userStreetAddress = streetAddress;
            this.userPostalCode = postalCode;
            this.userFloorAndUnit = floorAndUnit;
            this.userHealthConditions = healthConditions;
            this.user_id = userID;
        }

        public Long getUser_id() {return this.user_id; }
        public String getUserUID(){
            return this.userUID;
        }
        void setUserUID(String userUID){
            this.userUID = userUID;
        }
        public String getUserName(){
            return this.userName;
        }
        void setUserName(String Name){
            this.userName = Name;
        }
        public String getUserPhoneNumber(){
            return this.userPhoneNumber;
        }
        void setUserPhoneNumber(String phoneNumber){
            this.userPhoneNumber = phoneNumber;
        }
        public String getUserStreetAddress(){
            return this.userStreetAddress;
        }
        void setUserStreetAddress(String streetAddress){this.userStreetAddress = streetAddress;}
        public String getUserPostalCode(){return this.userPostalCode;}
        void setUserPostalCode(String postalCode){this.userPostalCode = postalCode;}
        public String getUserFloorAndUnit(){return this.userFloorAndUnit;}
        void setUserFloorAndUnit(String floorAndUnit){this.userFloorAndUnit = floorAndUnit;}
        public String getUserHealthConditions(){return this.userHealthConditions;}
        void setUserHealthConditions(String healthConditions){this.userHealthConditions = healthConditions;}
    }

    public static void registerUser(
        User user, FirebaseFirestore fStore, FireCallback onRegisterComplete
    ) {
        Log.d(TAG, "" + user.getUserUID());
        DocumentReference documentReference = fStore.collection("users").document(user.getUserUID());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User Created");
                onRegisterComplete.callback(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("EDIT_USER_FAIL", e.toString());
                onRegisterComplete.callback(false);
            }
        });
    }

    public static void editUser(
        User user, FirebaseFirestore fStore, FireCallback onEditComplete
    ) {
        Log.d("EDIT_USER", "" + user.getUserUID());
        DocumentReference documentReference = fStore.collection("users").document(user.getUserUID());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User Edited");
                onEditComplete.callback(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("EDIT_USER_FAIL", e.toString());
                onEditComplete.callback(false);
            }
        });
    }

    void loadShoppingCart(Integer userID) {
        FirebaseFirestore db = getInstanceDatabase();

        Consumer<ArrayList<Map<String, Object>>> onDocumentsLoad = (
            ArrayList<Map<String, Object>> documents
        ) -> {
            ArrayList<Promise<
                Map<String, Object>, ShoppingCartItemModel, ?
            >> promises = new ArrayList<>();


            for (Map<String, Object> doc: documents) {
                String tag_id = (String) doc.get("tag_id");
                Promise<
                    Map<String, Object>, ShoppingCartItemModel, ?
                    > subpromise = new Promise<
                    Map<String, Object>, ShoppingCartItemModel, Object
                >() {
                    @Override
                    void onPromiseReady(
                        Map<String, Object> result,
                        Resolver<ShoppingCartItemModel> resolver
                    ) {

                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                };
            }
        };

        Promise <
            ArrayList<Map<String, Object>>,
            ArrayList<Promise<Map<String, Object>, ShoppingCartItemModel, ?>>,
            Void
        > promise = new Promise<
            ArrayList<Map<String, Object>>,
            ArrayList<Promise<Map<String, Object>, ShoppingCartItemModel, ?>>,
            Void
        > (true) {
            @Override
            void onPromiseReady(
                ArrayList<Map<String, Object>> result,
                Resolver<
                    ArrayList<Promise<
                    Map<String, Object>,
                    ShoppingCartItemModel, ?
                    >
                >> resolver
            ) {
                ArrayList<Map<String, Object>> documents = new ArrayList<>();
                EventListener<QuerySnapshot> listener = new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                        @Nullable QuerySnapshot value,
                        @Nullable FirebaseFirestoreException error
                    ) {
                        if (error != null) {
                            resolver.reject(error);
                            return;
                        } else if (value == null) {
                            resolver.reject(new NullPointerException());
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            documents.add(doc.getData());
                        }
                    }
                };

                db.collection("shopping_cart")
                    .whereEqualTo("user_id", userID)
                    .addSnapshotListener(listener);
            }

            @Override
            public void onError(Throwable error) {
            }
        };
    }

    interface FireCallback {
        void callback(Object result);
    }

    public void getMaxUserID(FireCallback resultHandler) {
        getInstanceDatabase();
        fStore.collection("users")
            .orderBy("user_id", Query.Direction.DESCENDING).limit(1)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.isSuccessful()) {
                        resultHandler.callback(-1);
                    }

                    Long maxUserID = 0L;
                    QuerySnapshot snapshot = task.getResult();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        maxUserID = (Long) doc.get("user_id");
                    }

                    resultHandler.callback(maxUserID);
                }
            }
        );
    }

    public boolean loadUserID(FireCallback resultHandler) {
        String UUID = null;

        try {
            UUID = fAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            // callback is passed e is user is not logged in in fireAuth
            Log.e("FIRE_ERROR", "NO UUID");
            resultHandler.callback(e);
            return false;
        }

        getInstanceDatabase();
        fStore.collection("users")
            .whereEqualTo("userUID", UUID)
            .addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(
                        @Nullable QuerySnapshot value,
                        @Nullable FirebaseFirestoreException error
                    ) {
                        if (error != null) {
                            // callback is passed null if firebase has error
                            resultHandler.callback(error);
                            Log.e("FIRE_ERROR", "NO MATCHING UUID");
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, Object> docData = doc.getData();
                            int userID = -1;

                            try {
                                userID = Math.toIntExact((Long) docData.get("user_id"));
                            } catch (NullPointerException e) {
                                Log.e("FIRE_ERROR", docData.toString());
                                Log.e("FIRE_ERROR", "NO user_id FIELD");
                                resultHandler.callback(e);
                            }

                            resultHandler.callback(userID);
                            return;
                        }

                        // userID not set for current user
                        resultHandler.callback(-1);
                    }
                }
            );

        return true;
    }
}
