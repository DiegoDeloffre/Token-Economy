package com.example.token_eco;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class recompense extends menu_handler implements NavigationView.OnNavigationItemSelectedListener{
    private List<creationListeCadeau> listeCadeaux, listeCadeauxReclames;
    private DatabaseReference enfantExiste, recompenses, drBravos, drSync;
    private ValueEventListener eventEnfantExiste, eventRecompences, eventSync, eventBravos;
    private boolean pasEncoreCree = true;
    private RecyclerView recyclerView;
    private ImageView bravo;
    private TextView nbBravos;
    private FirebaseHandler fh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recompense);

        super.setupToolBar();
        fh = new FirebaseHandler();
        enfantExiste = fh.getDatabaseReferenceEnfant(getApplicationContext());
        eventEnfantExiste = super.enfantExiste(enfantExiste);
        drSync = fh.getDatabaseReferenceIUD().child("JePeuxLireSesEnfants");
        eventSync = super.estSync(drSync);
        bravo = findViewById(R.id.imageBravo2);
        listeCadeaux = new ArrayList<>();
        listeCadeauxReclames = new ArrayList<>();
        afficherRecompences();

        Button button_recompense_creation = findViewById(R.id.button_recompense_creation);
        recyclerView = findViewById(R.id.recyclerViewCadeaux);

        button_recompense_creation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent page= new Intent(getApplicationContext(), recompense_creation.class);
                startActivity(page);
            }
        });

        nbBravos = findViewById(R.id.nbBravosGlobal2);
        drBravos =  fh.getDatabaseReferenceEnfant(this);
        eventBravos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String phrase = dataSnapshot.child("prenom").getValue(String.class);
                    if (Integer.toString(dataSnapshot.child("nbBravos").getValue(Integer.class)) != null) {
                        nbBravos.setText(phrase + " a " + Integer.toString(dataSnapshot.child("nbBravos").getValue(Integer.class)));
                    }
                }catch (NullPointerException e){
                    //a remplir
                }
                if (dataSnapshot.child("img_bravo").exists()){
                    String s= dataSnapshot.child("img_bravo").getValue(String.class);
                    if(s == null){
                        Picasso.get().load(R.drawable.jeton).into(bravo);
                    }else if (s.equals("image1")){
                        Picasso.get().load(R.drawable.bravo1).into(bravo);
                    }else if (s.equals("image2")){
                        Picasso.get().load(R.drawable.bravo2).into(bravo);
                    }else if (s.equals("image3")){
                        Picasso.get().load(R.drawable.bravo3).into(bravo);
                    }else if (s.equals("image4")){
                        Picasso.get().load(R.drawable.bravo4).into(bravo);
                    }else{
                        Picasso.get().load(s).fit().into(bravo);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        drBravos.addValueEventListener(eventBravos);
        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void afficherRecompences(){
        recompenses = fh.getDatabaseReferenceEnfant(getApplicationContext());
        eventRecompences = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listeCadeaux.clear();
                listeCadeauxReclames.clear();
                String  url_bravo = dataSnapshot.child("img_bravo").getValue(String.class);
                for(DataSnapshot ds :  dataSnapshot.child("recompenses").getChildren()){
                    if(ds.child("coutBravos").getValue() != null && ds.child("estReclamee").getValue() != null && ds.getKey() !=null){
                        try {
                            String url = ds.child("image").getValue(String.class);
                            if(ds.child("estReclamee").getValue(Boolean.class)){
                                listeCadeauxReclames.add(new creationListeCadeau(ds.child("titre").getValue(String.class), ds.getKey(), ds.child("coutBravos").getValue(Integer.class), url ));
                            }else{
                                listeCadeaux.add(new creationListeCadeau(ds.child("titre").getValue(String.class), ds.getKey(), ds.child("coutBravos").getValue(Integer.class), url));
                            }
                        }catch (NullPointerException e){
                            Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                            Intent accueil = new Intent(getApplicationContext(), accueil.class);
                            finishAffinity();
                            startActivity(accueil);
                        }
                    }
                }
                List<creationListeCadeau> myListData = new ArrayList<creationListeCadeau>();
                for(creationListeCadeau c : listeCadeaux){
                    myListData.add(c);
                }
                for(creationListeCadeau c : listeCadeauxReclames){
                    myListData.add(c);
                }
                if(pasEncoreCree){
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(recompense.this));
                    recyclerView.addItemDecoration(new EspaceInterItemListe(30));
                    pasEncoreCree = false;

                }
                gestionListeCadeau adapter = new gestionListeCadeau(myListData, recompense.this, listeCadeaux.size(), url_bravo);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        recompenses.addValueEventListener(eventRecompences);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        drBravos.removeEventListener(eventBravos);
        recompenses.removeEventListener(eventRecompences);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        drBravos.removeEventListener(eventBravos);
        recompenses.removeEventListener(eventRecompences);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        drBravos.addValueEventListener(eventBravos);
        recompenses.addValueEventListener(eventRecompences);
        enfantExiste.addValueEventListener(eventEnfantExiste);
        drSync.addValueEventListener(eventSync);
    }
}
