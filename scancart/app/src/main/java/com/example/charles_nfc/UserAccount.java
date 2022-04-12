package com.example.charles_nfc;

import android.content.Context;
import android.content.SharedPreferences;

public class UserAccount {
    // singleton managing our userID, whether or not
    // we're logged out, and writing to shared preferences
    // when we log in / out
    final static String preferenceName = "account";
    final static String preferenceKey = "user_id";
    final static int INVALID_ID = -1;
    private static UserAccount account;
    private int userID = INVALID_ID;

    private UserAccount() {}
    private UserAccount(int userID, Context context) {
        // constructor of the SingletonExample class
        this.setUserID(userID);
        this.saveUserID(context);
    }

    private void setUserID(int userID) {
        this.userID = userID;
    }

    public int getUserID() {
        return userID;
    }

    public boolean isLoggedIn() {
        return this.userID != INVALID_ID;
    }

    public static UserAccount getAccount() {
        // write code that allows us to create only one object
        // access the object as per our need
        if (account == null) {
            account = new UserAccount();
        }
        return account;
    }

    public boolean saveUserID(Context context, int userID) {
        this.setUserID(userID);
        return this.saveUserID(context);
    }

    private boolean saveUserID(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
            preferenceName, Context.MODE_PRIVATE
        );

        if (this.userID == INVALID_ID) {
            return false;
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(preferenceKey, this.userID);
        editor.apply();
        return true;
    }

    public boolean logout(Context context) {
        this.userID = INVALID_ID;
        SharedPreferences sharedPref = context.getSharedPreferences(
            preferenceName, Context.MODE_PRIVATE
        );

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(preferenceKey, INVALID_ID);
        editor.apply();
        return true;
    }

    public int loadFromContext(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
            preferenceName, Context.MODE_PRIVATE
        );

        this.userID = sharedPref.getInt(preferenceKey, INVALID_ID);
        return this.userID;
    }
}
