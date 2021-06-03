package com.uranites.shhhquiet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText emailLogin, passwordLogin, forgetPassEmail;
    Button buttonLogin, forgetPassButton;
    TextView donthaveanaccount, forgetPassword;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    //private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

        emailLogin = findViewById(R.id.lEmail);
        passwordLogin = findViewById(R.id.lPassword);
        progressBar = findViewById(R.id.lprogressBar);
        fAuth = FirebaseAuth.getInstance();
        buttonLogin = findViewById(R.id.login);
        donthaveanaccount = findViewById(R.id.donthaveanaccount);
        forgetPassword = findViewById(R.id.fPass);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String email = emailLogin.getText().toString().trim();
                String password = passwordLogin.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    emailLogin.setError("Email is Required...");
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    passwordLogin.setError("Password is Required...");
                    return;
                }

                if(password.length() < 6)
                {
                    passwordLogin.setError("Password must be greater or equal to 6 letters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(Login.this, "Logged in Successfull", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(view.getContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(Login.this, "Error is Occured"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                });

            }
        });

        donthaveanaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgetDialog();
            }
        });
    }

    void forgetDialog()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.forget_pass, null);
        forgetPassButton  = view.findViewById(R.id.fSubmit);
        forgetPassEmail = view.findViewById(R.id.femailAddress);

        final AlertDialog forgetalertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        forgetalertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        forgetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = forgetPassEmail.getText().toString();
                fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Login.this, "Password reset link sent successfully", Toast.LENGTH_SHORT).show();
                        forgetalertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, "Password reset link cannot be sent "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        forgetalertDialog.show();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}