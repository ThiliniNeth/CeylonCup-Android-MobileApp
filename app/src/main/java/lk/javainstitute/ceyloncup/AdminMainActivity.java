package lk.javainstitute.ceyloncup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminMainActivity extends AppCompatActivity {

    private static final int load_time = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ProgressBar progressBar=findViewById(R.id.progressBar);
        new Thread(() -> {
            for (int progress = 0; progress <= 100; progress += 5) {
                try {
                    Thread.sleep(load_time / 20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int finalProgress = progress;
                runOnUiThread(() -> progressBar.setProgress(finalProgress));
            }

            runOnUiThread(() -> {
                Intent intent = new Intent(AdminMainActivity.this, AdminLogInActivity.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }
}