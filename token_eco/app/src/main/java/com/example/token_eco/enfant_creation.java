package com.example.token_eco;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class enfant_creation extends menu_handler implements NavigationView.OnNavigationItemSelectedListener{
    private FirebaseHandler fh;
    private Uri mAvatarUri, mBravoUri, photoURI;
    private String nom_imageAvatar, nom_imageBravo;
    private EditText prenom, surnom;
    private Boolean avatarChosi = false;
    private ImageView avatar, bravo;
    private List<File> photos;
    private int PICK_IMAGE_REQUEST = 1 ;
    private int CAMERA_REQUEST_CODE = 2 ;
    private int READ_PERMISSION_CODE = 1;
    private int CAMERA_PERMISSION_CODE = 2;
    private int WRITE_PERMISSION_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enfant_creation);
        super.setupToolBar();

        Button button_creer_enfant = findViewById(R.id.button_creer_enfant);
        surnom = findViewById(R.id.creation_enfant_surnom);
        prenom = findViewById(R.id.creation_enfant_prenom);
        ImageView retour = findViewById(R.id.fleche_retour);
        fh = new FirebaseHandler();
        photos = new ArrayList<>();
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        button_creer_enfant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enregistrerEnfant();
            }
        });

        prenom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        surnom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        avatar =findViewById(R.id.photo_enfant);
        //Click sur l'imageView avatar
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarChosi = true;
                //ouvre le premier popup (recycler view)
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popup = inflater.inflate(R.layout.popup_window2, null);
                popup.setBackground(getDrawable(R.drawable.notif_border));
                onButtonShowPopupWindowClick(popup);
            }
        });

        bravo = findViewById(R.id.imageView_creer_bravo);

        bravo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                avatarChosi = false;
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popup = inflater.inflate(R.layout.popup_window, null);
                popup.setBackground(getDrawable(R.drawable.notif_border));
                onButtonShowPopupWindowClick(popup);
            }
        });
    }

    private void enregistrerEnfant(){
        if(prenom.getText().length() == 0){
            prenom.setError("Il lui faut un nom");
            return;
        }
        if((prenom.getText().toString().trim()).equals(surnom.getText().toString().trim())){
            surnom.setError("Le surnom est identique au nom");
            return;
        }
        DatabaseReference nomEnfant = fh.getDatabaseReferenceIUD().child("enfants");
        nomEnfant.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> Existant = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Existant.add(ds.child("prenom").getValue(String.class).toLowerCase().trim());
                    if(ds.hasChild("surnom")){
                        Existant.add(ds.child("surnom").getValue(String.class).toLowerCase().trim());
                    }
                }
                if(Existant.contains(surnom.getText().toString().toLowerCase().trim())){
                    surnom.setError(surnom.getText().toString() + " est déja utilisé");
                    return;
                }
                if(Existant.contains(prenom.getText().toString().toLowerCase().trim())){
                    prenom.setError(prenom.getText().toString() + " est déja utilisé");
                }else{
                    DatabaseReference Enfant = fh.getDatabaseReferenceIUD().child("enfants");
                    String push = Enfant.push().getKey();
                    if(surnom.getText().length() == 0){
                        Enfant.child(push).setValue(new Post(prenom.getText().toString(), null, 0));
                    }else{
                        Enfant.child(push).setValue(new Post(prenom.getText().toString(), surnom.getText().toString(), 0));
                    }
                    try {
                        uploadFile(push);
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

    public static class Post {
        public String prenom;
        public String surnom;
        public int nbBravos;

        public Post(String prenom, String surnom, int nbBravos) {
            this.nbBravos=nbBravos;
            this.prenom=prenom;
            this.surnom=surnom;
        }
    }

    public void onButtonShowPopupWindowClick(View view) {
        // inflate the layout of the popup window
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView;
        if (avatarChosi){
            popupView = inflater.inflate(R.layout.popup_window2, null);
        }else{
            popupView = inflater.inflate(R.layout.popup_window, null);
        }

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 50);
        Button btn_upload = popupView.findViewById(R.id.uploadImages);

        Button btn_photo = popupView.findViewById(R.id.popup_photo);
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    Toast.makeText(enfant_creation.this, "Vous n'avez pas de caméra", Toast.LENGTH_SHORT).show();
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

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                avatarChosi = false;
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

        if(avatarChosi){
            popup_image1.setImageResource(R.drawable.enfant1);
            popup_image2.setImageResource(R.drawable.enfant2);
            popup_image3.setImageResource(R.drawable.enfant3);
            popup_image4.setImageResource(R.drawable.enfant4);
            popup_image5.setImageResource(R.drawable.enfant5);
            popup_image6.setImageResource(R.drawable.enfant6);
            popup_image7.setImageResource(R.drawable.enfant7);
            popup_image8.setImageResource(R.drawable.enfant8);

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
        }else{
            popup_image1.setImageResource(R.drawable.bravo1);
            popup_image2.setImageResource(R.drawable.bravo2);
            popup_image3.setImageResource(R.drawable.bravo3);
            popup_image4.setImageResource(R.drawable.bravo4);
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
        }
    }

    public void popupImageFullScreen(View view, final ImageView image) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.image_fullscreen, null);
        ImageView full = popupView.findViewById(R.id.image_full);
        full.setImageDrawable(image.getDrawable());
        full.setPadding(0,20,0,0);
        full.setImageDrawable(image.getDrawable());

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button valider = popupView.findViewById(R.id.selectionner_image);
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(avatarChosi){
                    popupWindow.dismiss();
                    mAvatarUri = null;
                    if(image.getId()==R.id.popup_image1){
                        nom_imageAvatar = "image1";
                        Picasso.get().load(R.drawable.enfant1).into(avatar);
                    } else if(image.getId()==R.id.popup_image2){
                        nom_imageAvatar = "image2";
                        Picasso.get().load(R.drawable.enfant2).into(avatar);
                    } else if(image.getId()==R.id.popup_image3){
                        nom_imageAvatar = "image3";
                        Picasso.get().load(R.drawable.enfant3).into(avatar);
                    } else if(image.getId()==R.id.popup_image4){
                        nom_imageAvatar = "image4";
                        Picasso.get().load(R.drawable.enfant4).into(avatar);
                    } else if(image.getId()==R.id.popup_image5){
                        nom_imageAvatar = "image5";
                        Picasso.get().load(R.drawable.enfant5).into(avatar);
                    } else if(image.getId()==R.id.popup_image6){
                        nom_imageAvatar = "image6";
                        Picasso.get().load(R.drawable.enfant6).into(avatar);
                    } else if(image.getId()==R.id.popup_image7){
                        nom_imageAvatar = "image7";
                        Picasso.get().load(R.drawable.enfant7).into(avatar);
                    } else if(image.getId()==R.id.popup_image8){
                        nom_imageAvatar = "image8";
                        Picasso.get().load(R.drawable.enfant8).into(avatar);
                    }
                    avatarChosi=false;
                }else{
                    mBravoUri = null;
                    popupWindow.dismiss();
                    if(image.getId()==R.id.popup_image1){
                        nom_imageBravo = "image1";
                        Picasso.get().load(R.drawable.bravo1).into(bravo);
                    } else if(image.getId()==R.id.popup_image2){
                        nom_imageBravo = "image2";
                        Picasso.get().load(R.drawable.bravo2).into(bravo);
                    } else if(image.getId()==R.id.popup_image3){
                        nom_imageBravo = "image3";
                        Picasso.get().load(R.drawable.bravo3).into(bravo);
                    } else if(image.getId()==R.id.popup_image4){
                        nom_imageBravo = "image4";
                        Picasso.get().load(R.drawable.bravo4).into(bravo);
                    }
                }

                //enfant_creation.avatar.setImageDrawable(image.getDrawable());


                //save l'image dans la bdd
                //fermer les deux popup
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
                // a faire
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }else{
            Toast.makeText(enfant_creation.this, "Impossible de prendre une photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (avatarChosi) {
                mAvatarUri = data.getData();
                Picasso.get().load(mAvatarUri).into(avatar);
            } else {
                mBravoUri = data.getData();
                Picasso.get().load(mBravoUri).into(bravo);
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (avatarChosi) {
                mAvatarUri = photoURI;
                Picasso.get().load(mAvatarUri).into(avatar);
            } else {
                mBravoUri = photoURI;
                Picasso.get().load(mBravoUri).into(bravo);
            }
        }
        avatarChosi = false;

    }

    private void uploadFile(String enfantIUD) throws IOException {
        FirebaseHandler fh2 = new FirebaseHandler();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final DatabaseReference mDatabaseRefAvatar = fh2.getDatabaseReferenceIUD().child("enfants").child(enfantIUD).child("avatar");
        StorageReference mStorageRefAvatar = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfantIUD).child("avatar");
        if (mAvatarUri != null) {
            Bitmap bmp;
            if(Build.VERSION.SDK_INT < 28) {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), mAvatarUri);
            }else{
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), mAvatarUri);
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
                    Toast.makeText(enfant_creation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (nom_imageAvatar!=null){
            mDatabaseRefAvatar.setValue(nom_imageAvatar);
            nom_imageAvatar=null;
        }
        final DatabaseReference mDatabaseRef = fh2.getDatabaseReferenceIUD().child("enfants").child(enfantIUD).child("img_bravo");
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfantIUD).child("img_bravo");
        if (mBravoUri != null) {
            Bitmap bmp;
            if(Build.VERSION.SDK_INT < 28) {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), mBravoUri);
            }else{
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), mBravoUri);
                bmp = ImageDecoder.decodeBitmap(source);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 15, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask2 = mStorageRef.putBytes(data);
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
                    mDatabaseRef.setValue(url.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(enfant_creation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (nom_imageBravo!=null){
            mDatabaseRef.setValue(nom_imageBravo);
            nom_imageBravo=null;
        }
    }

    private void demandePermissonPourLecture() {
        ActivityCompat.requestPermissions(enfant_creation.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
    }

    private void demandePermissonPourCamera() {
        ActivityCompat.requestPermissions(enfant_creation.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }


    private void demandePermissonPourEcriture() {
        ActivityCompat.requestPermissions(enfant_creation.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == READ_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(enfant_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(enfant_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(enfant_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(enfant_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == WRITE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(enfant_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(enfant_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);
        photos.add(image);
        return image;
    }

    @Override
    protected void onDestroy() {
        for(File f : photos){
            if(f!=null){
                f.delete();
            }
        }
        super.onDestroy();
    }
}
