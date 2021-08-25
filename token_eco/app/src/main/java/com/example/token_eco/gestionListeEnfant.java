package com.example.token_eco;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;

public class gestionListeEnfant extends RecyclerView.Adapter<gestionListeEnfant.ViewHolder>{
    private List<creationListeEnfant> listeEnfants;
    private Context contexte;

    public gestionListeEnfant(List<creationListeEnfant> myListData, Context contexte) {
        this.contexte=contexte;
        this.listeEnfants = myListData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listeEnfant= layoutInflater.inflate(R.layout.enfant_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listeEnfant);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String userIUD = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(userIUD.equals(listeEnfants.get(position).getIud())){
            if(listeEnfants.get(position).getSurnom() != null){
                holder.textView.setText(listeEnfants.get(position).getSurnom());
            }else{
                holder.textView.setText(listeEnfants.get(position).getPrenom());
            }
            holder.textViewPartage.setText(null);
        }else{
            FirebaseHandler fh = new FirebaseHandler();
            DatabaseReference responsable = fh.getDatabaseReference().child(listeEnfants.get(position).getIud());
            responsable.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot ds) {
                    String tmp = listeEnfants.get(position).getPrenom();
                    String tmp2 = "Partagé par " + ds.child("nom").getValue(String.class)+" "+ds.child("prenom").getValue(String.class);
                    holder.textView.setText(tmp);
                    holder.textViewPartage.setText(tmp2);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
        if(listeEnfants.get(position).getPhoto_profil_id() == null){
            holder.imageView.setImageResource(R.drawable.avatar);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image1")){
            Picasso.get().load(R.drawable.enfant1).into(holder.imageView);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image2")){
            Picasso.get().load(R.drawable.enfant2).into(holder.imageView);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image3")){
            Picasso.get().load(R.drawable.enfant3).into(holder.imageView);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image4")){
            Picasso.get().load(R.drawable.enfant4).into(holder.imageView);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image5")){
            Picasso.get().load(R.drawable.enfant5).into(holder.imageView);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image6")){
            Picasso.get().load(R.drawable.enfant6).into(holder.imageView);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image7")){
            Picasso.get().load(R.drawable.enfant7).into(holder.imageView);
        }else if (listeEnfants.get(position).getPhoto_profil_id().equals("image8")){
            Picasso.get().load(R.drawable.enfant8).into(holder.imageView);
        }else{
            Picasso.get().load(listeEnfants.get(position).getPhoto_profil_id()).fit().transform(new RoundedCornersTransform()).into(holder.imageView);
        }

        holder.modif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = contexte.getSharedPreferences("pref", MODE_PRIVATE);
                pref.edit().putString("enfant", listeEnfants.get(position).getIdEnfant()).apply();
                pref.edit().putString("iud", listeEnfants.get(position).getIud()).apply();
                Intent objectifs = new Intent(contexte, enfant_modification.class);
                contexte.startActivity(objectifs);
                //((accueil)contexte).finish();
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ui = new AlertDialog.Builder(contexte, R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Que faire ?")
                        .setMessage("Enlever " + listeEnfants.get(position).getPrenom()+" ?")
                        .setPositiveButton("oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseHandler fh = new FirebaseHandler();
                                fh.getDatabaseReference().child(listeEnfants.get(position).getIud()).child("enfants").child(listeEnfants.get(position).getIdEnfant()).removeValue();
                                deleteImages(listeEnfants.get(position).getIdEnfant());
                            }
                        }).setNegativeButton("non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                AlertDialog message = ui.create();
                message.show();
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = contexte.getSharedPreferences("pref", MODE_PRIVATE);
                pref.edit().putString("enfant", listeEnfants.get(position).getIdEnfant()).apply();
                pref.edit().putString("iud", listeEnfants.get(position).getIud()).apply();
                Intent objectifs = new Intent(contexte, enfant.class);
                contexte.startActivity(objectifs);
                //((accueil)contexte).finish();
            }
        });
    }

    private void deleteImages(String enfant){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        StorageReference mStoRefObj = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("objectifs");
        //objectifs
        mStoRefObj.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    prefix.child("imageJeton").child("imageJeton").delete();

                }
            }
        });
        //recompenses
        StorageReference mStoRefRec = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("recompenses");
        mStoRefRec.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    prefix.child("image").child("recompense").delete();

                }
            }
        });
        //avatar
        FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("avatar").delete();
        //imgBravo
        FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("img_bravo").child("img_bravo").delete();
    }

    @Override
    public int getItemCount() {
        return listeEnfants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView textViewPartage;
        public ImageView modif;
        public ImageView delete;
        public ConstraintLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.photo_enfant);
            this.textView =  itemView.findViewById(R.id.prenom_enfant);
            this.modif =  itemView.findViewById(R.id.modifier_enfant);
            this.delete =  itemView.findViewById(R.id.supprimer_enfant);
            this.textViewPartage = itemView.findViewById(R.id.textViewPartage);
            this.relativeLayout = itemView.findViewById(R.id.layout_item_enfant);
        }
    }
}
