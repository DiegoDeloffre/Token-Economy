package com.example.token_eco;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
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

public class compte extends menu_handler {
    private String email;
    private FirebaseAuth mAuth;
    private ImageView avatar;
    private EditText nbJetonsParDefaut;
    private int nbJetonsParDef;
    private ValueEventListener eventJeton;
    private List<File> photos;
    private DatabaseReference mDatabaseRef, jetonsParDef;
    private Uri mImageUri;
    private int PICK_IMAGE_REQUEST = 1 ;
    private int CAMERA_REQUEST_CODE = 2 ;
    private int READ_PERMISSION_CODE = 1;
    private int CAMERA_PERMISSION_CODE = 2;
    private int WRITE_PERMISSION_CODE = 3;
    private ByteArrayOutputStream baos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compte);
        super.setupToolBar();
        photos = new ArrayList<>();
        Button changer_mdp = findViewById(R.id.changer_mdp);
        Button modification = findViewById(R.id.modifier_compte);
        nbJetonsParDefaut = findViewById(R.id.modification_compte_jetons);
        mAuth = FirebaseAuth.getInstance();
        FirebaseHandler fh = new FirebaseHandler();
        avatar = findViewById(R.id.compte_photo_profil_adulte);
        jetonsParDef = fh.getDatabaseReferenceIUD().child("jetonParDefault");
        mDatabaseRef = fh.getDatabaseReferenceIUD().child("avatar");
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String s= dataSnapshot.getValue(String.class);
                    if(s == null){
                        Picasso.get().load(R.drawable.avatar).into(avatar);
                    }else{
                        Picasso.get().load(s).fit().into(avatar);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(compte.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        DatabaseReference mail = fh.getDatabaseReferenceIUD().child("mail");
        mail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                email = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        nbJetonsParDefaut.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        afficherJetons();
        nbJetonsParDefaut.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(nbJetonsParDefaut.getText().toString().equals(""))){
                    int valeurJetons = Integer.parseInt(nbJetonsParDefaut.getText().toString());
                    if(valeurJetons>15){
                        nbJetonsParDefaut.setText("15");
                        nbJetonsParDefaut.setError("Un maximum de 15 jetons peut être attribué");
                    }
                }
                if (nbJetonsParDefaut.getText().toString().equals("0")){
                    nbJetonsParDefaut.setText("1");
                    nbJetonsParDefaut.setError("Il faut attribuer au moins 1 jeton");
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        changer_mdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(compte.this, "Le lien pour réinitialiser votre mot de passe à été envoyé à votre adresse mail.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(compte.this, "Erreur, le lien n'a pas été envoyé : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
        ImageView retour = findViewById(R.id.fleche_retour);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        modification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(nbJetonsParDefaut.getText().toString())){
                    jetonsParDef.setValue(Integer.parseInt(nbJetonsParDefaut.getText().toString()));
                    nbJetonsParDefaut.setText(null);
                }
                uploadFile();
                Toast.makeText(getApplicationContext(), "Modifications effectuées", Toast.LENGTH_LONG).show();
            }
        });

        //Click sur l'imageView avatar
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ouvre le premier popup (recycler view)
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popup = inflater.inflate(R.layout.popup_window2, null);
                popup.setBackground(getDrawable(R.drawable.notif_border));
                onButtonShowPopupWindowClick(popup);
            }
        });
    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void afficherJetons(){
        eventJeton = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    nbJetonsParDef = dataSnapshot.getValue(Integer.class);
                    nbJetonsParDefaut.setHint(getResources().getString(R.string.creation_objectif_nombre_jeton)+" : "+nbJetonsParDef);
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
        jetonsParDef.addValueEventListener(eventJeton);
    }

    public void onButtonShowPopupWindowClick(View view) {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window2, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 150);

        Button btn_photo = popupView.findViewById(R.id.popup_photo);
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    Toast.makeText(compte.this, "Vous n'avez pas de caméra", Toast.LENGTH_SHORT).show();
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

        Button upload = popupView.findViewById(R.id.uploadImages);
        upload.setOnClickListener(new View.OnClickListener() {
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
        popup_image1.setImageResource(R.drawable.avatar1);
        popup_image2.setImageResource(R.drawable.avatar2);
        popup_image3.setImageResource(R.drawable.avatar3);
        popup_image4.setImageResource(R.drawable.avatar4);
        popup_image5.setImageResource(R.drawable.avatar5);
        popup_image6.setImageResource(R.drawable.avatar6);
        popup_image7.setImageResource(R.drawable.avatar7);
        popup_image8.setImageResource(R.drawable.avatar8);
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
                mImageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }else{
            Toast.makeText(compte.this, "Impossible de prendre une photo", Toast.LENGTH_SHORT).show();
        }
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
                if(image.getId()==R.id.popup_image1){
                    mImageUri = drawableEnUri(R.drawable.avatar1);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
                } else if(image.getId()==R.id.popup_image2){
                    mImageUri = drawableEnUri(R.drawable.avatar2);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
                } else if(image.getId()==R.id.popup_image3){
                    mImageUri = drawableEnUri(R.drawable.avatar3);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
                } else if(image.getId()==R.id.popup_image4){
                    mImageUri = drawableEnUri(R.drawable.avatar4);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
                } else if(image.getId()==R.id.popup_image5){
                    mImageUri = drawableEnUri(R.drawable.avatar5);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
                } else if(image.getId()==R.id.popup_image6){
                    mImageUri = drawableEnUri(R.drawable.avatar6);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
                } else if(image.getId()==R.id.popup_image7){
                    mImageUri = drawableEnUri(R.drawable.avatar7);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
                } else if(image.getId()==R.id.popup_image8){
                    mImageUri = drawableEnUri(R.drawable.avatar8);
                    bitmapCompress(100, mImageUri, Bitmap.CompressFormat.PNG);
                    Picasso.get().load(mImageUri).fit().into(avatar);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            bitmapCompress(15, mImageUri, Bitmap.CompressFormat.JPEG);
            Picasso.get().load(mImageUri).fit().into(avatar);
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmapCompress(15, mImageUri, Bitmap.CompressFormat.JPEG);
            Picasso.get().load(mImageUri).fit().into(avatar);
        }
    }

    private void uploadFile() {
        FirebaseHandler fh2 = new FirebaseHandler();
        mDatabaseRef = fh2.getDatabaseReferenceIUD().child("avatar");
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("avatar");
        if (mImageUri != null) {
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
                    Toast.makeText(compte.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        jetonsParDef.removeEventListener(eventJeton);
        super.onDestroy();
    }

    private void demandePermissonPourLecture() {
        ActivityCompat.requestPermissions(compte.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
    }
    private void demandePermissonPourCamera() {
        ActivityCompat.requestPermissions(compte.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }
    private void demandePermissonPourEcriture() {
        ActivityCompat.requestPermissions(compte.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == READ_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(compte.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(compte.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(compte.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(compte.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == WRITE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(compte.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(compte.this, "permission refusée", Toast.LENGTH_SHORT).show();
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

    private Uri drawableEnUri(int d){
        return  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                compte.this.getResources().getResourcePackageName(d)+ '/'
                + compte.this.getResources().getResourceTypeName(d)
                + '/' + compte.this.getResources().getResourceEntryName(d));
    }

    private void bitmapCompress(int qualite, Uri uri, Bitmap.CompressFormat format){
        baos = new ByteArrayOutputStream();
        Bitmap bmp = null;
        if(Build.VERSION.SDK_INT < 28) {
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            ImageDecoder.Source source = ImageDecoder.createSource(compte.this.getContentResolver(), uri);
            try {
                bmp = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bmp.compress(format, qualite, baos);
    }
}
