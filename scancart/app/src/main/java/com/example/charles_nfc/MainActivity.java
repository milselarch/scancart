package com.example.charles_nfc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.charles_nfc.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

//TODO: Find a way to input user data into Firestore
//TODO: Maybe find a way to authenticate the number first before OTP (Check firestore query first?)
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    UserAccount account;

    //if code send failed, will be used to resend code OTP
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks Callbacks;
    private String verificationID;
    private static final String TAG= "MAIN_TAG";
    private final FirebaseHandler firebaseManager = new FirebaseHandler();
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // disable auto dark mode across app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneLl.setVisibility(View.VISIBLE); //show phone layout
        binding.codeLl.setVisibility(View.GONE); //hide code layout, when OTP sent then hide phone, show code
        firebaseAuth = FirebaseHandler.getInstanceAuth();

        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);

        Context context = this.getApplicationContext();
        account = UserAccount.getAccount();
        account.loadFromContext(context);

        if (account.isLoggedIn()) {
            Toast.makeText(this, "logged in", Toast.LENGTH_SHORT).show();
            startFragmentActivity();
        } else {
            Toast.makeText(this, "not logged in", Toast.LENGTH_SHORT).show();
        }

        Callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //Invoked on two situations:
                //1- Instant Verification without needing to send OTP Code
                //2= Auto Retrieval = performs verfication without user action
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //invalid request for verification e.g. invalid phone format
                pd.dismiss();
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String ID, @NonNull PhoneAuthProvider.ForceResendingToken token){
                super.onCodeSent(ID, forceResendingToken);
                //SMS Code sent to phone number
                //ask user to enter code -> construct credential by combining code with verification ID
                Log.d(TAG, "onCodeSent: " +ID);

                verificationID = ID;
                forceResendingToken = token;
                pd.dismiss();
                //hide phone layout, show code layout
                binding.phoneLl.setVisibility(View.GONE);
                binding.codeLl.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Verification code sent", Toast.LENGTH_SHORT).show();
                binding.codeSentDescription.setText("Please type the verification code we sent \nto " + binding.phoneEt.getText().toString().trim());
            }
        };

        binding.phoneContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = binding.phoneEt.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(
                        MainActivity.this,
                        "Please enter phone number",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    startPhoneNumberVerification(phone);
                }
            }
        });

        binding.resendCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = binding.phoneEt.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(MainActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                }
                else{
                    resendVerification(phone, forceResendingToken);
                }
            }
        });

        binding.codeSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = binding.codeEt.getText().toString().trim();
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(MainActivity.this, "Please enter Verification code", Toast.LENGTH_SHORT).show();
                }
                else{
                    System.out.println(verificationID);
                    //prevents loss of MMR
                    verifyPhoneAndCode(verificationID, code, savedInstanceState);
                }
            }
        });
    }

    void startFragmentActivity() {
        Intent profile = new Intent(
            MainActivity.this, FragmentActivity.class
        );
        startActivity(profile);
    }

    private void startPhoneNumberVerification(String phone){
        pd.setMessage("Verifying Phone Number");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(Callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerification(
        String phone, PhoneAuthProvider.ForceResendingToken token
    ) {
        pd.setMessage("Resending Code");
        pd.show();

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(Callbacks)
                .setForceResendingToken(token)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneAndCode(
        String verificationID, String code, @NonNull Bundle savedInstanceState
    ) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(
            verificationID, code
        );
        if (verificationID == null && savedInstanceState != null){
            onRestoreInstanceState(savedInstanceState);
        }
        signInWithPhoneAuthCredential(credential);
        pd.setTitle("Verifying Code");
        pd.show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("KEY_ID", verificationID);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verificationID = savedInstanceState.getString("KEY_ID");
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        pd.setMessage("Logging in");
        Context context = this.getApplicationContext();

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener(new OnSuccessListener<AuthResult>(){
                @Override
                public void onSuccess(AuthResult authResult){
                    //Successful sign in
                    pd.dismiss();
                    String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                    Toast.makeText(
                        MainActivity.this, "Logged in as" + phone,
                        Toast.LENGTH_SHORT
                    ).show();

                    FirebaseHandler.FireCallback listener = new FirebaseHandler.FireCallback() {
                        @Override
                        public void callback(Object result) {
                            assert result != null;
                            if (result instanceof Exception) {
                                callback((Exception) result);
                                return;
                            }

                            int userID = (Integer) result;
                            callback(userID);
                        }

                        public void callback(int userID) {
                            if (userID == -1) {
                                // user has no userID set
                                Log.d("USER_ID", "No id set");
                                startFragmentActivity();
                                return;
                            }

                            // save userID to sharedPreferences
                            // and update account accordingly
                            account.saveUserID(context, userID);
                            assert account.isLoggedIn();

                            // go to profile activity after logging in
                            startFragmentActivity();
                        }

                        public void callback(Exception result) {
                            Log.e("FIRE_ERROR", result.toString());
                            Toast.makeText(
                                MainActivity.this,
                                "Firebase error encountered",
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    };

                    firebaseManager.loadUserID(listener);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed Signin in
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("LOGOUT", "MAIN_LOAD_FAILED");
                    account.logout(context);
                    assert !account.isLoggedIn();
                }
            });
    }
}