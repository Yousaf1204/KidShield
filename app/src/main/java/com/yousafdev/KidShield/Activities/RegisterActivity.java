package com.yousafdev.KidShield.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yousafdev.KidShield.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword, editTextParentEmail;
    private TextInputLayout textInputLayoutParentEmail;
    private Button buttonRegister;
    private TextView textViewLoginPrompt;
    private RadioGroup radioGroupRole;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        editTextEmail = findViewById(R.id.editText_email_register);
        editTextPassword = findViewById(R.id.editText_password_register);
        editTextParentEmail = findViewById(R.id.editText_parent_email);
        textInputLayoutParentEmail = findViewById(R.id.textInputLayout_parent_email);
        buttonRegister = findViewById(R.id.button_register);
        textViewLoginPrompt = findViewById(R.id.textView_login_prompt);
        radioGroupRole = findViewById(R.id.radioGroup_role);
        progressBar = findViewById(R.id.progressBar_register);

        // Listener for role selection to show/hide the parent email field
        radioGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton_child) {
                textInputLayoutParentEmail.setVisibility(View.VISIBLE);
            } else {
                textInputLayoutParentEmail.setVisibility(View.GONE);
            }
        });

        // Listener for the register button
        buttonRegister.setOnClickListener(v -> registerUser());

        // Listener to switch back to login screen
        textViewLoginPrompt.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String parentEmail = editTextParentEmail.getText().toString().trim();
        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();

        // --- Input Validation ---
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role (Parent/Child)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedRoleId == R.id.radioButton_child && TextUtils.isEmpty(parentEmail)) {
            Toast.makeText(this, "Please enter your parent's email", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- End Validation ---

        progressBar.setVisibility(View.VISIBLE);

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            RadioButton selectedRadioButton = findViewById(selectedRoleId);
                            String role = selectedRadioButton.getText().toString().toLowerCase();

                            // Create a user map to store in Realtime Database
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("uid", userId);
                            userMap.put("email", email);
                            userMap.put("role", role);

                            if (role.equals("child")) {
                                userMap.put("parentEmail", parentEmail);
                            }

                            // Save user info to database
                            mDatabase.child("users").child(userId).setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            // Send user to Login screen after successful registration
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            // Handle database write error
                                            Toast.makeText(RegisterActivity.this, "Database Error: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        // Handle registration failure
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}