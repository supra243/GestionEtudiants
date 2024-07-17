package com.example.gestionetudiant.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.gestionetudiant.R;
import com.example.gestionetudiant.models.EtudiantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EtudiantLoginActivity extends AppCompatActivity {

    private EditText loginEmailEtudiant, loginPasswordEtudiant;
    private Button loginButton;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiant_login);

        // Initialiser Firebase Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("etudiant");

        // Trouver les éléments de l'interface utilisateur
        loginEmailEtudiant = findViewById(R.id.LoginEmail);
        loginPasswordEtudiant = findViewById(R.id.LoginMotDePasse);
        loginButton = findViewById(R.id.LoginButton);
        progressBar = findViewById(R.id.progressBar);

        // Écouteur d'événement pour le bouton de connexion
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmailEtudiant.getText().toString().trim();
                String password = loginPasswordEtudiant.getText().toString().trim();

                // Vérifier si les champs sont vides
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(EtudiantLoginActivity.this, "Veuillez saisir votre email et votre mot de passe", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Afficher le ProgressBar
                progressBar.setVisibility(View.VISIBLE);

                // Vérifier si l'étudiant existe dans la base de données
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Masquer le ProgressBar
                        progressBar.setVisibility(View.GONE);

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                                EtudiantModel etudiant = studentSnapshot.getValue(EtudiantModel.class);
                                if (etudiant != null && etudiant.getEmail().equals(email) && etudiant.getMotDePasse().equals(password)) {
                                    // L'étudiant est authentifié
                                    Toast.makeText(EtudiantLoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                                    // Récupérer l'identifiant de l'étudiant
                                    String etudiantId = studentSnapshot.getKey();

                                    // Rediriger vers l'activité principale de l'étudiant
                                    Intent intent = new Intent(EtudiantLoginActivity.this, EtudiantHomeActivity.class);
                                    intent.putExtra("etudiantId", etudiantId); // Passer l'identifiant de l'étudiant à l'activité suivante
                                    startActivity(intent);
                                    finish(); // Fermer l'activité de connexion
                                    return; // Arrêter la boucle
                                }
                            }
                            // Aucun étudiant trouvé avec cet email et mot de passe
                            Toast.makeText(EtudiantLoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                        } else {
                            // La base de données est vide
                            Toast.makeText(EtudiantLoginActivity.this, "Aucun étudiant trouvé", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Masquer le ProgressBar en cas d'erreur
                        progressBar.setVisibility(View.GONE);
                        Log.e("Firebase", "Error checking user: ", databaseError.toException());
                    }
                });
            }
        });
    }
}
