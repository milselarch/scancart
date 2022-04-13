package com.example.charles_nfc;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfile extends AppCompatActivity {
    private Button Confirm;
    private Button b2Login;
    private EditText nameField;
    private EditText phoneNumberField;
    private EditText streetAddressField;
    private EditText postalCodeField;
    private EditText floorAndUnitField;
    private String spinnerChoice;
    private Spinner healthConditionsChoice;

    private final FirebaseHandler firebaseManager = new FirebaseHandler();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fStore;
    private final UserAccount account = UserAccount.getAccount();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        account.loadFromContext(getApplicationContext());
        Log.d("USER_LD_ID", String.valueOf(account.getUserID()));

        firebaseAuth = FirebaseHandler.getInstanceAuth();
        fStore = FirebaseHandler.getInstanceDatabase();

        //Fields
        nameField = findViewById(R.id.editName);
        phoneNumberField = findViewById(R.id.editPhoneNumber);
        streetAddressField = findViewById(R.id.editStreetAddress);
        postalCodeField = findViewById(R.id.editPostalcode);
        floorAndUnitField = findViewById(R.id.editUnitnumber);
        healthConditionsChoice = (Spinner) findViewById(R.id.healthConditions);

        //Buttons
        Confirm = findViewById(R.id.confirmDetails);
        b2Login = findViewById(R.id.editB2login);
        //Spinner Handler
        healthConditionsChoice = (Spinner) findViewById(R.id.healthConditions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.health_conditions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        healthConditionsChoice.setAdapter(adapter);
        healthConditionsChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                spinnerChoice = parent.getItemAtPosition(pos).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
                spinnerChoice = parent.getItemAtPosition(5).toString();
            }
        });

        this.loadDefaults();

        Confirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String UID = FirebaseHandler.getUID();
                String Name = nameField.getText().toString();
                String phoneNumber = phoneNumberField.getText().toString();
                String streetAddress = streetAddressField.getText().toString();
                String postalCode = postalCodeField.getText().toString();
                String floorAndUnit = floorAndUnitField.getText().toString();

                if (account.isLoggedIn()) {
                    Log.d("T_LOGGED_IN", String.valueOf(account.getUserID()));

                    long userID = (long) account.getUserID();
                    Log.d("LOADED_ID", String.valueOf(userID));
                    FirebaseHandler.User user = new FirebaseHandler().new User(
                        UID, Name, phoneNumber, streetAddress, postalCode,
                        floorAndUnit, spinnerChoice, userID
                    );

                    FirebaseHandler.editUser(user, fStore, (Object editResult) -> {
                        boolean success = (Boolean) editResult;
                        onUserEdited(success, Math.toIntExact(userID));
                    });
                    return;
                }

                Log.d("NOT_LOGGED_IN", String.valueOf(account.getUserID()));
                firebaseManager.loadUserID(new FirebaseHandler.FireCallback() {
                    @Override
                    public void callback(Object result) {
                        if (
                            (result instanceof Exception) ||
                            ((int) result == -1)
                        ) {
                            Toast.makeText(
                                EditProfile.this,
                                "failed to acquire user id",
                                Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        long userID = (long) (int) result;
                        FirebaseHandler.User user = new FirebaseHandler().new User(
                            UID, Name, phoneNumber, streetAddress, postalCode,
                            floorAndUnit, spinnerChoice, userID
                        );

                        FirebaseHandler.editUser(user, fStore, (Object editResult) -> {
                            boolean success = (Boolean) editResult;
                            onUserEdited(success, Math.toIntExact(userID));
                        });
                    }
                });
            }
        });

        b2Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(
                    EditProfile.this, Profile.class
                ));
            }
        });
    }

    void loadDefaults() {
        CollectionReference collection = fStore.collection("users");
        String UUID = FirebaseHandler.getUID();

        if (UUID != null) {
            DocumentReference result = collection.document(UUID);
            result.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot user = task.getResult();
                        if (user.exists()) {
                            nameField.setText(user.getData().get("userName").toString());
                            phoneNumberField.setText(user.getData().get("userPhoneNumber").toString());
                            streetAddressField.setText(user.getData().get("userStreetAddress").toString());
                            postalCodeField.setText(user.getData().get("userPostalCode").toString());
                            floorAndUnitField.setText(user.getData().get("userFloorAndUnit").toString());
                        }
                    }
                }
            });
        } else {
            account.logout(getApplicationContext());
            firebaseAuth.signOut();

            Log.e("LOGOUT", "UID NOT FOUND");
            Toast.makeText(
                this, "Failed to load profile",
                Toast.LENGTH_SHORT
            ).show();

            startActivity(new Intent(
                EditProfile.this, MainActivity.class
            ));
            finish();
        }
    }

    void loadFragmentActivity(int fragmentID) {
        Intent main_intent = new Intent(
            EditProfile.this, FragmentActivity.class
        );
        // tell fragment activity we want to go to cart fragment
        main_intent.putExtra("fragment_id", fragmentID);
        startActivity(main_intent);
    }

    void onUserEdited(boolean success, int userID) {
        Log.d("USER_EDIT_END", String.valueOf(userID));

        if (!success) {
            Toast.makeText(
                this, "failed to edit user",
                Toast.LENGTH_SHORT
            ).show();
        };

        assert userID != -1;
        account.saveUserID(getApplicationContext(), userID);
        assert account.isLoggedIn();
        Log.d("SET_USER_ID", String.valueOf(account.getUserID()));
        account.loadFromContext(getApplicationContext());
        Log.d("SET_USER_ID2", String.valueOf(account.getUserID()));
        assert account.getUserID() == userID;

        Toast.makeText(
            this, "successfully edited user",
            Toast.LENGTH_SHORT
        ).show();

        Log.d("EDIT_END", "GO MAIN PROFILE");
        loadFragmentActivity(R.id.profile);
    }
}