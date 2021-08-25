package com.example.token_eco;

public class creationListeNotification {

    private String nomPrenom;
    private String iud;

    public creationListeNotification(String description, String iud) {
        this.nomPrenom = description;
        this.iud=iud;
    }

    public String getNomPrenom() {
        return nomPrenom;
    }

    public String getIud() {
        return iud;
    }


}
