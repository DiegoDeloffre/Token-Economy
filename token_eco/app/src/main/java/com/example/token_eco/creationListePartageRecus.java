package com.example.token_eco;

public class creationListePartageRecus {

    private String prenom;
    private String iud;
    private String photo_profil_id;

    public creationListePartageRecus(String description, String photo_profil_id, String iud) {
        this.prenom = description;
        this.photo_profil_id = photo_profil_id;
        this.iud=iud;
    }

    public String getDescription() {
        return prenom;
    }

    public void setDescription(String description) {
        this.prenom = description;
    }

    public String getIUD() {
        return this.iud;
    }

    public String getPhoto_profil_id() {
        return photo_profil_id;
    }
}