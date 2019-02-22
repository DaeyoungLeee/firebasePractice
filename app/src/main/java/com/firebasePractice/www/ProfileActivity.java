package com.firebasePractice.www;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    private Button btn_save;
    private StorageReference storageRef;
    private FirebaseStorage storage;
    private ImageView img_profile;
    private EditText edt_title, edt_description;
    private String imagePath;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    private static final int GALLERY_CODE = 12;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btn_save = findViewById(R.id.btn_save);
        img_profile = findViewById(R.id.img_title);
        edt_description = findViewById(R.id.edt_description);
        edt_title = findViewById(R.id.edt_title);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                upload(imagePath);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == GALLERY_CODE){

            imagePath = getPath(data.getData());

            File f = new File(imagePath);

            img_profile.setImageURI(Uri.fromFile(f));
        }
    }

    public String getPath(Uri uri){

        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri,proj, null,null,null);

        Cursor cursoor = cursorLoader.loadInBackground();
        int index = cursoor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursoor.moveToFirst();

        return cursoor.getString(index);
    }

    private void upload(String uri){
        Uri file = Uri.fromFile(new File(uri));
        StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                //흠,,,수정 필요
                Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();

                ImageDTO imageDTO = new ImageDTO();
                imageDTO.imageUrl = downloadUrl.toString();
                imageDTO.title = edt_title.getText().toString();
                imageDTO.description = edt_description.getText().toString();


                database.getReference().child("image").push().setValue(imageDTO);   //push를 안넣으면 누를 때마다 쌓이지 않고 push() 넣어주면 데이터가 여러번 누를수록 쌓임
            }
        });
    }
}
