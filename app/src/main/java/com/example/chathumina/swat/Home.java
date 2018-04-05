package com.example.chathumina.swat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/*
* This activity is the base activity of the app.
* All the functionality are carried out in this class.
* After the first time launch of the app, when ever a user launches the app again, this activity will be displayed to him/her.
* Line graph is used is this activity to give the user a visual impact over his/her usages.
* Vibrant colors are used for easy navigation and remembrance of the UI components
* Without using the common navigation drawer, we used bottom navigation bar for easy access with one hand use.
* Used scrollView to give user a fluid feeling when interacting with the application.
* Used a material design to give a modern UI design for the user.
* Graph is fully responsive to view all the data plots.
* Used cardView without using commonly used buttons for better user experience. */

public class Home extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnChartValueSelectedListener {

    //Declaring the local variables
    private CardView applianceCard,optimizeCard,addCard;
    private LineChart mChart;
    DatabaseReference mDatabase, userRef;
    Context mContext;
    private Thread thread;
    FirebaseAuth mAuth;
    private int index = 0;
    private int index1 = 0;
    private int index2 = 0;
    private int index3 = 0;
    private ArrayList<String> usageList = new ArrayList<>();
    private ArrayList<String> usageKeys = new ArrayList<>();
    //String array to hold the spinner elements
    String[] usageType ={"per seconds","per hour","per 12hrs","per day"};

    //Initializing the bottom navigation bar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //handling the navigation bar events
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

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
        setContentView(R.layout.activity_home);

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);
        mContext = getApplicationContext();

        //Creating the ArrayAdapter instance having the name list
        ArrayAdapter aa = new ArrayAdapter(this,R.layout.spinner_layout, usageType);
        aa.setDropDownViewResource(R.layout.spinner_layout2);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);

        //line chart is initialized
        mChart = (LineChart) findViewById(R.id.linechart);
        //event handler for the chart
        mChart.setOnChartValueSelectedListener(this);


        //database reference pointing to root of database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        //database reference pointing to demo node
        userRef = mDatabase.child("users");
        userRef.keepSynced(true);

        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.TRANSPARENT);

        final LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(200f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        applianceCard = (CardView) findViewById(R.id.appliancesCard);
        optimizeCard = (CardView) findViewById(R.id.optimizeCard);
        addCard = (CardView) findViewById(R.id.addCard);

        //event handler for the card views
        applianceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Appliances.class);
                startActivity(intent);
                finish();
            }
        });

        optimizeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Add.class);
                startActivity(intent);
                finish();
            }
        });


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //arrayAdapter to hold the data sets passed from firebase database
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usageList );

        //default data set to display on the graph is selected
        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("Seconds").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                String value1 = dataSnapshot.getValue(String.class);
                usageList.add(value1);

                String key = dataSnapshot.getKey();
                usageKeys.add(key);

                //entry for the graph is added
                adapter.notifyDataSetChanged();
                int entry = Integer.valueOf(value1);
                addEntry(entry,20);

            }

            //event handler for the data set changed
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //adding the new data set into the graph
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                int index = usageKeys.indexOf(key);
                usageList.set(index,value);
                int entry = Integer.valueOf(value);
                addEntry(entry,50);
                adapter.notifyDataSetChanged();


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        feedMultiple();
        feedMultiple12hrs();
        feedMultipleDays();
        feedMultipleHour();

    }

    private void addEntry(int entry,int range) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), entry), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(range);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    //method to add data plots to the graph
    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Current Usage");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(usageType[position].equalsIgnoreCase("per hour")){
            usageList.clear();
            mChart.invalidate();
            mChart.clearValues();
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usageList );

            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("Minutes").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String value1 = dataSnapshot.getValue(String.class);
                    usageList.add(value1);

                    String key = dataSnapshot.getKey();
                    usageKeys.add(key);

                    adapter.notifyDataSetChanged();
                    int entry = Integer.valueOf(value1);
                    addEntry(entry,12);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    String value = dataSnapshot.getValue(String.class);
                    String key = dataSnapshot.getKey();
                    int index = usageKeys.indexOf(key);
                    usageList.set(index,value);
                    int entry = Integer.valueOf(value);
                    addEntry(entry,12);
                    adapter.notifyDataSetChanged();


                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(usageType[position].equalsIgnoreCase("per seconds")){
            usageList.clear();
            mChart.invalidate();
            mChart.clearValues();
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usageList );

            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("Seconds").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String value1 = dataSnapshot.getValue(String.class);
                    usageList.add(value1);

                    String key = dataSnapshot.getKey();
                    usageKeys.add(key);

                    adapter.notifyDataSetChanged();
                    int entry = Integer.valueOf(value1);
                    addEntry(entry,50);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    String value = dataSnapshot.getValue(String.class);
                    String key = dataSnapshot.getKey();
                    int index = usageKeys.indexOf(key);
                    usageList.set(index,value);
                    int entry = Integer.valueOf(value);
                    addEntry(entry,50);
                    adapter.notifyDataSetChanged();


                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(usageType[position].equalsIgnoreCase("per 12hrs")){
            usageList.clear();
            mChart.invalidate();
            mChart.clearValues();
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usageList );

            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("12hours").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String value1 = dataSnapshot.getValue(String.class);
                    usageList.add(value1);

                    String key = dataSnapshot.getKey();
                    usageKeys.add(key);

                    adapter.notifyDataSetChanged();
                    int entry = Integer.valueOf(value1);
                    addEntry(entry,12);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    String value = dataSnapshot.getValue(String.class);
                    String key = dataSnapshot.getKey();
                    int index = usageKeys.indexOf(key);
                    usageList.set(index,value);
                    int entry = Integer.valueOf(value);
                    addEntry(entry,12);
                    adapter.notifyDataSetChanged();


                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(usageType[position].equalsIgnoreCase("per day")){
            usageList.clear();
            mChart.invalidate();
            mChart.clearValues();
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usageList );

            mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("Days").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String value1 = dataSnapshot.getValue(String.class);
                    usageList.add(value1);

                    String key = dataSnapshot.getKey();
                    usageKeys.add(key);

                    adapter.notifyDataSetChanged();
                    int entry = Integer.valueOf(value1);
                    addEntry(entry,7);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    String value = dataSnapshot.getValue(String.class);
                    String key = dataSnapshot.getKey();
                    int index = usageKeys.indexOf(key);
                    usageList.set(index,value);
                    int entry = Integer.valueOf(value);
                    addEntry(entry,7);
                    adapter.notifyDataSetChanged();


                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void feedMultiple() {
        index =0;

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                String s = Integer.toString((int) (Math.random() * 40) + 30);
                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("Seconds").child(Integer.toString(index)).setValue(s);

                index++;
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 60; i++) {

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

    private void feedMultiple12hrs() {
        index1 =0;


        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                String s = Integer.toString((int) (Math.random() * 40) + 30);
                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("12hours").child(Integer.toString(index1)).setValue(s);

                index1++;
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 2; i++) {

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

    private void feedMultipleDays() {
        index2 =0;


        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                String s = Integer.toString((int) (Math.random() * 40) + 30);
                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("Days").child(Integer.toString(index2)).setValue(s);

                index2++;
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 30; i++) {

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

    private void feedMultipleHour() {
        index3 =0;


        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                String s = Integer.toString((int) (Math.random() * 40) + 30);
                mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Total Usage").child("Data").child("Minutes").child(Integer.toString(index3)).setValue(s);

                index3++;
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 60; i++) {

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
