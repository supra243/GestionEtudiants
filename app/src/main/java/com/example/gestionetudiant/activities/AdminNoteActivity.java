package com.example.gestionetudiant.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionetudiant.R;
import com.example.gestionetudiant.models.CoursModel;
import com.example.gestionetudiant.models.EtudiantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminNoteActivity extends AppCompatActivity {

    Button addNoteButton;
    TableLayout notesTable;
    private DatabaseReference databaseReferenceNotes;
    private DatabaseReference databaseReferenceEtudiants;
    private String coursId;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_note);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceNotes = firebaseDatabase.getReference("Notes");
        databaseReferenceEtudiants = firebaseDatabase.getReference("etudiant");

        addNoteButton = findViewById(R.id.add_note_button);
        notesTable = findViewById(R.id.notes_table);

        coursId = getIntent().getStringExtra("coursId");

        loadNotesFromFirebase();

        addNoteButton.setOnClickListener(v -> showAddNoteDialog());
    }

    private void showAddNoteDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        final Spinner spinnerEtudiantId = dialogView.findViewById(R.id.spinnerEtudiantId);
        final EditText editTextNote = dialogView.findViewById(R.id.editTextNote);

        loadEtudiantIdsIntoSpinner(spinnerEtudiantId);

        dialogBuilder.setTitle("Ajouter une Note")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String etudiantId = spinnerEtudiantId.getSelectedItem().toString();
                    String noteStr = editTextNote.getText().toString();

                    if (TextUtils.isEmpty(noteStr)) {
                        Toast.makeText(AdminNoteActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int note;
                    try {
                        note = Integer.parseInt(noteStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(AdminNoteActivity.this, "Note invalide", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addNoteToFirebase(etudiantId, note);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void loadEtudiantIdsIntoSpinner(Spinner spinner) {
        databaseReferenceEtudiants.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> etudiantIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String etudiantId = snapshot.getKey();
                    etudiantIds.add(etudiantId);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminNoteActivity.this, android.R.layout.simple_spinner_item, etudiantIds);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error loading student IDs: ", databaseError.toException());
            }
        });
    }

    private void addNoteToFirebase(String etudiantId, int note) {
        showLoadingDialog();

        String noteId = databaseReferenceNotes.push().getKey();

        CoursModel.Note newNote = new CoursModel.Note(noteId, etudiantId, coursId, note);

        if (noteId != null) {
            databaseReferenceNotes.child(noteId).setValue(newNote)
                    .addOnCompleteListener(task -> {
                        hideLoadingDialog();
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminNoteActivity.this, "Note ajoutée avec succès", Toast.LENGTH_SHORT).show();
                            loadNotesFromFirebase();
                        } else {
                            Toast.makeText(AdminNoteActivity.this, "Échec de l'ajout de la note", Toast.LENGTH_SHORT).show();
                            Exception e = task.getException();
                            if (e != null) {
                                Log.e("Firebase", "Error adding note", e);
                            }
                        }
                    });
        } else {
            hideLoadingDialog();
            Toast.makeText(AdminNoteActivity.this, "Erreur lors de la génération de l'ID de la note", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotesFromFirebase() {
        databaseReferenceNotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int childCount = notesTable.getChildCount();
                if (childCount > 1) {
                    notesTable.removeViews(1, childCount - 1);
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("idCours").getValue(String.class).equals(coursId)) {
                        CoursModel.Note note = snapshot.getValue(CoursModel.Note.class);

                        if (note != null) {
                            addNoteToTable(note);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error loading notes: ", databaseError.toException());
            }
        });
    }

    private void addNoteToTable(CoursModel.Note note) {
        TableRow row = new TableRow(AdminNoteActivity.this);

        databaseReferenceEtudiants.child(note.getIdEtudiant()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    EtudiantModel etudiant = dataSnapshot.getValue(EtudiantModel.class);
                    if (etudiant != null) {
                        TextView nomEtudiantText = new TextView(AdminNoteActivity.this);
                        nomEtudiantText.setText(etudiant.getNom() + " " + etudiant.getPrenom());
                        nomEtudiantText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        nomEtudiantText.setPadding(8, 8, 8, 8);

                        TextView noteText = new TextView(AdminNoteActivity.this);
                        noteText.setText(String.valueOf(note.getValeurNote()));
                        noteText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        noteText.setPadding(8, 8, 8, 8);

                        Button updateButton = new Button(AdminNoteActivity.this);
                        updateButton.setText("Modifier");
                        updateButton.setOnClickListener(v -> showUpdateNoteDialog(note));

                        Button deleteButton = new Button(AdminNoteActivity.this);
                        deleteButton.setText("Supprimer");
                        deleteButton.setOnClickListener(v -> deleteNoteFromFirebase(note.getIdNote()));

                        row.addView(nomEtudiantText);
                        row.addView(noteText);
                        row.addView(updateButton);
                        row.addView(deleteButton);

                        notesTable.addView(row);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving student data: ", databaseError.toException());
            }
        });
    }

    private void showUpdateNoteDialog(CoursModel.Note note) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        final Spinner spinnerEtudiantId = dialogView.findViewById(R.id.spinnerEtudiantId);
        final EditText editTextNote = dialogView.findViewById(R.id.editTextNote);

        loadEtudiantIdsIntoSpinner(spinnerEtudiantId);

        // Set the current student ID and disable the spinner
        spinnerEtudiantId.setSelection(((ArrayAdapter<String>) spinnerEtudiantId.getAdapter()).getPosition(note.getIdEtudiant()));
        spinnerEtudiantId.setEnabled(false);

        editTextNote.setText(String.valueOf(note.getValeurNote()));

        dialogBuilder.setTitle("Modifier la Note")
                .setPositiveButton("Mettre à jour", (dialog, which) -> {
                    String noteStr = editTextNote.getText().toString();

                    if (TextUtils.isEmpty(noteStr)) {
                        Toast.makeText(AdminNoteActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int updatedNote;
                    try {
                        updatedNote = Integer.parseInt(noteStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(AdminNoteActivity.this, "Note invalide", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateNoteInFirebase(note.getIdNote(), updatedNote);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateNoteInFirebase(String noteId, int newNoteValue) {
        showLoadingDialog();

        databaseReferenceNotes.child(noteId).child("valeurNote").setValue(newNoteValue)
                .addOnCompleteListener(task -> {
                    hideLoadingDialog();
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminNoteActivity.this, "Note mise à jour avec succès", Toast.LENGTH_SHORT).show();
                        loadNotesFromFirebase();
                    } else {
                        Toast.makeText(AdminNoteActivity.this, "Échec de la mise à jour de la note", Toast.LENGTH_SHORT).show();
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("Firebase", "Error updating note", e);
                        }
                    }
                });
    }

    private void deleteNoteFromFirebase(String noteId) {
        showLoadingDialog();

        databaseReferenceNotes.child(noteId).removeValue()
                .addOnCompleteListener(task -> {
                    hideLoadingDialog();
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminNoteActivity.this, "Note supprimée avec succès", Toast.LENGTH_SHORT).show();
                        loadNotesFromFirebase();
                    } else {
                        Toast.makeText(AdminNoteActivity.this, "Échec de la suppression de la note", Toast.LENGTH_SHORT).show();
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("Firebase", "Error deleting note", e);
                        }
                    }
                });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.progress_dialog, null);
            builder.setView(view);
            builder.setCancelable(false);
            loadingDialog = builder.create();
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
