package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityResetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    private String email;
    private ActivityResetPasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth =FirebaseAuth.getInstance();

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResetPasswordActivity.this, SignInActivity.class));
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVerificationValid()){
                    resetPassword();
                }
            }
        });


    }
    private void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void loadingProgress(boolean isLoading){
        if(isLoading){
            binding.btnSubmit.setVisibility(View.GONE);
            binding.pb.setVisibility(View.VISIBLE);
        }else{
            binding.btnSubmit.setVisibility(View.VISIBLE);
            binding.pb.setVisibility(View.GONE);
        }
    }
    private boolean isVerificationValid(){
        email = binding.edtEmail.getText().toString().trim();

        if(email.isEmpty()){
            showToastMessage("Please enter email address");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToastMessage("Please enter valid email address");
            return false;
        }else{
            return true;
        }
    }
    private void resetPassword(){
        loadingProgress(true);
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    startActivity(new Intent(ResetPasswordActivity.this,SignInActivity.class));
                    showToastMessage("Please check your emails to change your password");
                    loadingProgress(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToastMessage(e.getMessage());
                loadingProgress(false);
            }
        });


    }
}