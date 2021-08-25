package com.example.token_eco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class connexion extends AppCompatActivity {
    private EditText connexion_mail, connexion_password;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        Button button_connexion = findViewById(R.id.button_connexion);
        mAuth = FirebaseAuth.getInstance();
        connexion_mail = findViewById(R.id.connexion_mail);
        connexion_password = findViewById(R.id.connexion_password);
        TextView connexion_mdp_oublie = findViewById(R.id.connexion_mdp_oublie);
        TextView lien_creation_compte = findViewById(R.id.lien_creation_compte);

        connexion_mail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        connexion_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        lien_creation_compte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), creation_compte.class));
            }
        });



        if (mAuth.getCurrentUser() != null) {
            if(mAuth.getCurrentUser().isEmailVerified()){
                Intent acc = new Intent(getApplicationContext(), accueil.class);
                startActivity(acc);
                finish();
            }
        }

        button_connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String mEmail = connexion_mail.getText().toString().trim();
                String mPassword = connexion_password.getText().toString().trim();

                if (TextUtils.isEmpty(mEmail)) {
                    connexion_mail.setError("Le champ email est vide");
                    return;
                }

                if(TextUtils.isEmpty(mPassword)) {
                    connexion_password.setError("Le champ mot de passe est vide");
                    return;
                }

                if(mPassword.length() < 6 ) {
                    connexion_password.setError("Le mot de passe ne contient pas assez de caractère (6 minimum) ");
                    return;
                }

                //authentifier l'utilisateur

                mAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful() ) {
                            user = mAuth.getCurrentUser();
                            if(user.isEmailVerified()){
                                Toast.makeText(connexion.this,"Utilisateur connecté",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), accueil.class));
                                finish();
                            } else {
                                Toast.makeText(connexion.this,"L'adresse mail n'est pas vérifiée",Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(connexion.this,"Erreur" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        connexion_mdp_oublie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetMail = new EditText(v.getContext());
                resetMail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                resetMail.setTextColor(Color.BLACK);
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert);
                passwordResetDialog.setTitle("Reinitialiser le mot de passe");
                passwordResetDialog.setMessage("Entrer votre email pour changer votre mot de passe.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(connexion.this, "Le lien pour réinitialiser votre mot de passe à été envoyé à votre adresse mail.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(connexion.this, "Erreur, le lien n'a pas été envoyé" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });

                passwordResetDialog.create().show();

            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}