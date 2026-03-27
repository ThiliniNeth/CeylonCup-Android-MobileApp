package lk.javainstitute.ceyloncup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth firebaseAuth;

    private EditText username, email, mobile, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button button5 = findViewById(R.id.signup_button);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                username = findViewById(R.id.signup_username);
                email = findViewById(R.id.signup_email);
                mobile = findViewById(R.id.signup_mobile);
                password = findViewById(R.id.signup_password);

                String Username = username.getText().toString().trim();
                String Email = email.getText().toString().trim();
                String Mobile = mobile.getText().toString().trim();
                String Password = password.getText().toString().trim();


                if (Username.isBlank()) {
                    Toast.makeText(SignupActivity.this, "Please Enter Your User Name", Toast.LENGTH_SHORT).show();

                } else if (Email.isBlank()) {
                    Toast.makeText(SignupActivity.this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();

                } else if (!isValidEmail(Email)) {
                    Toast.makeText(SignupActivity.this, "Please Enter valid Email", Toast.LENGTH_SHORT).show();

                } else if (Mobile.isBlank()) {
                    Toast.makeText(SignupActivity.this, "Please Enter Your Mobile Number", Toast.LENGTH_SHORT).show();

                } else if (Mobile.length() != 10) {
                    Toast.makeText(SignupActivity.this, "Please Enter 10-digit Number", Toast.LENGTH_SHORT).show();

                } else if (!isValidMobilel(Mobile)) {
                    Toast.makeText(SignupActivity.this, "Please Enter Valid mobile", Toast.LENGTH_SHORT).show();

                } else if (Password.isBlank()) {
                    Toast.makeText(SignupActivity.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();

                } else if (Password.length() < 8) {
                    Toast.makeText(SignupActivity.this, "Password must be at least 8 characters!", Toast.LENGTH_SHORT).show();

                } else {

                    firebaseAuth = FirebaseAuth.getInstance();

                    firebaseAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                String userId = firebaseAuth.getCurrentUser().getUid();
                                saveUser(Username, Email, Mobile, Password);
                            } else {

                                Toast.makeText(SignupActivity.this, "Sign-up failed", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }

            }


            private boolean isValidEmail(String Email) {
                String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
                return Pattern.compile(emailPattern).matcher(Email).matches();
            }

            private boolean isValidMobilel(String Mobile) {
                String mobilePattern = "^07[01245678]{1}[0-9]{7}$";
                return Pattern.compile(mobilePattern).matcher(Mobile).matches();
            }

        });


        TextView textView8 = findViewById(R.id.loginRedirectText);
        textView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                startActivity(intent);


            }
        });


    }

    private void saveUser(String Username, String Email, String Mobile, String Password) {

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("user")
                .whereEqualTo("email", Email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            QuerySnapshot document = task.getResult();

                            if (document != null && !document.isEmpty()) {
                                Toast.makeText(SignupActivity.this, "This email already used ", Toast.LENGTH_SHORT).show();

                            } else {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("username", Username);
                                hashMap.put("email", Email);
                                hashMap.put("mobile", Mobile);
                                hashMap.put("password", Password);

                                firebaseFirestore.collection("user").add(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {

                                                Log.d("log25", "user document successfully");
                                                Toast.makeText(SignupActivity.this, "user registration success", Toast.LENGTH_SHORT).show();

                                                username.setText("");
                                                email.setText("");
                                                mobile.setText("");
                                                password.setText("");

                                                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                                                startActivity(intent);



                                                NotificationManager notificationManager = getSystemService(NotificationManager.class);


                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                                    NotificationChannel notificationChannel = new NotificationChannel(
                                                            "C1",
                                                            "Chanel1",
                                                            NotificationManager.IMPORTANCE_DEFAULT
                                                    );


                                                    notificationManager.createNotificationChannel(notificationChannel);

                                                }


                                                Notification notification = new NotificationCompat.Builder(SignupActivity.this, "C1")
                                                        .setContentTitle("User Registration success ")
                                                        .setContentText("Welcome CeylonCup")

                                                        .setPriority(Notification.PRIORITY_DEFAULT)
                                                        .build();

                                                notificationManager.notify(1, notification);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("log25", "user document error");

                                            }
                                        });

                            }


                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }


}

