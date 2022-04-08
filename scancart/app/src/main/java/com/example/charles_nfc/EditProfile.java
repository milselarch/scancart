package com.example.charles_nfc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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


        DocumentReference result = fStore.collection("users").document(FirebaseHandler.getUID());
        result.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
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

        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UID = FirebaseHandler.getUID();
                String Name = nameField.getText().toString();
                String phoneNumber = phoneNumberField.getText().toString();
                String streetAddress = streetAddressField.getText().toString();
                String postalCode = postalCodeField.getText().toString();
                String floorAndUnit = floorAndUnitField.getText().toString();
                FirebaseHandler.User user = new FirebaseHandler().new User(UID, Name, phoneNumber, streetAddress, postalCode, floorAndUnit, spinnerChoice);
                FirebaseHandler.editUser(user, fStore);
                startActivity(new Intent(EditProfile.this, ProfileActivity.class));

            }
        });

        b2Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditProfile.this, ProfileActivity.class));
            }
        });

    }
}