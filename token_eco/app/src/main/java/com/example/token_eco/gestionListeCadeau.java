package com.example.token_eco;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

public class gestionListeCadeau extends RecyclerView.Adapter<gestionListeCadeau.ViewHolder>{
    private List<creationListeCadeau> listeCadeaux;
    private Context contexte;
    private int reclames;
    private String url_cadeau;

    public gestionListeCadeau( List<creationListeCadeau> listeCadeaux,  Context contexte, int reclames, String url_cadeau) {
        this.contexte=contexte;
        this.listeCadeaux = listeCadeaux;
        this.reclames = reclames;
        this.url_cadeau=url_cadeau;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listeCdx= layoutInflater.inflate(R.layout.cadeau_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listeCdx);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final creationListeCadeau creationListeEnfant = listeCadeaux.get(position);
        holder.textView.setText(listeCadeaux.get(position).getTitre());
        if(listeCadeaux.get(position).getUrl_image_cadeau() == null){
            Picasso.get().load(R.drawable.cadeau).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image1")){
            Picasso.get().load(R.drawable.cadeau1).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image2")){
            Picasso.get().load(R.drawable.cadeau2).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image3")){
            Picasso.get().load(R.drawable.cadeau3).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image4")){
            Picasso.get().load(R.drawable.cadeau4).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image5")){
            Picasso.get().load(R.drawable.cadeau5).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image6")){
            Picasso.get().load(R.drawable.cadeau6).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image7")){
            Picasso.get().load(R.drawable.cadeau7).into(holder.imageView);
        }else if (listeCadeaux.get(position).getUrl_image_cadeau().equals("image8")){
            Picasso.get().load(R.drawable.cadeau8).into(holder.imageView);
        }else{
            Picasso.get().load(listeCadeaux.get(position).getUrl_image_cadeau()).fit().transform(new RoundedCornersTransform()).into(holder.imageView);
        }
        //holder.imageView.setImageResource(listeCadeaux[position].getPhoto_profil_id());
        //holder.bravo1.setImageResource(listeCadeaux[position].getBravo_id());
        if(position+1>reclames){
            holder.re.setBackground(contexte.getResources().getDrawable(R.drawable.border2, null));
        }

        Drawable img_recompense = null;
        if(url_cadeau == null) {
           img_recompense = contexte.getDrawable(R.drawable.jeton);
        }else if (url_cadeau.equals("image1")) {
            img_recompense = contexte.getDrawable(R.drawable.bravo1);
        } else if (url_cadeau.equals("image2")) {
            img_recompense = contexte.getDrawable(R.drawable.bravo2);
        } else if (url_cadeau.equals("image3")) {
            img_recompense = contexte.getDrawable(R.drawable.bravo3);
        } else if (url_cadeau.equals("image4")) {
            img_recompense = contexte.getDrawable(R.drawable.bravo4);
        }
        if(img_recompense == null) {
            Picasso.get().load(url_cadeau).fit().into(holder.bravo1);
            if(listeCadeaux.get(position).getCoutBravo()>1){
                Picasso.get().load(url_cadeau).fit().into(holder.bravo2);
                if(listeCadeaux.get(position).getCoutBravo()>2){
                    Picasso.get().load(url_cadeau).fit().into(holder.bravo3);
                    if(listeCadeaux.get(position).getCoutBravo()>3) {
                        Picasso.get().load(url_cadeau).fit().into(holder.bravo4);
                        if(listeCadeaux.get(position).getCoutBravo()>4) {
                            Picasso.get().load(url_cadeau).fit().into(holder.bravo5);
                        }
                    }
                }
            }
        }else {
            holder.bravo1.setImageDrawable(img_recompense);
            if(listeCadeaux.get(position).getCoutBravo()>1){
                holder.bravo2.setImageDrawable(img_recompense);
                if(listeCadeaux.get(position).getCoutBravo()>2){
                    holder.bravo3.setImageDrawable(img_recompense);
                    if(listeCadeaux.get(position).getCoutBravo()>3) {
                        holder.bravo4.setImageDrawable(img_recompense);
                        if(listeCadeaux.get(position).getCoutBravo()>4) {
                            holder.bravo5.setImageDrawable(img_recompense);
                        }
                    }
                }
            }
        }

        holder.re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detail = new Intent(contexte, recompense_detail.class);
                detail.putExtra("id", creationListeEnfant.getRecompenseID());
                contexte.startActivity(detail);
                //((recompense)contexte).finish();
            }
        });
    }


    @Override
    public int getItemCount() {
        return listeCadeaux.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public ImageView bravo1;
        public ImageView bravo2;
        public ImageView bravo3;
        public ImageView bravo4;
        public ImageView bravo5;
        public ConstraintLayout re;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.photo_cadeau);
            this.textView = itemView.findViewById(R.id.titre_cadeau);
            this.bravo1 = itemView.findViewById(R.id.bravo1);
            this.bravo2 = itemView.findViewById(R.id.bravo2);
            this.bravo3 = itemView.findViewById(R.id.bravo3);
            this.bravo4 = itemView.findViewById(R.id.bravo4);
            this.bravo5 = itemView.findViewById(R.id.bravo5);
            this.re = itemView.findViewById(R.id.layout_affichage_recompenses);
        }
    }
}
