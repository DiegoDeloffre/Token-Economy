package com.example.token_eco;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class gestionListePartageEnvoyes extends RecyclerView.Adapter<gestionListePartageEnvoyes.ViewHolder>{
    private List<creationListePartageEnvoyes> listePartageEnvoyes;
    private Context context;

    public gestionListePartageEnvoyes(List<creationListePartageEnvoyes> listePartageEnvoyes, Context context) {
        this.listePartageEnvoyes = listePartageEnvoyes;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listePartageEnvoyes= layoutInflater.inflate(R.layout.fragment1_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listePartageEnvoyes);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textView.setText(listePartageEnvoyes.get(position).getDescription());
        if(listePartageEnvoyes.get(position).getPhoto_profil_id() == null){
            holder.imageView.setImageResource(R.drawable.avatar);
        }else{
            Picasso.get().load(listePartageEnvoyes.get(position).getPhoto_profil_id()).fit().transform(new RoundedCornersTransform()).into(holder.imageView);
        }
        holder.unSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ad = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Désynchronisation")
                        .setMessage("Cette personne ne pourra plus voir vos enfants êtes vous sûr ?")
                        .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseHandler fh = new FirebaseHandler();
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                DatabaseReference delete = fh.getDatabaseReferenceIUD().child("PeutLireMesEnfants").child(listePartageEnvoyes.get(position).getIud());
                                DatabaseReference delete2 = fh.getDatabaseReference().child(listePartageEnvoyes.get(position).getIud()).child("JePeuxLireSesEnfants").child(mAuth.getCurrentUser().getUid());
                                delete.removeValue();
                                delete2.removeValue();
                            }
                        }).setNegativeButton("NON", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                AlertDialog message = ad.create();
                message.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listePartageEnvoyes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public ImageView unSync;
        public ConstraintLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.partage_frament1_image);
            this.unSync = itemView.findViewById(R.id.image_unsyn_frag1);
            this.textView = itemView.findViewById(R.id.partage_fragment1_text);
            this.relativeLayout = itemView.findViewById(R.id.layout_partage_fragment1);
        }
    }
}