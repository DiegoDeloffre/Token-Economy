package com.example.token_eco;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class gestionListeNotification extends RecyclerView.Adapter<gestionListeNotification.ViewHolder> {
    private List<creationListeNotification> notifications;
    private FirebaseHandler fh;

    public gestionListeNotification(List<creationListeNotification> notifications) {
        this.notifications = notifications;
        this.fh = new FirebaseHandler();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listeNotif = layoutInflater.inflate(R.layout.notification_item, parent, false);
        ViewHolder viewHolder = new gestionListeNotification.ViewHolder(listeNotif);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.notif.setText(notifications.get(position).getNomPrenom() + " souhaite partager le suivis de ses enfants avec vous, accepter ?");

        holder.refuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fh.getDatabaseReferenceIUD().child("notifications").child(notifications.get(position).getIud()).removeValue();
            }
        });

        holder.valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fh.getDatabaseReferenceIUD().child("notifications").child(notifications.get(position).getIud()).removeValue();
                DatabaseReference JePeuxLireSesEnfants = fh.getDatabaseReferenceIUD();
                JePeuxLireSesEnfants.child("JePeuxLireSesEnfants").child(notifications.get(position).getIud()).setValue(notifications.get(position).getNomPrenom());
                final DatabaseReference PeutLireMesEnfants = fh.getDatabaseReference().child(notifications.get(position).getIud()).child("PeutLireMesEnfants");
                JePeuxLireSesEnfants.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        String nom = dataSnapshot.child("nom").getValue(String.class);
                        String prenom = dataSnapshot.child("prenom").getValue(String.class);
                        PeutLireMesEnfants.child(mAuth.getCurrentUser().getUid()).setValue(nom + " " + prenom);
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
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView notif;
        public ImageView valider;
        public ImageView refuser;

        public ViewHolder(View itemView) {
            super(itemView);
            this.notif = itemView.findViewById(R.id.notification_texte);
            this.valider = itemView.findViewById(R.id.accepter_notification);
            this.refuser = itemView.findViewById(R.id.refuser_notification);
        }
    }
}
