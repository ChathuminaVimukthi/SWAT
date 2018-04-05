package com.example.chathumina.swat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


public class FirebaseHelper {

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    Boolean saved=null;

    public FirebaseHelper(DatabaseReference mDatabase, FirebaseAuth mAuth) {
        this.mDatabase = mDatabase;
        this.mAuth = mAuth;
    }

    //SAVE
    public Boolean save(Spacecraft spacecraft)
    {
        if(spacecraft==null)
        {
            saved=false;
        }else
        {
            try
            {
                mDatabase.child("Spacecraft").push().setValue(spacecraft);
                saved=true;
            }catch (DatabaseException e)
            {
                e.printStackTrace();
                saved=false;
            }
        }

        return saved;
    }

    //READ
    public ArrayList<String> retrieve()
    {
        final ArrayList<String> spacecrafts=new ArrayList<>();
        spacecrafts.add("Select Devices");

        mDatabase.child("Users").child(mAuth.getCurrentUser().getUid()).child("Devices").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String name = dataSnapshot.child("Name").getValue(String.class);
                spacecrafts.add(name);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //fetchData(dataSnapshot,spacecrafts);
                String name = dataSnapshot.child("Name").getValue(String.class);
                //spacecrafts.add(name);

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

        return spacecrafts;
    }

    private void fetchData(DataSnapshot snapshot, ArrayList<String> spacecrafts)
    {
        spacecrafts.clear();
        for (DataSnapshot ds:snapshot.getChildren())
        {
            String name=ds.getValue(String.class);
            spacecrafts.add(name);
        }

    }
}