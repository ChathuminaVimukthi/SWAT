package com.example.chathumina.swat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
* This activity holds the UI of add device function.
* App will prompt for the Device ID and the Home number to add the device.
* In here the home number is the ID of the home you want to register the device for.
* A user can have one to three houses per account.
* The design is a material design.*/

public class Add extends AppCompatActivity {

    //Declaring local variables
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private EditText productId,productName;
    private Button addbtn,cancelbtn;
    private Thread thread;
    private ProgressBar progressBar;
    private int index = 0;

    //initializing bottom navigation bar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(Add.this, Home.class);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.navigation_dashboard:

                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        //initializing edittexts and buttons
        productId = (EditText)findViewById(R.id.productId);
        productName = (EditText)findViewById(R.id.productName);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        addbtn = (Button)findViewById(R.id.addproduct);
        cancelbtn = (Button)findViewById(R.id.cancel);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //database reference pointing to root of database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //EventHandler for add button
        addbtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Initializing instance variables
                final String prodId = productId.getText().toString();
                final String prodName = productName.getText().toString();

                //validating the input fields
                if (TextUtils.isEmpty(prodId)) {
                    Toast.makeText(getApplicationContext(), "Enter Product ID!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(prodName)) {
                    Toast.makeText(getApplicationContext(), "Enter Product Name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //displaying the progress bar
                progressBar.setVisibility(View.VISIBLE);

                //adding the device under Devices in databse
                mDatabase.child("Devices").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Toast.makeText(Add.this, "Added device successfully:onComplete:", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        String currentUser = mAuth.getCurrentUser().getUid();
                        Products prod = new Products(prodName,currentUser);
                        mDatabase.child("Devices").child(prodId).setValue(prod);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //Getting the added device to the users account
                mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Devices").child(prodId).child("Name").setValue(prodName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                feedMultiple(prodId);

            }
        });

        //EventHandler for the cancel button
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Add.this, Home.class));
            }
        });

    }

    private void feedMultiple(final String prodId) {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                String s = Integer.toString((int) (Math.random() * 40) + 30);
                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Devices").child(prodId).child("Data").child("Seconds").child(Integer.toString(index)).setValue(s);
                index++;
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 51; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }
}
