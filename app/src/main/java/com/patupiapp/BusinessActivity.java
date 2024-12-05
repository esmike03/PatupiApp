package com.patupiapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BusinessActivity extends AppCompatActivity {

    Button push;
    TextView buttonText1;

    EditText name, email1, pass1, passcon, businessname;
    String userName, userEmail, userPass, userPassCon, business;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        push = findViewById(R.id.create);
        buttonText1 = findViewById(R.id.textView9);
        name = findViewById(R.id.inputname);
        businessname = findViewById(R.id.businessname);
        email1 = findViewById(R.id.inputemail);
        pass1 = findViewById(R.id.inputpass);
        passcon = findViewById(R.id.inputpasscfm);
        push = findViewById(R.id.create);


        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setTitle("Creating Account");
                progressDialog.setMessage("please wait...");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
                userName = name.getText().toString().trim();
                business = businessname.getText().toString().trim();
                userEmail = email1.getText().toString().trim();
                userPass = pass1.getText().toString().trim();
                userPassCon = passcon.getText().toString().trim();

                if (TextUtils.isEmpty(userName)) {
                    name.setError("Required!");
                    progressDialog.dismiss();
                } else if (TextUtils.isEmpty(userEmail)) {
                    email1.setError("Required!");
                    progressDialog.dismiss();
                } else if (TextUtils.isEmpty(userPass) || TextUtils.isEmpty(userPassCon)) {
                    Toast.makeText(BusinessActivity.this, "Password is empty!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else if (!userPass.equals(userPassCon)) {
                    Toast.makeText(BusinessActivity.this, "Password don't match!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else if(TextUtils.isEmpty(business)){
                    businessname.setError("Required!");
                    progressDialog.dismiss();
                }else {

                    String email, password;
                    userName = name.getText().toString().trim();
                    business = businessname.getText().toString().trim();
                    email = email1.getText().toString().trim();
                    password = pass1.getText().toString().trim();

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(BusinessActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                // Get the exception and display a specific message based on the error code
                                Exception e = task.getException();
                                if (e instanceof FirebaseAuthException) {
                                    String errorMessage = handleFirebaseAuthException((FirebaseAuthException) e);
                                    Toast.makeText(BusinessActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                } else {
                                    // Handle other exceptions (optional)
                                    Toast.makeText(BusinessActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                            reference = database.getReference("users");
                            FirebaseUser firebaseuser = mAuth.getCurrentUser();
                            String uid = firebaseuser.getUid();  // Get the unique user ID
                            String noProfile = "https://marketplace.canva.com/EAF6DOq8wwA/1/0/1600w/canva-black-and-white-modern-illustrative-barbershop-logo-MnTa2CvxlTE.jpg";

                            User user = new User(email, uid, userName, noProfile, business);
                            reference.child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // User data stored successfully
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(BusinessActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle database errors
                                    Toast.makeText(BusinessActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                }
            }

            private String handleFirebaseAuthException(FirebaseAuthException e) {
                String errorMessage = e.getMessage();
                if (e instanceof FirebaseAuthUserCollisionException) {
                    errorMessage = "The email address is already in use.";
                    progressDialog.dismiss();
                } else if (e.getMessage().contains("weak password")) {
                    errorMessage = "The password is too weak.";
                    progressDialog.dismiss();
                } else {
                    // Handle other errors
                    progressDialog.dismiss();
                }
                return errorMessage;

            }
        });


        String text = "Already have an account? Sign In";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ForegroundColorSpan bGreen = new ForegroundColorSpan(Color.GREEN);
        ssb.setSpan(bGreen, 24, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        buttonText1.setText(ssb);

        buttonText1.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessActivity.this, LoginBusinessActivity.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}