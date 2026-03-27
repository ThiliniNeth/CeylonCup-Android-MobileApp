package lk.javainstitute.ceyloncup;

import android.content.Intent;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class SigninActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText editTextEmail, editTextPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();

        Button signInButton = findViewById(R.id.login_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextEmail = findViewById(R.id.login_email);
                editTextPassword = findViewById(R.id.login_password);

                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (email.isBlank()) {
                    Toast.makeText(SigninActivity.this,"Please enter your Email.",Toast.LENGTH_LONG).show();

                } else if (!isValidEmail(email)) {
                    Toast.makeText(SigninActivity.this, "Please Enter valid Email", Toast.LENGTH_SHORT).show();

                }else if (password.isBlank()) {
                    Toast.makeText(SigninActivity.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();

                } else if (password.length()<8) {
                    Toast.makeText(SigninActivity.this, "Password must be at least 8 characters!", Toast.LENGTH_SHORT).show();

                }else{
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SigninActivity.this, "Sign-in Successfully.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SigninActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SigninActivity.this, "Sign-in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
            private boolean isValidEmail(String Email) {
                String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
                return Pattern.compile(emailPattern).matcher(Email).matches();
            }
        });

        TextView signupText = findViewById(R.id.signupRedirectText);
        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signupIntent = new Intent(SigninActivity.this,SignupActivity.class);
                startActivity(signupIntent);
                finish();
            }
        });

    }
}
