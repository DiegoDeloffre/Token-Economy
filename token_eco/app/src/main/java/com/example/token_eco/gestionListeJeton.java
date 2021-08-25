package com.example.token_eco;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class gestionListeJeton extends RecyclerView.Adapter<gestionListeJeton.ViewHolder> {
    private int nbJetonsObtenus, jetonsAObtenir;
    private String ImgageJeton, id;
    private Context contexte;
    private DatabaseReference gestionJetons;

    public gestionListeJeton(Context contexte, String ImgageJeton, int nbJetonsObtenus, int jetonsAObtenir, String id){
        this.contexte=contexte;
        this.ImgageJeton = ImgageJeton;
        this.nbJetonsObtenus=nbJetonsObtenus;
        this.id=id;
        this.jetonsAObtenir=jetonsAObtenir;
        FirebaseHandler fh = new FirebaseHandler();
        this.gestionJetons = fh.getDatabaseReferenceEnfant(contexte);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listeJetons= layoutInflater.inflate(R.layout.jetons_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listeJetons);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position){
        if(ImgageJeton == null){
            Picasso.get().load(R.drawable.jeton).into(holder.jeton);
        }else if (ImgageJeton.equals("image1")){
            Picasso.get().load(R.drawable.picto1).into(holder.jeton);
        }else if (ImgageJeton.equals("image2")){
            Picasso.get().load(R.drawable.picto2).into(holder.jeton);
        }else if (ImgageJeton.equals("image3")){
            Picasso.get().load(R.drawable.picto3).into(holder.jeton);
        }else if (ImgageJeton.equals("image4")){
            Picasso.get().load(R.drawable.picto4).into(holder.jeton);
        }else if (ImgageJeton.equals("image5")){
            Picasso.get().load(R.drawable.picto5).into(holder.jeton);
        }else if (ImgageJeton.equals("image6")){
            Picasso.get().load(R.drawable.picto6).into(holder.jeton);
        }else if (ImgageJeton.equals("image7")){
            Picasso.get().load(R.drawable.picto7).into(holder.jeton);
        }else if (ImgageJeton.equals("image8")){
            Picasso.get().load(R.drawable.picto8).into(holder.jeton);
        }else{
            Picasso.get().load(ImgageJeton).fit().into(holder.jeton);
        }

        if(position+1>nbJetonsObtenus){
            holder.jeton.setAlpha(0.3f);
        }
        holder.jeton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gestionJetons.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int nbBravosMax = 5;
                        int nbBravos = 0;
                        try{
                            nbBravos = dataSnapshot.child("nbBravos").getValue(Integer.class);
                            int valeur = 0;
                            for(DataSnapshot ds :  dataSnapshot.child("recompenses").getChildren()){
                                if(ds.child("coutBravos").getValue()!= null && ds.child("estReclamee").getValue() != null){
                                    if(ds.child("coutBravos").getValue(Integer.class)>valeur && !ds.child("estReclamee").getValue(Boolean.class)){
                                        valeur = ds.child("coutBravos").getValue(Integer.class);
                                        nbBravosMax = valeur;
                                        if(valeur == 5){
                                            break;
                                        }
                                    }
                                }
                            }
                        }catch (NullPointerException e){
                            Toast.makeText(contexte , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                            Intent accueil = new Intent(contexte, accueil.class);
                            ((objectif_detail)contexte).finishAffinity();
                            contexte.startActivity(accueil);
                        }
                        if(jetonsAObtenir != nbJetonsObtenus) {
                            if (position + 1 > nbJetonsObtenus) {
                                nbJetonsObtenus++;
                                if (nbJetonsObtenus == jetonsAObtenir) {
                                    if (nbBravos < nbBravosMax) {
                                        nbBravos++;
                                        gestionJetons.child("objectifs").child(id).child("jetonsObtenus").setValue(nbJetonsObtenus);
                                        gestionJetons.child("nbBravos").setValue(nbBravos);
                                        AlertDialog.Builder ad = new AlertDialog.Builder(contexte,R.style.Theme_AppCompat_Light_Dialog_Alert)
                                                .setTitle("que faire")
                                                .setMessage("Il faut faire quoi ?")
                                                .setPositiveButton("remettre objectif à 0", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        gestionJetons.child("objectifs").child(id).child("jetonsObtenus").setValue(0);
                                                    }
                                                }).setNegativeButton("laisser objectif comme fini", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                    }
                                                });
                                        AlertDialog message = ad.create();
                                        message.setCancelable(false);
                                        message.show();
                                    } else {
                                        if(nbBravosMax == 5){
                                            AlertDialog.Builder ad = new AlertDialog.Builder(contexte,R.style.Theme_AppCompat_Light_Dialog_Alert)
                                                    .setTitle("Trop de bravos")
                                                    .setMessage("le nombre maximum de bravos est atteint, il faut en dépenser")
                                                    .setPositiveButton("recompenses", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent rec = new Intent(contexte, recompense.class);
                                                            contexte.startActivity(rec);
                                                            ((objectif_detail)contexte).finish();
                                                        }
                                                    });
                                            AlertDialog message = ad.create();
                                            message.setCancelable(false);
                                            message.show();
                                        }else{
                                            AlertDialog.Builder ad = new AlertDialog.Builder(contexte,R.style.Theme_AppCompat_Light_Dialog_Alert)
                                                    .setTitle("Un cadeau est disponible")
                                                    .setMessage("le cadeau le plus cher disponible peut etre accepté !")
                                                    .setPositiveButton("recompenses", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent rec = new Intent(contexte, recompense.class);
                                                            contexte.startActivity(rec);
                                                            ((objectif_detail)contexte).finish();
                                                        }
                                                    });
                                            AlertDialog message = ad.create();
                                            message.setCancelable(false);
                                            message.show();
                                        }
                                    }
                                } else {
                                    gestionJetons.child("objectifs").child(id).child("jetonsObtenus").setValue(nbJetonsObtenus);
                                }
                            } else {
                                nbJetonsObtenus--;
                                gestionJetons.child("objectifs").child(id).child("jetonsObtenus").setValue(nbJetonsObtenus);
                            }
                        }else {
                            Toast.makeText(contexte, "l'objectif est fini", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return jetonsAObtenir;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView jeton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jeton = itemView.findViewById(R.id.imageView_jeton);
        }
    }
}
