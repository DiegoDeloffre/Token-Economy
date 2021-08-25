package com.example.token_eco;

public class creationListeEnfant {

    private String iudUser;
    private String prenom;
    private String surnom;
    private String idEnfant;
    private String photo_profil_url;

    public creationListeEnfant(String prenom, String surnom,  String idEnfant, String iudUser ,String photo_profil_url) {
        this.prenom=prenom;
        this.surnom=surnom;
        this.idEnfant=idEnfant;
        this.photo_profil_url = photo_profil_url;
        this.iudUser=iudUser;
    }

    public String getPrenom() { return this.prenom; }

    public String getSurnom() {
        return this.surnom;
    }

    public String getIud() {
        return this.iudUser;
    }

    public String getIdEnfant() {
        return this.idEnfant;
    }

    public String getPhoto_profil_id() {
        return photo_profil_url;
    }


}
