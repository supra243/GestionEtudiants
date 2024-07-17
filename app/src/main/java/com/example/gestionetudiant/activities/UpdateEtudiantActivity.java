package com.example.gestionetudiant.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gestionetudiant.R;
import com.example.gestionetudiant.models.EtudiantModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UpdateEtudiantActivity extends AppCompatActivity {

    ImageView updateImage;
    Button updateButton;
    EditText updateNom, updatePostNom, updatePrenom, updateMotDePasse, updateEmail;
    String nom, postNom, prenom, motDePasse, email;
    String imageUrl;
    String idEtudiant, oldImageURL;
    Uri uri;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_etudiant);

        updateButton = findViewById(R.id.updateButton);
        updateNom = findViewById(R.id.updateNom);
        updatePostNom = findViewById(R.id.updatePostNom);
        updatePrenom = findViewById(R.id.updatePrenom);
        updateEmail = findViewById(R.id.updateEmail);
        updateMotDePasse = findViewById(R.id.updateMotDePasse);
        updateImage = findViewById(R.id.updateImage);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                uri = data.getData();
                                updateImage.setImageURI(uri);
                            }
                        } else {
                            Toast.makeText(UpdateEtudiantActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Glide.with(UpdateEtudiantActivity.this).load(bundle.getString("Image")).into(updateImage);
            updateNom.setText(bundle.getString("Nom"));
            updatePostNom.setText(bundle.getString("PostNom"));
            updatePrenom.setText(bundle.getString("Prenom"));
            updateEmail.setText(bundle.getString("Email"));
            updateMotDePasse.setText(bundle.getString("MotDePasse"));

            idEtudiant = bundle.getString("idEtudiant");
            oldImageURL = bundle.getString("Image");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("etudiant").child(idEtudiant);

        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fieldsAreValid()) {
                    saveData();
                } else {
                    Toast.makeText(UpdateEtudiantActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean fieldsAreValid() {
        return !updateNom.getText().toString().trim().isEmpty() &&
                !updatePostNom.getText().toString().trim().isEmpty() &&
                !updatePrenom.getText().toString().trim().isEmpty() &&
                !updateEmail.getText().toString().trim().isEmpty() &&
                !updateMotDePasse.getText().toString().trim().isEmpty();
    }

    public void saveData() {
        if (uri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        storageReference = FirebaseStorage.getInstance().getReference().child("etudiant").child(uri.getLastPathSegment());

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateEtudiantActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri urlImage = task.getResult();
                            if (urlImage != null) {
                                imageUrl = urlImage.toString();
                                updateData();
                                dialog.dismiss();
                            }
                        } else {
                            dialog.dismiss();
                            Toast.makeText(UpdateEtudiantActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(UpdateEtudiantActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateData() {
        nom = updateNom.getText().toString().trim();
        postNom = updatePostNom.getText().toString().trim();
        prenom = updatePrenom.getText().toString().trim();
        email = updateEmail.getText().toString().trim();
        motDePasse = updateMotDePasse.getText().toString().trim();

        EtudiantModel etudiant = new EtudiantModel(nom, postNom, prenom, motDePasse, imageUrl, email);

        databaseReference.setValue(etudiant).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageURL);
                    reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UpdateEtudiantActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UpdateEtudiantActivity.this, ListesEtudiantActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UpdateEtudiantActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(UpdateEtudiantActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateEtudiantActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
