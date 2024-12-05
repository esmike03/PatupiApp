package com.patupiapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginBusinessActivity extends AppCompatActivity {

    EditText edemail, edpass;
    Button buttonLogin, buttonbusiness;
    TextView buttonText1, forgot;
    ImageButton googleAuth;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 20;
    private ProgressDialog progressDialog;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginbusiness);

        progressDialog = new ProgressDialog(this);
        // Check for location permission
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//        googleAuth = findViewById(R.id.imageButton);
        buttonText1 = findViewById(R.id.textView5);

        edemail = findViewById(R.id.emaillogin);
        edpass = findViewById(R.id.inputpassword);
        buttonLogin = findViewById(R.id.button);
        buttonbusiness = findViewById(R.id.button2);

        forgot = findViewById(R.id.forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginBusinessActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        checkBox();
        check();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        googleAuth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                progressDialog.setTitle("Log In");
//                progressDialog.setMessage("please wait...");
//                progressDialog.setCanceledOnTouchOutside(true);
//                progressDialog.show();
//                googleSignIn();
//            }
//        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = edemail.getText().toString();
                password = edpass.getText().toString();
                progressDialog.setTitle("Log In");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();

                if (TextUtils.isEmpty(email)) {
                    edemail.setError("Required!");
                    progressDialog.dismiss();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginBusinessActivity.this, "Password is required!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Check if the "business" field is empty in the database
                                        String userId = auth.getCurrentUser().getUid();
                                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(userId);

                                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    String business = snapshot.child("business").getValue(String.class);
                                                    if (TextUtils.isEmpty(business)) {
                                                        // Business field is empty
                                                        Toast.makeText(LoginBusinessActivity.this, "Not a Business Account!", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                        auth.signOut(); // Log out the user if the field is empty
                                                    } else {
                                                        // Proceed to next activity
                                                        Toast.makeText(LoginBusinessActivity.this, "Sign In Successfully!",
                                                                Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                        saveLoginStatus(true);
                                                        Intent intent = new Intent(LoginBusinessActivity.this, BusinessHomeActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                } else {
                                                    // User data not found in the database
                                                    Toast.makeText(LoginBusinessActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(LoginBusinessActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        });
                                    } else {
                                        // Authentication failed
                                        Toast.makeText(LoginBusinessActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        saveLoginStatus(false);
                                    }
                                }
                            });
                }
            }
        });



        String text =  "Don't have any account? Sign Up";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ForegroundColorSpan bGreen = new ForegroundColorSpan(Color.GREEN);
        ssb.setSpan(bGreen, 24, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        buttonText1.setText(ssb);

        buttonText1.setOnClickListener(v -> {
            Intent intent = new Intent(LoginBusinessActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        buttonbusiness.setOnClickListener(v -> {
            Intent intent = new Intent(LoginBusinessActivity.this, BusinessActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to get the location

            }
        }
    }

    private void getLastKnownLocation() {
        // Get the last known location
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    if (location != null) {
                        // Use the location
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Now
                    }
                }
            });
        }
    }

    private void checkBox() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String check = sharedPreferences.getString("name", "");
        if(check.equals("true")){
            Intent intent = new Intent(LoginBusinessActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void check() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(LoginBusinessActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }


    private void googleSignIn() {

        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (Exception e) {
                Toast.makeText(this, "Error:" + e, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }

    private void firebaseAuth(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", user.getUid());
                            map.put("name", user.getDisplayName());
                            map.put("email", user.getEmail());
                            map.put("profile", user.getPhotoUrl().toString());

                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", "true");
                            editor.apply();

                            database.getReference().child("users").child(user.getUid()).setValue(map);
                            Toast.makeText(LoginBusinessActivity.this, "Log In Successfully", Toast.LENGTH_SHORT ).show();
                            Intent intent = new Intent(LoginBusinessActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                            progressDialog.dismiss();

                        }else{
                            Toast.makeText(LoginBusinessActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT ).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

}