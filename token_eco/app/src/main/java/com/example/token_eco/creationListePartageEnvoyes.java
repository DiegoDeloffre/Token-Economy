package com.example.token_eco;

public class creationListePartageEnvoyes {

    private String prenom;
    private String iud;
    private String photo_profil_id;


    public creationListePartageEnvoyes(String description, String photo_profil_id, String iud) {
        this.prenom = description;
        this.iud = iud;
        this.photo_profil_id = photo_profil_id;
    }

    public String getDescription() {
        return prenom;
    }

    public String getIud() {
        return iud;
    }

    public void setDescription(String description) {
        this.prenom = description;
    }

    public String getPhoto_profil_id() {
        return photo_profil_id;
    }
}
