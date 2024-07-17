package com.example.gestionetudiant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.gestionetudiant.R;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EtudiantProfileActivity extends AppCompatActivity {

    AppCompatButton detailNom, detailPrenom, detailPostNom, detailMotDePasse;
    TextView detailEmail;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;
    String idEtudiant = "";
    String imageUrl = "";

    private DatabaseReference etudiantsReference;
    private DatabaseReference notesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiant_profile);

        detailEmail = findViewById(R.id.detailEmail);
        detailNom = findViewById(R.id.detailNom);
        detailPrenom = findViewById(R.id.detailPrenom);
        detailPostNom = findViewById(R.id.detailPostNom);
        detailMotDePasse = findViewById(R.id.detailMotDePasse);
        detailImage = findViewById(R.id.detailImage);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);

        etudiantsReference = FirebaseDatabase.getInstance().getReference("etudiant");
        notesReference = FirebaseDatabase.getInstance().getReference("Notes");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailNom.setText(bundle.getString("nom"));
            detailPrenom.setText(bundle.getString("prenom"));
            detailPostNom.setText(bundle.getString("postNom"));
            detailEmail.setText(bundle.getString("email"));
            detailMotDePasse.setText(bundle.getString("motDePasse"));
            idEtudiant = bundle.getString("idEtudiant");
            imageUrl = bundle.getString("Image");

            Glide.with(this)
                    .load(imageUrl)
                    .transform(new CircleCrop())
                    .into(detailImage);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStudentAndNotes();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EtudiantProfileActivity.this, UpdateEtudiantActivity.class);
                intent.putExtra("Nom", detailNom.getText().toString());
                intent.putExtra("PostNom", detailPostNom.getText().toString());
                intent.putExtra("Prenom", detailPrenom.getText().toString());
                intent.putExtra("Email", detailEmail.getText().toString());
                intent.putExtra("MotDePasse", detailMotDePasse.getText().toString());
                intent.putExtra("Image", imageUrl);
                intent.putExtra("idEtudiant", idEtudiant);
                startActivity(intent);
            }
        });
    }

    private void deleteStudentAndNotes() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // First, delete all notes associated with this student
                notesReference.orderByChild("idEtudiant").equalTo(idEtudiant)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                                    noteSnapshot.getRef().removeValue();
                                }
                                // After deleting notes, delete the student
                                etudiantsReference.child(idEtudiant).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(EtudiantProfileActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(), ListesEtudiantActivity.class));
                                                finish();
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle possible errors
                            }
                        });
            }
        });
    }
}
