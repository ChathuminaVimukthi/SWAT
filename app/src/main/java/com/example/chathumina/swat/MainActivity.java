package com.example.chathumina.swat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

/*
* This class contains the Splash screen code.. Code contains Image view to hold the App logo and a TextView to hold the team name.
* The UI is animated to give a splash effect when the app is launched for the first time in a device.
* The basic purpose of this class is to give a professional feeling about the app with the logo and the team name*/

public class MainActivity extends AppCompatActivity {

    //Local variables assigned
    private TextView xlabs;
    private ImageView logox;
    //Instance of PrefManager class
    private PrefManager prefManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prefManager object is initialized
        prefManager = new PrefManager(this);
        //checks for the first time launch
        if (!prefManager.isFirstTimeLaunch()) {//not first time launch
            launchHomeScreen();//call for the function
            finish();
        }else{//it is first time launch
            //set the content of the UI
            setContentView(R.layout.activity_main);
            //initializing the local variables
            xlabs = (TextView)findViewById(R.id.xlabs);
            logox = (ImageView)findViewById(R.id.logox);
            //Animation is called( Splash effect)
            Animation splash = AnimationUtils.loadAnimation(this,R.anim.logintransition);
            xlabs.startAnimation(splash);//assigning the animation to UI
            logox.startAnimation(splash);
            final Intent signup = new Intent(this, Signup.class);//Intent to direct the user to next activity
            //A Thread is used to run the animation for 5 seconds
            Thread timer = new Thread(){
                public void run(){
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally {
                        startActivity(signup);//after 5 seconds the next activity is called
                        finish();
                    }
                }
            };
            timer.start();
        }

        //check for the SDK version of the device and setting the UI to full screen to give user a better user experiance
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        //method to change the status bar color according the layout background color
        changeStatusBarColor();



    }

    //method to launch the Home screen of the app if it is not the first time launch
    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(MainActivity.this, Home.class));
        finish();
    }

    //method to change the status bar color
    private void changeStatusBarColor() {
        //checks for the sdk version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);//setting the status bar color to tranparent
        }
    }
}
