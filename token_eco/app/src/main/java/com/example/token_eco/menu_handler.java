package com.example.token_eco;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class menu_handler extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer;

    public void  setupToolBar(){
        //gestion de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ImageView menu = findViewById(R.id.menu_ouverture);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.END);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer = findViewById(R.id.drawer_layout);
        switch (item.getItemId()) {
            case R.id.nav_accueil:
                if(!(this instanceof accueil)){
                    finishAffinity();
                    Intent page = new Intent(getApplicationContext(), accueil.class);
                    startActivity(page);
                }
                break;
            case R.id.nav_compte:
                if(!(this instanceof compte)){
                    Intent page2 = new Intent(getApplicationContext(), compte.class);
                    startActivity(page2);
                    if(this instanceof Notification || this instanceof aPropos || this instanceof Partage){
                        finish();
                    }
                }
                break;
            case R.id.nav_apropos:
                if(!(this instanceof aPropos)){
                    Intent page3 = new Intent(getApplicationContext(), aPropos.class);
                    startActivity(page3);
                    if(this instanceof compte || this instanceof Notification || this instanceof Partage){
                        finish();
                    }
                }
                break;
            case R.id.nav_deconnexion:
                AlertDialog.Builder ad = new AlertDialog.Builder(this,R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Voulez-vous vraiment vous déconnecter ?")
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("Confimer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(getApplicationContext(), connexion.class));
                                finishAffinity();
                                //finish();
                            }
                        });
                AlertDialog message = ad.create();
                message.show();
                break;
            case R.id.nav_notif:
                if(!(this instanceof Notification)){
                    Intent page6 = new Intent(getApplicationContext(), Notification.class);
                    startActivity(page6);
                    if(this instanceof compte || this instanceof aPropos || this instanceof Partage){
                        finish();
                    }
                }
                break;
            case R.id.nav_synchro:
                if(!(this instanceof Partage)){
                    Intent page7 = new Intent(getApplicationContext(), Partage.class);
                    startActivity(page7);
                    if(this instanceof compte || this instanceof Notification || this instanceof aPropos){
                        finish();
                    }
                }
                break;
        }
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        }else{
            if(!(this instanceof accueil)){
                super.onBackPressed();
            }
        }
    }

    public boolean MenuEstOuvert(){
        drawer = findViewById(R.id.drawer_layout);
        return drawer.isDrawerOpen(GravityCompat.END);
    }

    public ValueEventListener enfantExiste( DatabaseReference dr){
        ValueEventListener v = new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Toast.makeText(getApplicationContext() , "Impossible de lire les données", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                    Intent acc = new Intent(getApplicationContext(), accueil.class);
                    startActivity(acc);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        dr.addValueEventListener(v);
        return v;
    }

    public ValueEventListener estSync( DatabaseReference dr){
        ValueEventListener v = new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                SharedPreferences pref;
                pref = menu_handler.this.getSharedPreferences("pref", MODE_PRIVATE);
                String iud = pref.getString("iud", "no data");
                if(!iud.equals(auth.getCurrentUser().getUid()))
                   if(!dataSnapshot.hasChild(iud)){
                       finishAffinity();
                       Intent acc = new Intent(getApplicationContext(), accueil.class);
                       startActivity(acc);
                       Toast.makeText(getApplicationContext() , "Problème de synchronisation", Toast.LENGTH_SHORT).show();
                   }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        dr.addValueEventListener(v);
        return v;
    }
}


