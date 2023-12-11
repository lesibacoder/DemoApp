package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.models.UsersData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userID,image,surname,name,email,downloadUrl;
    private  final int REQ = 1;
    private Bitmap bitmap;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseReference = FirebaseDatabase.getInstance().getReference("UsersData");
        storageReference = FirebaseStorage.getInstance().getReference("UsersData");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = firebaseAuth.getCurrentUser().getUid();

        binding.imgLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(MainActivity.this,
                    "You signed out",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this,SignInActivity.class));
        });

        getData();

        binding.imgProfile.setOnClickListener(v -> openGallery());
        binding.btnUpdate.setOnClickListener(v -> isVerificationValid());
        binding.btnDelete.setOnClickListener(v -> deleteProfile());
    }

    private void showToastMessage(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void loadingProgress(boolean isLoading){
        if(isLoading){
            binding.btnUpdate.setVisibility(View.GONE);
            binding.pb.setVisibility(View.VISIBLE);
        }else{
            binding.btnUpdate.setVisibility(View.VISIBLE);
            binding.pb.setVisibility(View.GONE);
        }
    }

    private void loadingProgress1(boolean isLoading){
        if(isLoading){
            binding.btnDelete.setVisibility(View.GONE);
            binding.pb1.setVisibility(View.VISIBLE);
        }else{
            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.pb1.setVisibility(View.GONE);
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            binding.imgProfile.setImageBitmap(bitmap);
        }
    }

    private void getData() {
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsersData usersData = snapshot.getValue(UsersData.class);
                if(usersData != null){
                    image = usersData.getImage();
                    surname = usersData.getSurname();
                    name = usersData.getName();
                    email = usersData.getEmail();

                    binding.txtUserName.setText(surname+" "+name);
                    try {
                        Picasso.get().load(image).into(binding.imgProfile);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    binding.edtSurname.setText(surname);
                    binding.edtName.setText(name);
                    binding.edtEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Something went wrong: "
                                + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isVerificationValid() {
        surname = binding.edtSurname.getText().toString().trim();
        name = binding.edtName.getText().toString().trim();
        email = binding.edtEmail.getText().toString().trim();

        if(surname.isEmpty()){
            showToastMessage("Please enter surname");
            return false;
        }else if(name.isEmpty()){
            showToastMessage("Please enter name");
            return false;
        }else if(email.isEmpty()){
            showToastMessage("Please enter email address");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showToastMessage("Please enter valid email address");
            return false;
        }else if(bitmap == null){
            updateProfile(image);
            return false;
        }else {
            uploadImage();
            return true;
        }
    }

    private void uploadImage() {
        loadingProgress(true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] finalImg = byteArrayOutputStream.toByteArray();

        final StorageReference finalPath;
        finalPath = storageReference.child(finalImg+"jpg");
        final UploadTask uploadTask = finalPath.putBytes(finalImg);

        uploadTask.addOnCompleteListener(MainActivity.this, task -> {
            if(task.isSuccessful()){
                uploadTask.addOnSuccessListener(taskSnapshot ->
                        finalPath.getDownloadUrl().addOnSuccessListener(uri -> {
                            downloadUrl = String.valueOf(uri);
                            updateProfile(downloadUrl);
                            loadingProgress(false);
                        }));
            }else{
                showToastMessage("Something went wrong");
                loadingProgress(false);
            }
        });
    }

    private void updateProfile(String s) {

        loadingProgress(true);

        HashMap hashMap = new HashMap();
        hashMap.put("surname",surname);
        hashMap.put("name",name);
        hashMap.put("email",email);
        hashMap.put("image",s);

        databaseReference.child(userID).updateChildren(hashMap).addOnSuccessListener(o -> {
            showToastMessage("Profile Updated successfully");
            loadingProgress(false);
        }).addOnFailureListener(e -> {
            showToastMessage("Something went wrong: "+ e.getMessage());
            loadingProgress(false);
        });
    }

    private void deleteProfile() {
        loadingProgress1(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to delete your account?").setPositiveButton("Yes", (dialog, which) ->
                databaseReference.child(userID).removeValue().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        firebaseUser.delete();//delete user in firebase authentication.
                        showToastMessage("Profile deleted successfully");
                        startActivity(intent);
                        loadingProgress1(false);
                    }
                }).addOnFailureListener(e -> {
                    showToastMessage("Something went wrong: "+e.getMessage());
                    loadingProgress1(false);
                })).setNegativeButton("No", (dialog, which) ->
                loadingProgress1(false));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}