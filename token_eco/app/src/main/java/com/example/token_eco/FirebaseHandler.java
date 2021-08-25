package com.example.token_eco;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class FirebaseHandler {

    private DatabaseReference databaseReference;
    private static boolean isPersistenceEnabled = false;
    private FirebaseAuth auth;

    public FirebaseHandler() {
        this.auth = FirebaseAuth.getInstance();
        if (!isPersistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistenceEnabled = true;
        }
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.databaseReference.keepSynced(true);
    }

    public DatabaseReference getDatabaseReference(){
        return  this.databaseReference;
    }

    public DatabaseReference getDatabaseReferenceIUD(){
        return  this.databaseReference.child(this.auth.getCurrentUser().getUid());
    }

    public DatabaseReference getDatabaseReferenceEnfant(Context context){
        SharedPreferences pref;
        pref = context.getSharedPreferences("pref", MODE_PRIVATE);
        String enfant = pref.getString("enfant", "no data");
        String iud = pref.getString("iud", "no data");
        //this.auth.getCurrentUser().getUid()).child("enfants").child(
        return  this.databaseReference.child(iud).child("enfants").child(enfant);
    }


}
