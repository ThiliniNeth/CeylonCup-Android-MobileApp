package lk.javainstitute.ceyloncup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class locationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking); // Ensure tracking.xml exists

        Button buttonMap = findViewById(R.id.button); // Ensure button ID matches XML
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(locationActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
