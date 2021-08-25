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

public class gestionListePartageRecus extends RecyclerView.Adapter<gestionListePartageRecus.ViewHolder>{

    private Context context;
    private List<creationListePartageRecus> listePartageRecus;

    public gestionListePartageRecus(List<creationListePartageRecus> listePartageRecus, Context context) {
        this.listePartageRecus = listePartageRecus;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listePartageRecus= layoutInflater.inflate(R.layout.fragment2_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listePartageRecus);
        return new ViewHolder(listePartageRecus);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textView.setText(listePartageRecus.get(position).getDescription());
        if(listePartageRecus.get(position).getPhoto_profil_id() == null){
            holder.imageView.setImageResource(R.drawable.avatar);
        }else{
            Picasso.get().load(listePartageRecus.get(position).getPhoto_profil_id()).fit().transform(new RoundedCornersTransform()).into(holder.imageView);
        }

        holder.unSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ad = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Désynchronisation")
                        .setMessage("Vous ne pourrez plus voir les enfants de cette personne, êtes vous sûr ?")
                        .setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseHandler fh = new FirebaseHandler();
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                DatabaseReference delete = fh.getDatabaseReferenceIUD().child("JePeuxLireSesEnfants").child(listePartageRecus.get(position).getIUD());
                                DatabaseReference delete2 = fh.getDatabaseReference().child(listePartageRecus.get(position).getIUD()).child("PeutLireMesEnfants").child(mAuth.getCurrentUser().getUid());
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
        return listePartageRecus.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageView unSync;
        public TextView textView;
        public ConstraintLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.partage_frament2_image);
            this.unSync =  itemView.findViewById(R.id.image_unsyn_frag2);
            this.textView =  itemView.findViewById(R.id.partage_fragment2_text);
            this.relativeLayout = itemView.findViewById(R.id.layout_partage_fragment2);
        }
    }
}
