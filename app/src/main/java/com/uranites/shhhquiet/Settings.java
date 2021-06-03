package com.uranites.shhhquiet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Settings extends AppCompatActivity {

    public static final String TAG = "TAG";

    TextView changepass, feedback, aboutus, updateworklocation;
    EditText sendquery, oldpass, newpass, confirmnewpass;
    Button query, confirmButton;
    ImageView logout, back;

    FirebaseUser user;
    FirebaseAuth fAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        changepass = findViewById(R.id.changepass);
        feedback = findViewById(R.id.feedback);
        aboutus = findViewById(R.id.aboutus);
        logout = findViewById(R.id.logout);
        back = findViewById(R.id.back);
        updateworklocation = findViewById(R.id.updateworklocation);

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        updateworklocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateworkloc();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logout();
            }
        });

        changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Feedback();
            }
        });

        aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Aboutus();
            }
        });
    }

    void changePassword()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.change_password, null);

        confirmButton = view.findViewById(R.id.confirmButton);
        oldpass = view.findViewById(R.id.oldpass);
        newpass = view.findViewById(R.id.newpass);
        confirmnewpass = view.findViewById(R.id.confirmnewpass);

        final AlertDialog confirmalertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        confirmalertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String op = oldpass.getText().toString();
                final String np = newpass.getText().toString();
                final String cnp = confirmnewpass.getText().toString();
                String userEmail = user.getEmail();

                if(TextUtils.isEmpty(op))
                {
                    oldpass.setError("Old Password is Required...");
                    return;
                }

                if(TextUtils.isEmpty(np))
                {
                    newpass.setError("New Password is Required...");
                    return;
                }

                if(np.length() < 6)
                {
                    newpass.setError("Password must be greater or equal to 6 letters");
                    return;
                }

                if(!np.equals(cnp))
                {
                    confirmnewpass.setError("Password confirmation failed...");
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(userEmail, op);

                user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.updatePassword(np).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Settings.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                confirmalertDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Settings.this, "Password cannot be changed, try resetting the password", Toast.LENGTH_SHORT).show();
                                confirmalertDialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Settings.this, "Old Password is Incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        confirmalertDialog.show();
    }

    void updateworkloc()
    {
        String uLat = String.valueOf(LocationService.latitude);
        String uLon = String.valueOf(LocationService.longitude);

        Map<String, Object> workloc = new HashMap<>();
        workloc.put("lat", uLat.substring(0,6));
        workloc.put("lon", uLon.substring(0,6));

        String emailId = fAuth.getCurrentUser().getEmail();

        db.collection(emailId).document("workloc")
                .set(workloc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(Settings.this, "Work location updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(Settings.this, "Work location can't be updated, please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void Feedback()
    {
        Toast.makeText(this, "Feed Working", Toast.LENGTH_SHORT).show();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.feedback, null);

        sendquery = view.findViewById(R.id.sendquery);
        query = view.findViewById(R.id.query);

        final AlertDialog emailDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        emailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        sendquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String queryemail = user.getEmail();
                String[] emailTo = {"uranite.corporation@gmail.com"};
                String sub = "Regarding Shhh Quiet mailId: "+queryemail;
                String q = query.getText().toString();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, emailTo);
                intent.putExtra(Intent.EXTRA_SUBJECT, sub);
                intent.putExtra(Intent.EXTRA_TEXT, q);

                intent.setType("email/rfc822");
                startActivity(Intent.createChooser(intent, "Choose an Email Client"));
                emailDialog.dismiss();
            }
        });
        emailDialog.show();
    }

    void Aboutus()
    {
        String url = "http://www.uranites.tech";
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage("com.android.chrome");
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            i.setPackage(null);
            startActivity(i);
        }
    }

    void Logout()
    {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}