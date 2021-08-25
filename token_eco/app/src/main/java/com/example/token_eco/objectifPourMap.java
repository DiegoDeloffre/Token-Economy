package com.example.token_eco;

public class objectifPourMap {
    private String idObjectif, titre, urlImage;
    private int jetonsAObtenir, jetonsObtenus;

    public objectifPourMap(String idObjectif, String titre, int jetonsAObtenir, int jetonsObtenus, String urlImage){
        this.idObjectif=idObjectif;
        this.titre=titre;
        this.jetonsAObtenir=jetonsAObtenir;
        this.jetonsObtenus=jetonsObtenus;
        this.urlImage = urlImage;
    }

    public String getIdObjectif(){
        return idObjectif;
    }
    public String getTitre(){
        return titre;
    }
    public String getJetonsAObtenir(){
        return Integer.toString(jetonsAObtenir);
    }
    public String getJetonsObtenus(){
        return Integer.toString(jetonsObtenus);
    }
    public String getUrlImage() { return urlImage; }
}
