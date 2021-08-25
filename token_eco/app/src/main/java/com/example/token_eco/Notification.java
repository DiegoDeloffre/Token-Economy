package com.example.token_eco;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notification extends menu_handler {

    private Boolean pasEncoreCree = true;
    private RecyclerView recyclerView;
    private Map<String, String>  listeNotif;
    private DatabaseReference drNotifs;
    private ValueEventListener eventNotifs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        recyclerView = findViewById(R.id.recyclerViewNotification);
        super.setupToolBar();
        listeNotif = new HashMap<>();
        afficherNotifs();
        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void afficherNotifs() {
        FirebaseHandler fh = new FirebaseHandler();
        drNotifs = fh.getDatabaseReferenceIUD().child("notifications");
        eventNotifs = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listeNotif.clear();
                if(pasEncoreCree){
                    RecyclerView.LayoutManager layMan = new LinearLayoutManager(Notification.this);
                    recyclerView.setLayoutManager(layMan);
                    pasEncoreCree = false;
                }
                for(DataSnapshot ds :  dataSnapshot.getChildren()){
                    String iud = ds.getKey();
                    String nomPrenom = ds.getValue(String.class);
                    listeNotif.put(iud, nomPrenom);
                }
                List<creationListeNotification> myListData = new ArrayList<creationListeNotification>();
                for(HashMap.Entry<String, String> map : listeNotif.entrySet()){
                    myListData.add(new creationListeNotification(map.getValue(), map.getKey()));
                }
                gestionListeNotification adapter = new gestionListeNotification(myListData);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        drNotifs.addValueEventListener(eventNotifs);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        drNotifs.removeEventListener(eventNotifs);
        super.onDestroy();
    }
}
