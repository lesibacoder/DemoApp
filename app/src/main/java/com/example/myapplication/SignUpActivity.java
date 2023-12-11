package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySignUpBinding;
import com.example.myapplication.models.UsersData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private final int REQ = 1;
    private Bitmap bitmap;
    private String userID,image,surname,name,email,password;

    private ActivitySignUpBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseReference = FirebaseDatabase.getInstance().getReference("UsersData");
        storageReference = FirebaseStorage.getInstance().getReference("UsersData");
        firebaseAuth = FirebaseAuth.getInstance();

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        binding.txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
            }
        });
        binding.imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVerificationValid()){
                    uploadImage();
                }
            }
        });
    }

    
    private void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void loadingProgress(boolean isLoading){
        if(isLoading){
            binding.btnSignUp.setVisibility(View.GONE);
            binding.pb.setVisibility(View.VISIBLE);
        }else{
            binding.btnSignUp.setVisibility(View.VISIBLE);
            binding.pb.setVisibility(View.GONE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQ && resultCode==RESULT_OK){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            binding.imgProfile.setImageBitmap(bitmap);
            binding.txtAdd.setVisibility(View.GONE);
        }
    }
    private boolean isVerificationValid(){

        surname = binding.edtSurname.getText().toString().trim();
        name = binding.edtName.getText().toString().trim();
        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();

        if(bitmap == null){
            showToastMessage("Please upload image");
            return false;
        }else if(surname.isEmpty()){
            showToastMessage("please enter surname");
            return false;
        } else if (name.isEmpty()) {
            showToastMessage("Please enter name");
            return false;
        } else if (email.isEmpty()) {
            showToastMessage("Please enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToastMessage("Please enter valid email address");
            return false;
        } else if (password.isEmpty()) {
            showToastMessage("Please enter password");
            return false;
        } else if (password.length() != 6 && password.length() < 6) {
            showToastMessage("Please enter at least 6 characters of password");
            return false;
        }else {
            return true;
        }

    }

    private void uploadImage(){
        loadingProgress(true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] finalImg = byteArrayOutputStream.toByteArray();
        final StorageReference finalPath;
        finalPath = storageReference.child(finalImg+"jpg");
        final UploadTask uploadTask = finalPath.putBytes(finalImg);

        uploadTask.addOnCompleteListener(SignUpActivity.this, task -> {
            if(task.isSuccessful()){
                uploadTask.addOnSuccessListener(taskSnapshot ->
                        finalPath.getDownloadUrl().addOnSuccessListener(uri -> {
                    image = String.valueOf(uri);
                    signUp();
                    loadingProgress(false);

                }));
            }else{
                showToastMessage("Something went wrong");
                loadingProgress(false);
            }

        });
    }
    private void signUp(){
        loadingProgress(true);
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                UsersData usersData = new UsersData(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        image,surname,name,email,password);
                databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(usersData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){


                                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                    showToastMessage("Successful");
                                    loadingProgress(false);

                                }
                                else{
                                    showToastMessage("wrong credentials");
                                    loadingProgress(false);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showToastMessage("Failed: "+e.getMessage());
                                loadingProgress(false);
                            }
                        });

            }
        });


    }
    


}