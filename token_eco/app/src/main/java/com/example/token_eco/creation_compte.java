package com.example.token_eco;

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
import android.text.TextUtils;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
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

public class creation_compte extends AppCompatActivity {

    private EditText creation_compte_mail, creation_compte_mdp, creation_compte_mdp_confirme, creation_compte_nom,creation_compte_prenom ;
    private int PICK_IMAGE_REQUEST = 1 ;
    private int CAMERA_REQUEST_CODE = 2 ;
    private int READ_PERMISSION_CODE = 1;
    private int CAMERA_PERMISSION_CODE = 2;
    private int WRITE_PERMISSION_CODE = 3;
    private ImageView avatar;
    private DatabaseReference mDatabaseRef;
    private List<File> photos;
    private FirebaseAuth mAuth;
    private Uri mImageUri;
    private ByteArrayOutputStream baos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation_compte);
        photos = new ArrayList<>();
        Button button_creation_compte =  findViewById(R.id.button_creation_compte);
        creation_compte_mail =  findViewById(R.id.creation_compte_mail);
        creation_compte_mdp = findViewById(R.id.creation_compte_mdp);
        creation_compte_mdp_confirme = findViewById(R.id.creation_compte_mdp_confirme);
        creation_compte_nom = findViewById(R.id.creation_compte_nom);
        creation_compte_prenom = findViewById(R.id.creation_compte_prenom);
        mAuth = FirebaseAuth.getInstance();
        mImageUri = drawableEnUri(R.drawable.avatar);
        creation_compte_mail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        creation_compte_mdp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        creation_compte_mdp_confirme.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        creation_compte_nom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        creation_compte_prenom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        button_creation_compte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail = creation_compte_mail.getText().toString().trim();
                String mPassword = creation_compte_mdp.getText().toString().trim();
                String mMdpConfirme = creation_compte_mdp_confirme.getText().toString().trim();
                String mNom = creation_compte_nom.getText().toString().trim();
                String mPrenom = creation_compte_prenom.getText().toString().trim();

                if (TextUtils.isEmpty(mEmail)) {
                    creation_compte_mail.setError("Le champ est vide");
                    return;
                }

                if (TextUtils.isEmpty(mPassword)) {
                    creation_compte_mdp.setError("Le champ est vide");
                    return;
                }

                if (TextUtils.isEmpty(mMdpConfirme)) {
                    creation_compte_mdp_confirme.setError("Le champ est vide");
                    return;
                }

                if (TextUtils.isEmpty(mNom)) {
                    creation_compte_nom.setError("Le champ est vide");
                    return;
                }

                if (TextUtils.isEmpty(mPrenom)) {
                    creation_compte_prenom.setError("Le champ est vide");
                    return;
                }

                if (mPassword.length() < 6) {
                    creation_compte_mdp.setError("Le mot de passe ne contient pas assez de caractère (6 minimum) ");
                    return;
                }

                if (!(mPassword.equals(mMdpConfirme))) {
                    creation_compte_mdp.setError("Le mots de passe ne correspondent pas ");
                    return;
                }
                mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser fuser = mAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(creation_compte.this, "Le mail de vérification à été envoyé", Toast.LENGTH_SHORT).show();
                                    enregisterAdulte();
                                    uploadFile();
                                    startActivity(new Intent(getApplicationContext(), connexion.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(creation_compte.this, "Erreur , le mail de vérification n'a pas été envoyé", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(creation_compte.this, "Erreur" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

        });

        avatar =findViewById(R.id.photo_profil_adulte);
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

    private void enregisterAdulte(){
        final FirebaseHandler fh = new FirebaseHandler();
        try {
            String userUid = mAuth.getCurrentUser().getUid();
            DatabaseReference mail = fh.getDatabaseReference().child(userUid).child("mail");
            DatabaseReference nom = fh.getDatabaseReference().child(userUid).child("nom");
            DatabaseReference prenom = fh.getDatabaseReference().child(userUid).child("prenom");
            DatabaseReference jetonParDef = fh.getDatabaseReference().child(userUid).child("jetonParDefault");
            jetonParDef.setValue(7);
            mail.setValue(creation_compte_mail.getText().toString());
            nom.setValue(creation_compte_nom.getText().toString());
            prenom.setValue(creation_compte_prenom.getText().toString());
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext() , "il y a eu un problème pour récupèrer les parametres", Toast.LENGTH_LONG).show();
            Intent co = new Intent(getApplicationContext(), connexion.class);
            finish();
            startActivity(co);
        }
    }

    public void onButtonShowPopupWindowClick(View view) {


        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window2, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 150);

        Button btn_photo = popupView.findViewById(R.id.popup_photo);
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    Toast.makeText(creation_compte.this, "Vous n'avez pas de caméra", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(creation_compte.this, "Impossible de prendre une photo", Toast.LENGTH_SHORT).show();
        }
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
                    Toast.makeText(creation_compte.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void demandePermissonPourLecture() {
        ActivityCompat.requestPermissions(creation_compte.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
    }
    private void demandePermissonPourCamera() {
        ActivityCompat.requestPermissions(creation_compte.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }
    private void demandePermissonPourEcriture() {
        ActivityCompat.requestPermissions(creation_compte.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == READ_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(creation_compte.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(creation_compte.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(creation_compte.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(creation_compte.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == WRITE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(creation_compte.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(creation_compte.this, "permission refusée", Toast.LENGTH_SHORT).show();
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
                creation_compte.this.getResources().getResourcePackageName(d)+ '/'
                + creation_compte.this.getResources().getResourceTypeName(d)
                + '/' + creation_compte.this.getResources().getResourceEntryName(d));
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
            ImageDecoder.Source source = ImageDecoder.createSource(creation_compte.this.getContentResolver(), uri);
            try {
                bmp = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bmp.compress(format, qualite, baos);
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
