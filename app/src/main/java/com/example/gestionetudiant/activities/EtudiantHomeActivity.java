package com.example.gestionetudiant.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionetudiant.R;
import com.example.gestionetudiant.models.CoursModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EtudiantHomeActivity extends AppCompatActivity {

    private TableLayout notesTable;
    private DatabaseReference databaseReferenceNotes;
    private DatabaseReference databaseReferenceCours; // Référence pour les cours
    private String etudiantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiant_home); // Créez un layout XML "activity_etudiant.xml"

        // Initialize Firebase Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceNotes = firebaseDatabase.getReference("Notes");
        databaseReferenceCours = firebaseDatabase.getReference("Cours"); // Initialiser la référence pour les cours

        notesTable = findViewById(R.id.notes_table);

        // Récupérer l'ID de l'étudiant de l'Intent après la connexion
        etudiantId = getIntent().getStringExtra("etudiantId");

        // Charger les notes de l'étudiant
        loadNotesForEtudiant();
    }

    // Méthode pour charger les notes de l'étudiant
    private void loadNotesForEtudiant() {
        // Afficher le dialogue de chargement
        showLoadingDialog();

        databaseReferenceNotes.orderByChild("idEtudiant").equalTo(etudiantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Effacer les anciennes notes dans le tableau
                int childCount = notesTable.getChildCount();
                if (childCount > 1) {
                    notesTable.removeViews(1, childCount - 1);
                }

                // Map pour stocker les notes de l'étudiant
                Map<String, String> notesMap = new HashMap<>();

                // Parcourir les notes de l'étudiant
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String idCours = String.valueOf(snapshot.child("idCours").getValue());
                    String note = String.valueOf(snapshot.child("valeurNote").getValue());
                    notesMap.put(idCours, note);
                }

                // Récupérer les détails des cours
                for (Map.Entry<String, String> entry : notesMap.entrySet()) {
                    String idCours = entry.getKey();
                    String note = entry.getValue();

                    databaseReferenceCours.child(idCours).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                CoursModel.Cours cours = dataSnapshot.getValue(CoursModel.Cours.class);
                                if (cours != null) {
                                    // Créer une nouvelle ligne pour la note
                                    TableRow row = new TableRow(EtudiantHomeActivity.this);

                                    // Créer un TextView pour le nom du cours
                                    TextView nomCoursText = new TextView(EtudiantHomeActivity.this);
                                    nomCoursText.setText(cours.getNomCours());
                                    nomCoursText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                                    nomCoursText.setPadding(8, 8, 8, 8);

                                    // Créer un TextView pour la note
                                    TextView noteText = new TextView(EtudiantHomeActivity.this);
                                    noteText.setText(note);
                                    noteText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                                    noteText.setPadding(8, 8, 8, 8);

                                    // Ajouter les TextView à la ligne du tableau
                                    row.addView(nomCoursText);
                                    row.addView(noteText);

                                    // Ajouter la ligne au TableLayout
                                    notesTable.addView(row);
                                }
                            } else {
                                // Le cours n'est pas trouvé, afficher un message "Cours indisponible"
                                showCoursIndisponible(idCours);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Firebase", "Error retrieving cours details: ", databaseError.toException());
                        }
                    });
                }
                // Masquer le dialogue de chargement
                hideLoadingDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gérer les erreurs
                Log.e("Firebase", "Error loading notes: ", databaseError.toException());
                // Masquer le dialogue de chargement
                hideLoadingDialog();
            }
        });
    }

    // Méthode pour afficher un message "Cours indisponible"
    private void showCoursIndisponible(String idCours) {
        // Créer une nouvelle ligne pour le cours indisponible
        TableRow row = new TableRow(EtudiantHomeActivity.this);

        TextView nomCoursText = new TextView(EtudiantHomeActivity.this);
        nomCoursText.setText("Cours indisponible");
        nomCoursText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        nomCoursText.setPadding(8, 8, 8, 8);

        TextView noteText = new TextView(EtudiantHomeActivity.this);
        noteText.setText("N/A");
        noteText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        noteText.setPadding(8, 8, 8, 8);

        row.addView(nomCoursText);
        row.addView(noteText);
        notesTable.addView(row);
    }

    // Affichez le dialogue de chargement
    // (Implémentez les méthodes showLoadingDialog et hideLoadingDialog comme dans AdminNoteActivity)
    private AlertDialog loadingDialog;

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
