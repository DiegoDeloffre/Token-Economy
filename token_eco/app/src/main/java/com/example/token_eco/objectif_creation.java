package com.example.token_eco;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class objectif_creation extends menu_handler implements NavigationView.OnNavigationItemSelectedListener {

    private EditText titreObjectif, description, nbJeton;
    private FirebaseHandler fh;
    private int nbJetonsParDef;
    private ImageView imageJeton;
    private DatabaseReference enfantExiste;
    private DatabaseReference jetonsParDef;
    private DatabaseReference drSync;
    private ValueEventListener eventEnfantExiste;
    private ValueEventListener eventJetons;
    private ValueEventListener eventSync;
    private String nom_imageJeton;
    private Uri mJetonUri;
    private List<File> photos;
    private int PICK_IMAGE_REQUEST = 1 ;
    private int CAMERA_REQUEST_CODE = 2 ;
    private int READ_PERMISSION_CODE = 1;
    private int CAMERA_PERMISSION_CODE = 2;
    private int WRITE_PERMISSION_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objectif_creation);
        Button button_creer_objectif = findViewById(R.id.button_creer_objectif);
        titreObjectif = findViewById(R.id.creation_objectif_titre_comportement);
        nbJeton = findViewById(R.id.creation_objectif_nombre_jeton);
        description = findViewById(R.id.creation_objectif_description);
        imageJeton = findViewById(R.id.image_jeton_creation);
        super.setupToolBar();
        photos = new ArrayList<>();
        fh = new FirebaseHandler();
        enfantExiste = fh.getDatabaseReferenceEnfant(getApplicationContext());
        eventEnfantExiste = super.enfantExiste(enfantExiste);
        drSync = fh.getDatabaseReferenceIUD().child("JePeuxLireSesEnfants");
        eventSync = super.estSync(drSync);
        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        nbJetonsParDef=0;

        nbJeton.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(nbJeton.getText().toString().equals(""))){
                    int valeurJetons = Integer.parseInt(nbJeton.getText().toString());
                    if(valeurJetons>15){
                        nbJeton.setText("15");
                        nbJeton.setError("Un maximum de 15 jetons peut être attribué");
                    }
                }
                if (nbJeton.getText().toString().equals("0")){
                    nbJeton.setText("1");
                    nbJeton.setError("Il faut attribuer au moins 1 jeton");
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        nbJeton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        titreObjectif.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        jetonsParDef = fh.getDatabaseReferenceIUD().child("jetonParDefault");
        eventJetons = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    nbJetonsParDef = dataSnapshot.getValue(Integer.class);
                    nbJeton.setHint(getResources().getString(R.string.creation_objectif_nombre_jeton)+" : "+nbJetonsParDef);
                }catch (NullPointerException e){
                    Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
                    Intent accueil = new Intent(getApplicationContext(), accueil.class);
                    finishAffinity();
                    startActivity(accueil);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        jetonsParDef.addValueEventListener(eventJetons);
        button_creer_objectif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(titreObjectif.getText())){
                    titreObjectif.setError("L'objectif n'a pas de titre");
                    return;
                }
                if (TextUtils.isEmpty(description.getText())){
                    description.setError("L'objectif n'a pas de description");
                    return;
                }
                enregistrerObjectif();
            }
        });

        imageJeton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popup = inflater.inflate(R.layout.popup_window2, null);
                popup.setBackground(getDrawable(R.drawable.notif_border));
                onButtonShowPopupWindowClick(popup);
            }
        });
    }

    private void enregistrerObjectif(){
        final DatabaseReference objectif = fh.getDatabaseReferenceEnfant(getApplicationContext()).child("objectifs");
        objectif.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> titres = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    titres.add(ds.child("titre").getValue(String.class).toLowerCase().trim());
                }
                if(titres.contains(titreObjectif.getText().toString().toLowerCase().trim())){
                    titreObjectif.setError("Titre déjà utilisé.");
                }else{
                    String url = objectif.push().getKey();
                    if(nbJeton.length() != 0){
                        objectif.child(url).setValue(new Post(titreObjectif.getText().toString() , description.getText().toString(), Integer.parseInt(nbJeton.getText().toString()), 0));
                    }else{
                        objectif.child(url).setValue(new Post(titreObjectif.getText().toString() , description.getText().toString(), nbJetonsParDef, 0));
                    }
                    try {
                        uploadFile(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        for(File f : photos){
            if(f!=null){
                f.delete();
            }
        }
        jetonsParDef.removeEventListener(eventJetons);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        jetonsParDef.removeEventListener(eventJetons);
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        jetonsParDef.addValueEventListener(eventJetons);
        enfantExiste.addValueEventListener(eventEnfantExiste);
        drSync.addValueEventListener(eventSync);
    }

    public static class Post {
        public String description;
        public String titre;
        public int jetonsAObtenir;
        public int jetonsObtenus;

        public Post(String titre, String description, int jetonsAObtenir, int jetonsObtenus) {
            this.jetonsObtenus=jetonsObtenus;
            this.jetonsAObtenir=jetonsAObtenir;
            this.titre=titre;
            this.description=description;
        }
    }

    public void onButtonShowPopupWindowClick(View view) {

        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window2, null);


        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);


        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 50);
        Button btn_upload = popupView.findViewById(R.id.uploadImages);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    demandePermissonPourLecture();
                }else{
                    openFileChooser();
                    popupWindow.dismiss();
                }
            }
        });
        Button btn_photo = popupView.findViewById(R.id.popup_photo);
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    Toast.makeText(objectif_creation.this, "Vous n'avez pas de caméra", Toast.LENGTH_SHORT).show();
                } else {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        demandePermissonPourCamera();
                    } else {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            demandePermissonPourEcriture();
                        } else {
                            prendrePhoto();
                            popupWindow.dismiss();
                        }
                    }
                }
            }
        });


        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();

                return true;
            }
        });

        final ImageView popup_image1, popup_image2, popup_image3, popup_image4,popup_image5,popup_image6,popup_image7,popup_image8;
        popup_image1 = popupView.findViewById(R.id.popup_image1);
        popup_image2 = popupView.findViewById(R.id.popup_image2);
        popup_image3 = popupView.findViewById(R.id.popup_image3);
        popup_image4 = popupView.findViewById(R.id.popup_image4);
        popup_image5 = popupView.findViewById(R.id.popup_image5);
        popup_image6 = popupView.findViewById(R.id.popup_image6);
        popup_image7 = popupView.findViewById(R.id.popup_image7);
        popup_image8 = popupView.findViewById(R.id.popup_image8);

        popup_image1.setImageResource(R.drawable.picto1);
        popup_image2.setImageResource(R.drawable.picto2);
        popup_image3.setImageResource(R.drawable.picto3);
        popup_image4.setImageResource(R.drawable.picto4);
        popup_image5.setImageResource(R.drawable.picto5);
        popup_image6.setImageResource(R.drawable.picto6);
        popup_image7.setImageResource(R.drawable.picto7);
        popup_image8.setImageResource(R.drawable.picto8);

        popup_image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image1);
                }
            });
        popup_image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image2);
                }
            });
        popup_image3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image3);
                }
            });
        popup_image4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image4);
                }
            });
        popup_image5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image5);
                }
            });
        popup_image6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image6);
                }
            });
        popup_image7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image7);
                }
            });
        popup_image8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View popup = inflater.inflate(R.layout.image_fullscreen, null);
                    popupImageFullScreen(popup, popup_image8);
                }
            });
    }

    public void popupImageFullScreen(View view, final ImageView image) {


        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.image_fullscreen, null);
        ImageView full = popupView.findViewById(R.id.image_full);
        full.setImageDrawable(image.getDrawable());
        full.setPadding(0,20,0,0);
        full.setImageDrawable(image.getDrawable());

        Button valider = popupView.findViewById(R.id.selectionner_image);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                mJetonUri = null;
                if(image.getId()==R.id.popup_image1){
                    nom_imageJeton = "image1";
                    Picasso.get().load(R.drawable.picto1).into(imageJeton);
                } else if(image.getId()==R.id.popup_image2){
                    nom_imageJeton = "image2";
                    Picasso.get().load(R.drawable.picto2).into(imageJeton);
                } else if(image.getId()==R.id.popup_image3){
                    nom_imageJeton = "image3";
                    Picasso.get().load(R.drawable.picto3).into(imageJeton);
                } else if(image.getId()==R.id.popup_image4){
                    nom_imageJeton = "image4";
                    Picasso.get().load(R.drawable.picto4).into(imageJeton);
                } else if(image.getId()==R.id.popup_image5){
                    nom_imageJeton = "image5";
                    Picasso.get().load(R.drawable.picto5).into(imageJeton);
                } else if(image.getId()==R.id.popup_image6){
                    nom_imageJeton = "image6";
                    Picasso.get().load(R.drawable.picto6).into(imageJeton);
                } else if(image.getId()==R.id.popup_image7){
                    nom_imageJeton = "image7";
                    Picasso.get().load(R.drawable.picto7).into(imageJeton);
                } else if(image.getId()==R.id.popup_image8){
                    nom_imageJeton = "image8";
                    Picasso.get().load(R.drawable.picto8).into(imageJeton);
                }
            }
        });

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void prendrePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                mJetonUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mJetonUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }else{
            Toast.makeText(objectif_creation.this, "Impossible de prendre une photo", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mJetonUri = data.getData();
            Picasso.get().load(mJetonUri).fit().into(imageJeton);
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Picasso.get().load(mJetonUri).into(imageJeton);
        }
    }

    private void uploadFile(String objectifIUD) throws IOException {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final DatabaseReference mDatabaseRefAvatar = fh.getDatabaseReferenceEnfant(getApplicationContext()).child("objectifs").child(objectifIUD).child("imageJeton");
        SharedPreferences pref;
        pref = this.getSharedPreferences("pref", MODE_PRIVATE);
        String enfant = pref.getString("enfant", "no data");
        StorageReference mStorageRefAvatar = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("objectifs").child(objectifIUD).child("imageJeton");
        if (mJetonUri != null) {
            Bitmap bmp;
            if(Build.VERSION.SDK_INT < 28) {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), mJetonUri);
            }else{
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), mJetonUri);
                bmp = ImageDecoder.decodeBitmap(source);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 15, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask2 = mStorageRefAvatar.putBytes(data);
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {}}, 500);
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete()){}
                    Uri url = uri.getResult();
                    mDatabaseRefAvatar.setValue(url.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(objectif_creation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (nom_imageJeton!=null){
            mDatabaseRefAvatar.setValue(nom_imageJeton);
            nom_imageJeton=null;
        }
    }

    private void demandePermissonPourLecture() {
        ActivityCompat.requestPermissions(objectif_creation.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
    }
    private void demandePermissonPourCamera() {
        ActivityCompat.requestPermissions(objectif_creation.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }
    private void demandePermissonPourEcriture() {
        ActivityCompat.requestPermissions(objectif_creation.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == READ_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(objectif_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(objectif_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(objectif_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(objectif_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == WRITE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(objectif_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(objectif_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File image = File.createTempFile(imageFileName,".jpg", storageDir);
        //photoPath = image.getAbsolutePath();
        return File.createTempFile(imageFileName,".jpg", storageDir);
    }
}
