package com.example.token_eco;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
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
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
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

public class recompense_creation extends menu_handler implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {
    private EditText titreRecompense;
    private int nbBravos_spinner;
    private int PICK_IMAGE_REQUEST = 1 ;
    private int CAMERA_REQUEST_CODE = 2 ;
    private int READ_PERMISSION_CODE = 1;
    private int CAMERA_PERMISSION_CODE = 2;
    private int WRITE_PERMISSION_CODE = 3;
    private String nom_image;
    private List<File> photos;
    public ImageView img;
    private DatabaseReference enfantExiste, drSync, mDatabaseRef;
    private ValueEventListener eventEnfantExiste, eventSync;
    private FirebaseHandler fh;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recompense_creation);
        Button button_creer_recompense = findViewById(R.id.button_creer_recompense);
        photos = new ArrayList<>();
        Spinner spinner = findViewById(R.id.spinner_nbBravos);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.bravos_array,R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        titreRecompense = findViewById(R.id.creation_recompense_intitule);
        titreRecompense.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        super.setupToolBar();
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

        button_creer_recompense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(titreRecompense.getText())){
                    titreRecompense.setError("La récompense n'a pas de titre");
                    return;
                }
                enregisterRecompense();
            }
        });

        img =findViewById(R.id.image_recompense);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popup = inflater.inflate(R.layout.popup_window2, null);
                popup.setBackground(getDrawable(R.drawable.notif_border));
                onButtonShowPopupWindowClick(popup);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.nbBravos_spinner = Integer.parseInt(parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        this.nbBravos_spinner = 1;
    }

    public void enregisterRecompense(){
        final DatabaseReference recompense = fh.getDatabaseReferenceEnfant(getApplicationContext()).child("recompenses");
        recompense.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> recomp = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    recomp.add(ds.child("titre").getValue(String.class).toLowerCase().trim());
                }
                if(recomp.contains(titreRecompense.getText().toString().toLowerCase().trim())){
                    titreRecompense.setError("Recompense déjà créee.");
                }else{
                    String push = recompense.push().getKey();
                    recompense.child(push).setValue(new Post(titreRecompense.getText().toString(), false, nbBravos_spinner));
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

    @Override
    protected void onDestroy() {
        for(File f : photos){
            if(f!=null){
                f.delete();
            }
        }
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        enfantExiste.removeEventListener(eventEnfantExiste);
        drSync.removeEventListener(eventSync);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        enfantExiste.addValueEventListener(eventEnfantExiste);
        drSync.addValueEventListener(eventSync);
    }

    public class Post {
        public String titre;
        public Boolean estReclamee;
        public int coutBravos;

        public Post(String titre, Boolean estReclamee, int coutBravos) {
            this.titre=titre;
            this.estReclamee=estReclamee;
            this.coutBravos=coutBravos;
        }
    }

    public void onButtonShowPopupWindowClick(View view) {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window2, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
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
                    Toast.makeText(recompense_creation.this, "Vous n'avez pas de caméra", Toast.LENGTH_SHORT).show();
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
        popup_image1.setImageResource(R.drawable.cadeau1);
        popup_image2.setImageResource(R.drawable.cadeau2);
        popup_image3.setImageResource(R.drawable.cadeau3);
        popup_image4.setImageResource(R.drawable.cadeau4);
        popup_image5.setImageResource(R.drawable.cadeau5);
        popup_image6.setImageResource(R.drawable.cadeau6);
        popup_image7.setImageResource(R.drawable.cadeau7);
        popup_image8.setImageResource(R.drawable.cadeau8);
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

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        Button valider = popupView.findViewById(R.id.selectionner_image);
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                mImageUri = null;
                if(image.getId()==R.id.popup_image1){
                    nom_image = "image1";
                    Picasso.get().load(R.drawable.cadeau1).into(img);
                } else if(image.getId()==R.id.popup_image2){
                    nom_image = "image2";
                    Picasso.get().load(R.drawable.cadeau2).into(img);
                } else if(image.getId()==R.id.popup_image3){
                    nom_image = "image3";
                    Picasso.get().load(R.drawable.cadeau3).into(img);
                } else if(image.getId()==R.id.popup_image4){
                    nom_image = "image4";
                    Picasso.get().load(R.drawable.cadeau4).into(img);
                } else if(image.getId()==R.id.popup_image5){
                    nom_image = "image5";
                    Picasso.get().load(R.drawable.cadeau5).into(img);
                } else if(image.getId()==R.id.popup_image6){
                    nom_image = "image6";
                    Picasso.get().load(R.drawable.cadeau6).into(img);
                } else if(image.getId()==R.id.popup_image7){
                    nom_image = "image7";
                    Picasso.get().load(R.drawable.cadeau7).into(img);
                } else if(image.getId()==R.id.popup_image8){
                    nom_image = "image8";
                    Picasso.get().load(R.drawable.cadeau8).into(img);
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
            Toast.makeText(recompense_creation.this, "Impossible de prendre une photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).fit().into(img);
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Picasso.get().load(mImageUri).into(img);
        }
    }

    private void uploadFile(String push) throws IOException {
        FirebaseHandler fh2 = new FirebaseHandler();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref;
        pref = this.getSharedPreferences("pref", MODE_PRIVATE);
        String enfant = pref.getString("enfant", "no data");
        mDatabaseRef = fh2.getDatabaseReferenceEnfant(getApplicationContext()).child("recompenses").child(push).child("image");
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).child("enfants").child(enfant).child("recompenses").child(push).child("image");
        if (mImageUri != null) {
            Bitmap bmp;
            if(Build.VERSION.SDK_INT < 28) {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
            }else{
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), mImageUri);
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
                    Toast.makeText(recompense_creation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (nom_image!=null){
            mDatabaseRef.setValue(nom_image);
            nom_image = null;
        }
    }

    private void demandePermissonPourLecture() {
        ActivityCompat.requestPermissions(recompense_creation.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
    }
    private void demandePermissonPourCamera() {
        ActivityCompat.requestPermissions(recompense_creation.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }
    private void demandePermissonPourEcriture() {
        ActivityCompat.requestPermissions(recompense_creation.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == READ_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(recompense_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(recompense_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(recompense_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(recompense_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == WRITE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(recompense_creation.this, "permission accordée", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(recompense_creation.this, "permission refusée", Toast.LENGTH_SHORT).show();
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
