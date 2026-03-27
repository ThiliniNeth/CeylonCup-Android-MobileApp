package lk.javainstitute.ceyloncup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import lk.javainstitute.ceyloncup.model.User;

public class UserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<User> userArrayList;
    private UserAdapter userAdapter;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load users with valid data
        userArrayList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerViewUsers);

        LinearLayoutManager layoutManager = new LinearLayoutManager(UserActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        userAdapter = new UserAdapter(this, userArrayList);
        recyclerView.setAdapter(userAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();

        //load database users
        loadUsers();
    }

    private void loadUsers() {
        CollectionReference usersRef = firebaseFirestore.collection("user");
        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(UserActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
                    return;
                }

                userArrayList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    User user = doc.toObject(User.class);
                    user.setUid(doc.getId());  // Store Firestore document ID
                    userArrayList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }
        });
    }
}

class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private final ArrayList<User> usersArrayList;

    private FirebaseFirestore firebaseFirestore;

    public UserAdapter(Context context, ArrayList<User> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userEmail;
        Switch userSwitch;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.textView45);
            userSwitch = itemView.findViewById(R.id.switch1);
        }
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.useritem, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = usersArrayList.get(position);

        holder.userEmail.setText(user.getEmail());
        holder.userSwitch.setChecked(user.isActive());

        // Handle switch toggle event
        holder.userSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            user.setActive(isChecked);
            updateUserStatus(user);
        });
    }

    private void updateUserStatus(User user) {
        firebaseFirestore.collection("user").document(user.getUid())
                .update("active", user.isActive())
                .addOnSuccessListener(aVoid -> {
                    String message = user.isActive() ? "User Activated" : "User Deactivated";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update user status", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }
}