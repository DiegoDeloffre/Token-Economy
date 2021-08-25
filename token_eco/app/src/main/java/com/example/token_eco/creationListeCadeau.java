package com.example.token_eco;

public class creationListeCadeau {
    private String titre;
    private String RecompenseID;
    private int coutBravo;
    private String url_image_cadeau;

    public creationListeCadeau(String titre, String RecompenseID, int coutBravo, String url_image_cadeau) {
        this.titre = titre;
        this.RecompenseID = RecompenseID;
        this.coutBravo = coutBravo;
        this.url_image_cadeau = url_image_cadeau;
    }

    public int getCoutBravo() {
        return coutBravo;
    }

    public String getRecompenseID() {
        return RecompenseID;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }
    public String getUrl_image_cadeau(){
        return this.url_image_cadeau;
    }
}
