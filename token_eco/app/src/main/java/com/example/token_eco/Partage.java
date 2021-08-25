package com.example.token_eco;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Partage extends menu_handler {
    private static final String TAG = "MainActivity";
    private ViewPager mViewPager;
    private FirebaseAuth mAuth;
    private List<creationListePartageEnvoyes> listePartageEnvoyes;
    private List<creationListePartageRecus> listePartageRecus;
    private DatabaseReference recups;
    private SectionsPagerAdapter adapter;
    private ValueEventListener eventRecup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partage);
        super.setupToolBar();
        Log.d(TAG, "onCreate: Starting.");

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);


        TabLayout tabLayout =  findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FirebaseHandler fh = new FirebaseHandler();

        listePartageEnvoyes = new ArrayList<>();
        listePartageRecus = new ArrayList<>();
        recups = fh.getDatabaseReferenceIUD();
        adapter = new SectionsPagerAdapter( getSupportFragmentManager());
        recuperationAdultes();


        Button partage = findViewById(R.id.button_partage);
        partage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText syncMail = new EditText(Partage.this);
                syncMail.setTextColor(Color.BLACK);
                syncMail.setHint("Saisir l'adresse mail");
                syncMail.setHintTextColor(Color.parseColor("#AAAAAA"));
                syncMail.setPadding(50,70,50,40);
                syncMail.requestFocus();

                syncMail.setInputType(InputType.TYPE_CLASS_TEXT  | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                final AlertDialog.Builder synchroniser = new AlertDialog.Builder(Partage.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                synchroniser.setTitle("Synchroniser les données de vos enfants");
                synchroniser.setMessage("Entrer l'email de la personne avec laquelle vous souhaitez synchroniser les données de vos enfants.");
                synchroniser.setView(syncMail);
                synchroniser.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseHandler fh = new FirebaseHandler();
                        final DatabaseReference recupMail = fh.getDatabaseReference();
                        recupMail.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String mail = syncMail.getText().toString();
                                mAuth = FirebaseAuth.getInstance();
                                String userIud = mAuth.getCurrentUser().getUid();
                                if(!mail.equals(dataSnapshot.child(userIud).child("mail").getValue(String.class))){
                                    for (DataSnapshot ds : dataSnapshot.getChildren() ) {
                                        if (mail.equals(ds.child("mail").getValue(String.class))) {
                                            String nom = dataSnapshot.child(userIud).child("nom").getValue(String.class);
                                            String prenom = dataSnapshot.child(userIud).child("prenom").getValue(String.class);
                                            String utilVise = ds.getKey();
                                            if(!dataSnapshot.child(userIud).child("PeutLireMesEnfants").hasChild(utilVise)){
                                                recupMail.child(ds.getKey()).child("notifications").child(userIud).setValue(nom + " " + prenom);
                                                Toast.makeText(getApplicationContext(), "Notification envoyée !", Toast.LENGTH_LONG).show();
                                            }else{
                                                Toast.makeText(getApplicationContext(), "Cet utilisateur peut déjà voir vos enfants dans son appli", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(), "Vous ne pouvez pas synchroniser avec vous même !", Toast.LENGTH_LONG).show();
                                }

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                synchroniser.setNegativeButton("Retour", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                synchroniser.create().show();
            }
        });

    }


    private void recuperationAdultes(){
        eventRecup = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listePartageEnvoyes.clear();
                listePartageRecus.clear();
                try {
                    for (DataSnapshot ds : dataSnapshot.child("PeutLireMesEnfants").getChildren()) {
                        StorageReference sto = FirebaseStorage.getInstance().getReference().child(ds.getKey()).child("avatar");
                        Task<Uri> uri = sto.getDownloadUrl();
                        while (!uri.isComplete()) {
                        }
                        String url = uri.getResult().toString();
                        listePartageEnvoyes.add(new creationListePartageEnvoyes(ds.getValue(String.class), url, ds.getKey()));
                    }
                    for (DataSnapshot ds : dataSnapshot.child("JePeuxLireSesEnfants").getChildren()) {
                        StorageReference sto = FirebaseStorage.getInstance().getReference().child(ds.getKey()).child("avatar");
                        Task<Uri> uri = sto.getDownloadUrl();
                        while (!uri.isComplete()) {
                        }
                        String url = uri.getResult().toString();
                        listePartageRecus.add(new creationListePartageRecus(ds.getValue(String.class), url, ds.getKey()));
                    }
                    adapter.removeFragments();
                    adapter.addFragment(new PartageFragment1(listePartageEnvoyes, Partage.this), "J'ai partagé à");
                    adapter.addFragment(new PartageFragment2(listePartageRecus, Partage.this), "Ils m'ont partagé");
                    mViewPager.setAdapter(adapter);
                }catch (NullPointerException e){
                    Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                    Intent accueil = new Intent(getApplicationContext(), accueil.class);
                    finishAffinity();
                    startActivity(accueil);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        recups.addValueEventListener(eventRecup);
    }

    @Override
    protected void onDestroy() {
        recups.removeEventListener(eventRecup);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
