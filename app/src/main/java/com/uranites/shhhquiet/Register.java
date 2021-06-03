package com.uranites.shhhquiet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText emailSignup, passwordSignup, cpasswordSignup;
    Button buttonSignup;
    TextView alreadyhaveanaccount;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    //private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /*MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        emailSignup = findViewById(R.id.rEmail);
        passwordSignup = findViewById(R.id.rPass);
        cpasswordSignup = findViewById(R.id.crPass);
        buttonSignup = findViewById(R.id.signup);
        alreadyhaveanaccount = findViewById(R.id.alreadyhaveanaccount);

        fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.rprogressBar);

        if(fAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        buttonSignup.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(final View view) {
                String email = emailSignup.getText().toString().trim();
                String password = passwordSignup.getText().toString().trim();
                String cpassword = cpasswordSignup.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    emailSignup.setError("Email is Required...");
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    passwordSignup.setError("Password is Required...");
                    return;
                }

                if(password.length() < 6)
                {
                    passwordSignup.setError("Password must be greater or equal to 6 letters");
                    return;
                }

                if(!password.equals(cpassword))
                {
                    cpasswordSignup.setError("Password confirmation failed...");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {

                            Map<String, Object> appStatus = new HashMap<>();
                            appStatus.put("appstatus", 0);

                            db.collection(email).document("appstatus")
                                    .set(appStatus)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                            Toast.makeText(Register.this, "Your Profile Created Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(view.getContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(Register.this, "Error is Occured"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });

        alreadyhaveanaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Login.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

}