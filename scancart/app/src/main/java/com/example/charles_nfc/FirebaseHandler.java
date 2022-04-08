package com.example.charles_nfc;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHandler {
    protected static FirebaseFirestore fStore;
    protected static FirebaseAuth fAuth;
    protected FirebaseHandler(){

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

    public static String getPhone() {return fAuth.getCurrentUser().getPhoneNumber();}

    class User{
        String userUID;
        String userName;
        String userPhoneNumber;
        String userStreetAddress;
        String userPostalCode;
        String userFloorAndUnit;
        String userHealthConditions;

        public User(){}

        public User(String UUID, String Name, String phoneNumber, String streetAddress, String postalCode, String floorAndUnit, String healthConditions){
            this.userUID = UUID;
            this.userName = Name;
            this.userPhoneNumber = phoneNumber;
            this.userStreetAddress = streetAddress;
            this.userPostalCode = postalCode;
            this.userFloorAndUnit = floorAndUnit;
            this.userHealthConditions = healthConditions;
        }

        public String getUserUID(){
            return this.userUID;
        }
        void setUserUID(String UUID){
            this.userUID = UUID;
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

    public static void registerUser(User user, FirebaseFirestore fStore){
        Log.d(TAG, "" + user.getUserUID());
        DocumentReference documentReference = fStore.collection("users").document(user.getUserUID());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User Created");
            }
        });

    }
    public static void editUser(User user, FirebaseFirestore fStore){
        Log.d(TAG, "" + user.getUserUID());
        DocumentReference documentReference = fStore.collection("users").document(user.getUserUID());
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "User Edited");
            }
        });

    }


}
