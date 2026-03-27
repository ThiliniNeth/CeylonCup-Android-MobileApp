package lk.javainstitute.ceyloncup;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class userprofileFragment extends Fragment {

    private EditText editFName, editLName, editMobile, editEmail, editPassword;
    private Button saveButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editFName = view.findViewById(R.id.editFName);
        editLName = view.findViewById(R.id.editLName);
        editMobile = view.findViewById(R.id.editMobile);
        editEmail = view.findViewById(R.id.editEmail);
        editPassword = view.findViewById(R.id.editPassword);
        saveButton = view.findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserData();

        saveButton.setOnClickListener(v -> updateProfile());

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Get basic info from FirebaseAuth
            editEmail.setText(user.getEmail());
            if (user.getDisplayName() != null) {
                String[] nameParts = user.getDisplayName().split(" ", 2);
                editFName.setText(nameParts[0]);
                if (nameParts.length > 1) editLName.setText(nameParts[1]);
            }

            // Fetch additional data from Firestore
            DocumentReference docRef = firestore.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    editMobile.setText(documentSnapshot.getString("mobile"));
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(getActivity(), "Failed to load profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String fname = editFName.getText().toString().trim();
        String lname = editLName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String mobile = editMobile.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fname) || TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "First name and email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Update FirebaseAuth Display Name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fname + " " + lname)
                .build();

        user.updateProfile(profileUpdates);

        // 2. Update FirebaseAuth Email
        if (!user.getEmail().equals(email)) {
            user.updateEmail(email).addOnFailureListener(e ->
                    Toast.makeText(getActivity(), "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // 3. Update FirebaseAuth Password
        if (!TextUtils.isEmpty(password)) {
            user.updatePassword(password).addOnFailureListener(e ->
                    Toast.makeText(getActivity(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // 4. Update Firestore user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", fname);
        userData.put("lastName", lname);
        userData.put("mobile", mobile);
        userData.put("email", email); // optional

        firestore.collection("user").document(((FirebaseUser) user).getUid())
                .set(userData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
