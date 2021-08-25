package com.example.token_eco;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class accueil extends menu_handler implements NavigationView.OnNavigationItemSelectedListener{
    private List<creationListeEnfant> listeEnfants;
    private boolean doubleBack = false;
    private RecyclerView recyclerView;
    private gestionListeEnfant adapter;
    private FirebaseHandler fh;
    private ValueEventListener eventEnfants;
    private DatabaseReference enfantsAffichage;
    private Map<DatabaseReference , ValueEventListener> EnfantsDesAutres;
    private List<String> iudDesAutres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        super.setupToolBar();

        Button button_enfant_creation =  findViewById(R.id.button_enfant_creation);
        recyclerView = findViewById(R.id.recyclerViewEnfants);

        fh = new FirebaseHandler();
        enfantsAffichage = fh.getDatabaseReferenceIUD();
        listeEnfants = new ArrayList<>();
        EnfantsDesAutres = new HashMap<>();
        iudDesAutres = new ArrayList<>();
        creerView();
        GestionNotif();
        afficherEnfants();

        button_enfant_creation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent page= new Intent(getApplicationContext(), enfant_creation.class);
                startActivity(page);
            }
        });

    }

    public void afficherEnfants(){
        eventEnfants = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String userIud = auth.getCurrentUser().getUid();
                    for (Iterator<creationListeEnfant> iterator = listeEnfants.iterator(); iterator.hasNext(); ) {
                        String iud2 = iterator.next().getIud();
                        if (userIud.equals(iud2)) {
                            iterator.remove();
                        }
                    }
                    for (DataSnapshot ds : dataSnapshot.child("enfants").getChildren()) {
                        if (ds.child("nbBravos").getValue() != null) {
                            String prenom = ds.child("prenom").getValue(String.class);
                            String surnom = ds.child("surnom").getValue(String.class);
                            String url = ds.child("avatar").getValue(String.class);
                            listeEnfants.add(0, new creationListeEnfant(prenom, surnom, ds.getKey(), userIud, url));
                        }
                    }
                    lireMapEnfant();
                    if (dataSnapshot.hasChild("JePeuxLireSesEnfants")) {
                        for (DataSnapshot ds : dataSnapshot.child("JePeuxLireSesEnfants").getChildren()) {
                            if(!iudDesAutres.contains(ds.getKey())){
                                iudDesAutres.add(ds.getKey());
                                DatabaseReference partage = fh.getDatabaseReference().child(ds.getKey());
                                ValueEventListener event = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (Iterator<creationListeEnfant> iterator = listeEnfants.iterator(); iterator.hasNext(); ) {
                                            String iud = iterator.next().getIud();
                                            if (dataSnapshot.getKey().equals(iud)) {
                                                iterator.remove();
                                            }
                                        }
                                        for (DataSnapshot ds : dataSnapshot.child("enfants").getChildren()) {
                                            if (ds.child("nbBravos").getValue() != null) {
                                                String prenom = ds.child("prenom").getValue(String.class);
                                                String surnom = ds.child("surnom").getValue(String.class);
                                                String url = ds.child("avatar").getValue(String.class);
                                                listeEnfants.add(new creationListeEnfant(prenom, surnom, ds.getKey(), dataSnapshot.getKey(), url));
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                };
                                EnfantsDesAutres.put(partage, event);
                                partage.addValueEventListener(event);
                            }

                        }
                    }
                    List<String> tmp = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.child("JePeuxLireSesEnfants").getChildren()) {
                        tmp.add(ds.getKey());
                    }
                    if(iudDesAutres.size() != tmp.size()){
                        for(String s : iudDesAutres){
                            if(!tmp.contains(s)){
                                accueil.this.recreate();
                            }
                        }
                        iudDesAutres.clear();
                        iudDesAutres.addAll(tmp);
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(accueil.this, "Erreur lors de la récupération d'informations", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    Intent co = new Intent(accueil.this, connexion.class);
                    finishAffinity();
                    startActivity(co);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        enfantsAffichage.addValueEventListener(eventEnfants);
    }

    public void lireMapEnfant(){
        adapter = new gestionListeEnfant(listeEnfants, accueil.this);
        recyclerView.setAdapter(adapter);
    }

    private void creerView(){
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layMan = new LinearLayoutManager(accueil.this);
        recyclerView.setLayoutManager(layMan);
        recyclerView.addItemDecoration(new EspaceInterItemListe(30));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!super.MenuEstOuvert()){
            if (doubleBack) {
                this.finishAffinity();
                //System.exit(0);
            }
            this.doubleBack = true;
            Toast.makeText(this, "Retour une 2eme fois pour quitter", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBack=false;
                }
            }, 2000);
        }
    }

    public void GestionNotif() {
        FirebaseHandler fh = new FirebaseHandler();
        DatabaseReference notif = fh.getDatabaseReferenceIUD();
        notif.child("notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    addNotification();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void addNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(accueil.this, "synchro")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Demande de partage")
                .setContentText("Quelqu'un souhaite partager le suivi de son enfant avec vous")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo_petit);
        Intent notificationIntent = new Intent(this.getApplicationContext(), Notification.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "synchro";
            NotificationChannel channel = new NotificationChannel(channelId, "Demande de partage", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        manager.notify(0, builder.build());
    }


    @Override
    protected void onStop() {
        enfantsAffichage.removeEventListener(eventEnfants);
        for(Map.Entry<DatabaseReference, ValueEventListener> map : EnfantsDesAutres.entrySet()){
            map.getKey().removeEventListener(map.getValue());
        }
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        enfantsAffichage.addValueEventListener(eventEnfants);
        for(Map.Entry<DatabaseReference, ValueEventListener> map : EnfantsDesAutres.entrySet()){
            map.getKey().addValueEventListener(map.getValue());
        }
    }


    @Override
    protected void onDestroy() {
        enfantsAffichage.removeEventListener(eventEnfants);
        for(Map.Entry<DatabaseReference, ValueEventListener> map : EnfantsDesAutres.entrySet()){
            map.getKey().removeEventListener(map.getValue());
        }
        super.onDestroy();
    }
}
