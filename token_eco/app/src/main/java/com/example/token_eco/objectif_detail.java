package com.example.token_eco;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class objectif_detail extends menu_handler implements NavigationView.OnNavigationItemSelectedListener{

    private TextView titre, description, nombreJetons;
    private String idO;
    private Boolean pasEncoreCree = true;
    private ImageView imageJeton;
    private Button reactiver, supprimer, modification;
    private DatabaseReference objectif, enfantExiste,drSync;
    private ValueEventListener eventEnfantExiste, eventObjecif, eventSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objectif_detail);

        titre = findViewById(R.id.titre_objectif);
        description = findViewById(R.id.description_objectif);
        nombreJetons = findViewById(R.id.nbjetons_objectif);
        reactiver = findViewById(R.id.button_reactiver_objetif);
        supprimer =findViewById(R.id.button_supprimer_objectif);
        imageJeton = findViewById(R.id.image_jeton_objectif);
        idO = getIntent().getStringExtra("id");

        super.setupToolBar();
        FirebaseHandler fh = new FirebaseHandler();
        enfantExiste = fh.getDatabaseReferenceEnfant(getApplicationContext());
        eventEnfantExiste = super.enfantExiste(enfantExiste);
        drSync = fh.getDatabaseReferenceIUD().child("JePeuxLireSesEnfants");
        eventSync = super.estSync(drSync);
        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        objectif = fh.getDatabaseReferenceEnfant(getApplicationContext()).child("objectifs").child(idO);
        modification = findViewById(R.id.button_modifier_objectif);

        modification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent modif = new Intent(objectif_detail.this, objectif_modification.class);
                modif.putExtra("id", idO);
                startActivity(modif);
            }
        });

        reactiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                objectif.child("jetonsObtenus").setValue(0);
            }
        });

        supprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                objectif.removeValue();
                deleteImage();
            }
        });
        
        afficherInfos();
    }

    public void afficherInfos(){
        eventObjecif = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Toast.makeText(getApplicationContext() , "L'objectif a été supprimé", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                int jetonsObtenus = 0;
                int jetonsAObtenir = 0;
                try {
                    titre.setText(dataSnapshot.child("titre").getValue(String.class));
                    description.setText(dataSnapshot.child("description").getValue(String.class));
                    jetonsObtenus = dataSnapshot.child("jetonsObtenus").getValue(Integer.class);
                    jetonsAObtenir = dataSnapshot.child("jetonsAObtenir").getValue(Integer.class);
                    nombreJetons.setText(Integer.toString(jetonsAObtenir));
                }catch (NullPointerException e){
                    Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                    Intent accueil = new Intent(getApplicationContext(), accueil.class);
                    finishAffinity();
                    startActivity(accueil);
                }
                String s = dataSnapshot.child("imageJeton").getValue(String.class);
                if(s == null){
                    Picasso.get().load(R.drawable.jeton).into(imageJeton);
                }else if (s.equals("image1")){
                    Picasso.get().load(R.drawable.picto1).into(imageJeton);
                }else if (s.equals("image2")){
                    Picasso.get().load(R.drawable.picto2).into(imageJeton);
                }else if (s.equals("image3")){
                    Picasso.get().load(R.drawable.picto3).into(imageJeton);
                }else if (s.equals("image4")){
                    Picasso.get().load(R.drawable.picto4).into(imageJeton);
                }else if (s.equals("image5")){
                    Picasso.get().load(R.drawable.picto5).into(imageJeton);
                }else if (s.equals("image6")){
                    Picasso.get().load(R.drawable.picto6).into(imageJeton);
                }else if (s.equals("image7")){
                    Picasso.get().load(R.drawable.picto7).into(imageJeton);
                }else if (s.equals("image8")){
                    Picasso.get().load(R.drawable.picto8).into(imageJeton);
                }else{
                    Picasso.get().load(s).fit().into(imageJeton);
                }
                RecyclerView recyclerView = findViewById(R.id.recyclerViewJetons);
                if(pasEncoreCree){
                    recyclerView.setLayoutManager(new GridLayoutManager(objectif_detail.this, 5));
                    pasEncoreCree = false;
                }
                gestionListeJeton adapter = new gestionListeJeton(objectif_detail.this, s, jetonsObtenus, jetonsAObtenir, idO);
                recyclerView.setAdapter(adapter);
                if(jetonsAObtenir==jetonsObtenus){
                    reactiver.setVisibility(View.VISIBLE);
                    supprimer.setVisibility(View.VISIBLE);
                    modification.setVisibility(View.INVISIBLE);
                }
                if(jetonsObtenus==0){
                    reactiver.setVisibility(View.INVISIBLE);
                    supprimer.setVisibility(View.INVISIBLE);
                    modification.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        objectif.addValueEventListener(eventObjecif);
    }

    public void deleteImage(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref;
        pref = this.getSharedPreferences("pref", MODE_PRIVATE);
        String enfant = pref.getString("enfant", "no data");
        FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("objectifs").child(idO).child("imageJeton").child("imageJeton").delete();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        objectif.removeEventListener(eventObjecif);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        objectif.removeEventListener(eventObjecif);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        objectif.addValueEventListener(eventObjecif);
        enfantExiste.addValueEventListener(eventEnfantExiste);
        drSync.addValueEventListener(eventSync);
    }
}
