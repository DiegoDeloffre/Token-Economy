package com.example.token_eco;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class aPropos extends menu_handler {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_propos);
        super.setupToolBar();

        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
