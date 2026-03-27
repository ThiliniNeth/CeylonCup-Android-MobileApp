package lk.javainstitute.ceyloncup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminLogInActivity extends AppCompatActivity {

    private static final String admin_email = "thilininethmini890@gmail.com";
    private static final String admin_password = "thilini123@";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageView = findViewById(R.id.imageView2);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim);
        imageView.startAnimation(animation);

        EditText editTextEmail = findViewById(R.id.EditText1);
        EditText editTextPassword = findViewById(R.id.EditText2);
        Button signInButton = findViewById(R.id.button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(AdminLogInActivity.this,"Email is required.",Toast.LENGTH_SHORT).show();

                }
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    Toast.makeText(AdminLogInActivity.this,"Invalid Email.",Toast.LENGTH_SHORT).show();

                }
                if (password.isEmpty()) {
                    Toast.makeText(AdminLogInActivity.this,"Password is required.",Toast.LENGTH_SHORT).show();

                }
                if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
                    Toast.makeText(AdminLogInActivity.this,"Password must be at least 8 characters, contain letters, numbers, and a special character.",Toast.LENGTH_SHORT).show();

                }

                // Check admin credentials
                if (email.equals(admin_email) && password.equals(admin_password)) {
                    Toast.makeText(AdminLogInActivity.this,"Login Successfully.",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AdminLogInActivity.this, AdminHomeActivity.class);
                        startActivity(intent);
                        finish();

                } else {
                    Toast.makeText(AdminLogInActivity.this,"Invalid email or password.",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}