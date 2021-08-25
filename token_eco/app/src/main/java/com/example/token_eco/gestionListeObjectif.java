package com.example.token_eco;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class gestionListeObjectif extends RecyclerView.Adapter<gestionListeObjectif.ViewHolder>{
    private List<objectifPourMap> listeObjectifs;
    private Context contexte;
    private int nbFinis;

    public gestionListeObjectif(List<objectifPourMap> listeEnfants, Context contexte, int nbFinis) {
        this.contexte=contexte;
        this.listeObjectifs = listeEnfants;
        this.nbFinis = nbFinis;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listeEnfant= layoutInflater.inflate(R.layout.objectif_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listeEnfant);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.intitule.setText(listeObjectifs.get(position).getTitre());
        holder.nbJetonEnCours.setText(listeObjectifs.get(position).getJetonsObtenus());
        holder.nbJetonTotal.setText(listeObjectifs.get(position).getJetonsAObtenir());
        if(listeObjectifs.get(position).getUrlImage() == null){
            Picasso.get().load(R.drawable.jeton).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image1")){
            Picasso.get().load(R.drawable.picto1).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image2")){
            Picasso.get().load(R.drawable.picto2).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image3")){
            Picasso.get().load(R.drawable.picto3).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image4")){
            Picasso.get().load(R.drawable.picto4).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image5")){
            Picasso.get().load(R.drawable.picto5).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image6")){
            Picasso.get().load(R.drawable.picto6).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image7")){
            Picasso.get().load(R.drawable.picto7).into(holder.imageJeton);
        }else if (listeObjectifs.get(position).getUrlImage().equals("image8")){
            Picasso.get().load(R.drawable.picto8).into(holder.imageJeton);
        }else{
            Picasso.get().load(listeObjectifs.get(position).getUrlImage()).fit().transform(new RoundedCornersTransform()).into(holder.imageJeton);
        }
        if(position+1>nbFinis){
            holder.constraintLayout.setBackground(contexte.getResources().getDrawable(R.drawable.border2, null));
        }
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detail = new Intent(contexte, objectif_detail.class);
                detail.putExtra("id", listeObjectifs.get(position).getIdObjectif());
                contexte.startActivity(detail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listeObjectifs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView intitule;
        public TextView nbJetonEnCours;
        public TextView nbJetonTotal;
        public ImageView imageJeton;
        public ConstraintLayout constraintLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.intitule = itemView.findViewById(R.id.intitule_objectif);
            this.nbJetonEnCours = itemView.findViewById(R.id.nombre_jeton_en_cours);
            this.nbJetonTotal = itemView.findViewById(R.id.nombre_jeton_total);
            this.imageJeton = itemView.findViewById(R.id.jeton_objectif_item);
            this.constraintLayout = itemView.findViewById(R.id.layout_item_objectif);
        }
    }
}
