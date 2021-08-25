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

public class enfant extends menu_handler implements NavigationView.OnNavigationItemSelectedListener{

    private List<objectifPourMap> listeObjectifs, listeObjectifsfinis;
    private boolean pasEncoreCree = true;
    private RecyclerView recyclerView;
    private ImageView bravo;
    private TextView nbBravos;
    private ValueEventListener eventEnfantExiste, eventBravo, eventObjectif, eventSync;
    private DatabaseReference enfantExiste, drBravos, drObjectifs, drSync;
    private FirebaseHandler fh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enfant);

        super.setupToolBar();
        fh = new FirebaseHandler();
        enfantExiste = fh.getDatabaseReferenceEnfant(getApplicationContext());
        drSync = fh.getDatabaseReferenceIUD().child("JePeuxLireSesEnfants");
        eventSync = super.estSync(drSync);
        eventEnfantExiste = super.enfantExiste(enfantExiste);
        bravo = findViewById(R.id.imageBravo);
        nbBravos = findViewById(R.id.nbBravosGlobal);
        eventBravo = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String phrase =dataSnapshot.child("prenom").getValue(String.class);
                try {
                    nbBravos.setText(phrase+" a " + dataSnapshot.child("nbBravos").getValue(Integer.class));
                }catch (NullPointerException e){
                    Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                    Intent accueil = new Intent(getApplicationContext(), accueil.class);
                    finishAffinity();
                    startActivity(accueil);
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
        drBravos =fh.getDatabaseReferenceEnfant(this);
        drBravos.addValueEventListener(eventBravo);

        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button button_objectif_creation = findViewById(R.id.button_objectif_creation);
        Button button_recompenses = findViewById(R.id.button_recompenses);
        recyclerView = findViewById(R.id.recyclerViewObjectifs);

        listeObjectifs = new ArrayList<>();
        listeObjectifsfinis = new ArrayList<>();
        afficherObjectifs();

        button_objectif_creation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent page= new Intent(getApplicationContext(), objectif_creation.class);
                startActivity(page);
                //finish();
            }
        });


        button_recompenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent page= new Intent(getApplicationContext(), recompense.class);
                startActivity(page);
                //finish();
            }
        });
    }

    public void afficherObjectifs(){
        drObjectifs = fh.getDatabaseReferenceEnfant(getApplicationContext()).child("objectifs");
        eventObjectif = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listeObjectifs.clear();
                listeObjectifsfinis.clear();
                for(DataSnapshot ds :  dataSnapshot.getChildren()){
                    if(ds.child("jetonsAObtenir").getValue() != null && ds.child("jetonsObtenus").getValue() != null && ds.child("titre").getValue() != null) {
                        String url = ds.child("imageJeton").getValue(String.class);
                        try {
                            int jetonsAObtenir = ds.child("jetonsAObtenir").getValue(Integer.class);
                            int jetonsObtenus = ds.child("jetonsObtenus").getValue(Integer.class);
                            if(jetonsObtenus == jetonsAObtenir){
                                listeObjectifsfinis.add(new objectifPourMap(ds.getKey(), ds.child("titre").getValue(String.class),jetonsAObtenir, jetonsObtenus, url));
                            }else{
                                listeObjectifs.add(new objectifPourMap(ds.getKey(), ds.child("titre").getValue(String.class), jetonsAObtenir, jetonsObtenus, url));
                            }
                        }catch (NullPointerException e){
                            Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                            Intent accueil = new Intent(getApplicationContext(), accueil.class);
                            finishAffinity();
                            startActivity(accueil);
                        }
                    }
                }
                List<objectifPourMap> myListData = new ArrayList<>();
                for(objectifPourMap o : listeObjectifs){
                    myListData.add(o);
                }
                for(objectifPourMap o : listeObjectifsfinis){
                    myListData.add(o);
                }
                if(pasEncoreCree) {
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(enfant.this));
                    recyclerView.addItemDecoration(new EspaceInterItemListe(30));
                    pasEncoreCree=false;
                }
                gestionListeObjectif adapter = new gestionListeObjectif(myListData, enfant.this, listeObjectifs.size());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        drObjectifs.addValueEventListener(eventObjectif);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        drBravos.removeEventListener(eventBravo);
        drObjectifs.removeEventListener(eventObjectif);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        drBravos.removeEventListener(eventBravo);
        drObjectifs.removeEventListener(eventObjectif);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        drBravos.addValueEventListener(eventBravo);
        drObjectifs.addValueEventListener(eventObjectif);
        enfantExiste.addValueEventListener(eventEnfantExiste);
        drSync.addValueEventListener(eventSync);
    }
}


