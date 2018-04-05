package com.example.chathumina.swat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Console;
/*
* This activity contains the user Sign up UI.
* UI is based on modern material design for a better UX.
*User is signed in using an email address and a password.
* The Email address is authenticated via Google Firebase.
* User data is completely secured using the firebase database rules.*/

public class Signup extends AppCompatActivity {
//    local variables declared
    private EditText inputEmail, inputPassword, userId;
    private Button btnSignIn, btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    //Instance of the prefManager class is created
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get PrefManager instance and initializing it
        prefManager = new PrefManager(this);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        //Get Firebase database and initializing it
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_sugnup);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        changeStatusBarColor();

        //Initializing the local variables
        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        userId = (EditText) findViewById(R.id.userId);

        //Event handler for signin button
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User is directed to the login activity
                startActivity(new Intent(Signup.this, Login.class));

            }
        });

        //Event handler for signup button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Instance variables are initialized with the user inputs
                final String email = inputEmail.getText().toString().trim();
                final String password = inputPassword.getText().toString().trim();
                final String userID = userId.getText().toString().trim();

                //validating the entered name
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //validating the entered password
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //validating the size of the password for more security
                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //progress bar is displayed
                progressBar.setVisibility(View.VISIBLE);
                //create user in the database
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(Signup.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);//setting the progress bar to disappear
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user to be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Signup.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");//Setting the location to write user data
                                    DatabaseReference currentUser = mDatabase.child(auth.getCurrentUser().getUid());//getting the unique user id created using firebase authentication
                                    User user = new User(password, email,userID);//creating the user object
                                    currentUser.setValue(user);//sending the user object to the databse under the unique ID for better security.
                                    Toast.makeText(Signup.this, "Authentication correct.",
                                            Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(Signup.this, Walkthrough.class));//finally user is directed to the home activity
                                    finish();
                                }
                            }
                        });

            }
        });
    }

    //handling the progressbar
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    //method to change the status bar color according to the background color
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
