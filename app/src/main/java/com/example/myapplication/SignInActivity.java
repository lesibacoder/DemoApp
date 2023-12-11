package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String email,password;
    private ActivitySignInBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
            }
        });
        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
        binding.txtResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,ResetPasswordActivity.class));
            }
        });
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVerificationValid()){
                    signIn();
                }
            }
        });

    }
    private void showToastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void loadingProgress(boolean isLoading){
        if(isLoading){
            binding.btnSignIn.setVisibility(View.GONE);
            binding.pb.setVisibility(View.VISIBLE);
        }else{
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.pb.setVisibility(View.GONE);
        }
    }
    private boolean isVerificationValid(){
        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();

        if(email.isEmpty()){
            showToastMessage("please enter email address");
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

    private void signIn() {
        loadingProgress(true);
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    binding.edtEmail.setText("");
                    binding.edtPassword.setText("");
                    loadingProgress(false);
                }else{
                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                    showToastMessage("Email not registered! " +
                            "Please check your emails to verify your account");
                    loadingProgress(false);
                }
            }else{
                showToastMessage("Something went wrong.");
            }
        }).addOnFailureListener(e -> {
            showToastMessage(e.getMessage());
            loadingProgress(false);
        });
    }
}