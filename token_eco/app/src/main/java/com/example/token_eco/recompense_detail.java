package com.example.token_eco;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class recompense_detail extends menu_handler implements NavigationView.OnNavigationItemSelectedListener{
    private ValueEventListener eventEnfantExiste, eventRecompense, eventSync, eventImage;
    private DatabaseReference recompense, drSync, enfantExiste, image;
    private Button button_recuperer_recompense, button_reactiver_recompense,button_supprimer_recompense, button_modifier_recompense;
    private ImageView bravo1, bravo2, bravo3, bravo4, bravo5, imageRecompense, bravoSeul;
    private int coutRecompense, nbBravos;
    private TextView titre, cout;
    private String idR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recompense_detail);
        button_recuperer_recompense = findViewById(R.id.button_recuperer_recompense);
        button_supprimer_recompense = findViewById(R.id.button_supprimer_recompense);
        button_reactiver_recompense = findViewById(R.id.button_reactiver_recompense);
        button_modifier_recompense = findViewById(R.id.button_modifier_recompense);

        bravo1 = findViewById(R.id.detail_bravo1);
        bravo2 = findViewById(R.id.detail_bravo2);
        bravo3 = findViewById(R.id.detail_bravo3);
        bravo4 = findViewById(R.id.detail_bravo4);
        bravo5 = findViewById(R.id.detail_bravo5);
        bravoSeul = findViewById(R.id.bravo);
        titre = findViewById(R.id.titre_recompense);
        cout = findViewById(R.id.cout_recompense);
        imageRecompense = findViewById(R.id.image_recompense);
        idR = getIntent().getStringExtra("id");
        super.setupToolBar();

        FirebaseHandler fh = new FirebaseHandler();
        enfantExiste = fh.getDatabaseReferenceEnfant(getApplicationContext());
        eventEnfantExiste = super.enfantExiste(enfantExiste);
        drSync = fh.getDatabaseReferenceIUD().child("JePeuxLireSesEnfants");
        eventSync = super.estSync(drSync);
        recompense = fh.getDatabaseReferenceEnfant(getApplicationContext());
        image =fh.getDatabaseReferenceEnfant(getApplicationContext()).child("recompenses").child(idR).child("image");

        afficherRecompense();
        afficherImage();
        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        button_recuperer_recompense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recompense.child("nbBravos").setValue(nbBravos-coutRecompense);
                recompense.child("recompenses").child(idR).child("estReclamee").setValue(true);
            }
        });

        button_reactiver_recompense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recompense.child("recompenses").child(idR).child("estReclamee").setValue(false);
                button_reactiver_recompense.setVisibility(View.INVISIBLE);
                button_supprimer_recompense.setVisibility(View.INVISIBLE);
                button_modifier_recompense.setVisibility(View.VISIBLE);
            }
        });

        button_supprimer_recompense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recompense.child("recompenses").child(idR).removeValue();
                deleteRecompense();
                //finish();
            }
        });

        button_modifier_recompense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent modif = new Intent(recompense_detail.this, recompense_modification.class);
                modif.putExtra("id", idR);
                startActivity(modif);
            }
        });




        DatabaseReference refBravos = fh.getDatabaseReferenceEnfant(getApplicationContext()).child("img_bravo");
        refBravos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = dataSnapshot.getValue(String.class);
                    switch (s) {
                        case "image1":
                            Picasso.get().load(R.drawable.bravo1).into(bravo1);
                            Picasso.get().load(R.drawable.bravo1).into(bravo2);
                            Picasso.get().load(R.drawable.bravo1).into(bravo3);
                            Picasso.get().load(R.drawable.bravo1).into(bravo4);
                            Picasso.get().load(R.drawable.bravo1).into(bravo5);
                            Picasso.get().load(R.drawable.bravo1).into(bravoSeul);
                            break;
                        case "image2":
                            Picasso.get().load(R.drawable.bravo2).into(bravo1);
                            Picasso.get().load(R.drawable.bravo2).into(bravo2);
                            Picasso.get().load(R.drawable.bravo2).into(bravo3);
                            Picasso.get().load(R.drawable.bravo2).into(bravo4);
                            Picasso.get().load(R.drawable.bravo2).into(bravo5);
                            Picasso.get().load(R.drawable.bravo2).into(bravoSeul);
                            break;
                        case "image3":
                            Picasso.get().load(R.drawable.bravo3).into(bravo1);
                            Picasso.get().load(R.drawable.bravo3).into(bravo2);
                            Picasso.get().load(R.drawable.bravo3).into(bravo3);
                            Picasso.get().load(R.drawable.bravo3).into(bravo4);
                            Picasso.get().load(R.drawable.bravo3).into(bravo5);
                            Picasso.get().load(R.drawable.bravo3).into(bravoSeul);
                            break;
                        case "image4":
                            Picasso.get().load(R.drawable.bravo4).into(bravo1);
                            Picasso.get().load(R.drawable.bravo4).into(bravo2);
                            Picasso.get().load(R.drawable.bravo4).into(bravo3);
                            Picasso.get().load(R.drawable.bravo4).into(bravo4);
                            Picasso.get().load(R.drawable.bravo4).into(bravo5);
                            Picasso.get().load(R.drawable.bravo4).into(bravoSeul);
                            break;
                        default:
                            Picasso.get().load(s).fit().into(bravo1);
                            Picasso.get().load(s).fit().into(bravo2);
                            Picasso.get().load(s).fit().into(bravo3);
                            Picasso.get().load(s).fit().into(bravo4);
                            Picasso.get().load(s).fit().into(bravo5);
                            Picasso.get().load(s).fit().into(bravoSeul);
                            break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void afficherImage() {
        eventImage = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String s = dataSnapshot.getValue(String.class);
                    if (s == null) {
                        Picasso.get().load(R.drawable.cadeau).into(imageRecompense);
                    } else if (s.equals("image1")) {
                        Picasso.get().load(R.drawable.cadeau1).into(imageRecompense);
                    } else if (s.equals("image2")) {
                        Picasso.get().load(R.drawable.cadeau2).into(imageRecompense);
                    } else if (s.equals("image3")) {
                        Picasso.get().load(R.drawable.cadeau3).into(imageRecompense);
                    } else if (s.equals("image4")) {
                        Picasso.get().load(R.drawable.cadeau4).into(imageRecompense);
                    } else if (s.equals("image5")) {
                        Picasso.get().load(R.drawable.cadeau5).into(imageRecompense);
                    } else if (s.equals("image6")) {
                        Picasso.get().load(R.drawable.cadeau6).into(imageRecompense);
                    } else if (s.equals("image7")) {
                        Picasso.get().load(R.drawable.cadeau7).into(imageRecompense);
                    } else if (s.equals("image8")) {
                        Picasso.get().load(R.drawable.cadeau8).fit().into(imageRecompense);
                    } else {
                        Picasso.get().load(s).fit().into(imageRecompense);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(recompense_detail.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        image.addValueEventListener(eventImage);
    }
    private void afficherRecompense(){
        eventRecompense = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("recompenses").child(idR).exists()){
                    Toast.makeText(getApplicationContext() , "La recompense a été supprimée", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                try {
                    titre.setText(dataSnapshot.child("recompenses").child(idR).child("titre").getValue(String.class));
                    coutRecompense = dataSnapshot.child("recompenses").child(idR).child("coutBravos").getValue(Integer.class);
                    cout.setText(Integer.toString(coutRecompense));
                    nbBravos = dataSnapshot.child("nbBravos").getValue(Integer.class);
                    switch (nbBravos) {
                        case 1:
                            if (coutRecompense > 3) {
                                bravo1.setAlpha(1.0f);
                            } else if (coutRecompense != 1){
                                bravo2.setAlpha(1.0f);
                            }else{
                                bravo3.setAlpha(1.0f);
                            }
                            cacherBravo(coutRecompense);
                            break;
                        case 2:
                            if (coutRecompense > 3) {
                                bravo1.setAlpha(1.0f);
                            } else{
                                bravo3.setAlpha(1.0f);
                            }
                            bravo2.setAlpha(1.0f);
                            cacherBravo(coutRecompense);
                            break;
                        case 3:
                            if (coutRecompense > 3) {
                                bravo1.setAlpha(1.0f);
                            } else{
                                bravo4.setAlpha(1.0f);
                            }
                            bravo2.setAlpha(1.0f);
                            bravo3.setAlpha(1.0f);
                            cacherBravo(coutRecompense);
                            break;
                        case 4:
                            bravo1.setAlpha(1.0f);
                            bravo2.setAlpha(1.0f);
                            bravo3.setAlpha(1.0f);
                            bravo4.setAlpha(1.0f);
                            cacherBravo(coutRecompense);
                            break;
                        case 5:
                            bravo1.setAlpha(1.0f);
                            bravo2.setAlpha(1.0f);
                            bravo3.setAlpha(1.0f);
                            bravo4.setAlpha(1.0f);
                            bravo5.setAlpha(1.0f);
                            cacherBravo(coutRecompense);
                            break;
                        default:
                            bravo1.setAlpha(.5f);
                            bravo2.setAlpha(.5f);
                            bravo3.setAlpha(.5f);
                            bravo4.setAlpha(.5f);
                            bravo5.setAlpha(.5f);
                            cacherBravo(coutRecompense);
                            break;
                    }
                    if(coutRecompense<=nbBravos){
                        button_modifier_recompense.setVisibility(View.INVISIBLE);
                        button_recuperer_recompense.setVisibility(View.VISIBLE);
                    }
                    if (dataSnapshot.child("recompenses").child(idR).child("estReclamee").getValue(Boolean.class)){
                        button_reactiver_recompense.setVisibility(View.VISIBLE);
                        button_supprimer_recompense.setVisibility(View.VISIBLE);
                        button_recuperer_recompense.setVisibility(View.INVISIBLE);
                    }
                }catch (NullPointerException e){
                    Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                    Intent accueil = new Intent(getApplicationContext(), accueil.class);
                    finishAffinity();
                    startActivity(accueil);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        recompense.addValueEventListener(eventRecompense);
    }

    public void cacherBravo(int coutRencomp){
        bravo1.setVisibility(View.VISIBLE);
        bravo2.setVisibility(View.VISIBLE);
        bravo4.setVisibility(View.VISIBLE);
        bravo5.setVisibility(View.VISIBLE);
        if(coutRencomp < 5){
            bravo5.setVisibility(View.GONE);
            if(coutRencomp <4){
                bravo1.setVisibility(View.GONE);
                if(coutRencomp <3){
                    bravo4.setVisibility(View.GONE);
                    if(coutRencomp <2){
                        bravo2.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void deleteRecompense(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref;
        pref = this.getSharedPreferences("pref", MODE_PRIVATE);
        String enfant = pref.getString("enfant", "no data");
        FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("recompenses").child(idR).child("image").child("recompense").delete();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        recompense.removeEventListener(eventRecompense);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        image.removeEventListener(eventImage);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        recompense.removeEventListener(eventRecompense);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        image.removeEventListener(eventImage);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recompense.addValueEventListener(eventRecompense);
        enfantExiste.addValueEventListener(eventEnfantExiste);
        drSync.addValueEventListener(eventSync);
        image.addValueEventListener(eventImage);
    }
}
