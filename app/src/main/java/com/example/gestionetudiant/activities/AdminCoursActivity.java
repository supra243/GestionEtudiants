package com.example.gestionetudiant.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionetudiant.R;
import com.example.gestionetudiant.models.CoursModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminCoursActivity extends AppCompatActivity {

    Button adminAddCoursButton;
    TableLayout coursesTable;
    private DatabaseReference databaseReference;
    private DatabaseReference notesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cours);

        // Initialize Firebase Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Cours");
        notesReference = firebaseDatabase.getReference("Notes");

        adminAddCoursButton = findViewById(R.id.add_cours_button);
        coursesTable = findViewById(R.id.courses_table);

        adminAddCoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCourseDialog();
            }
        });

        // Load courses from Firebase when activity is created
        loadCoursesFromFirebase();
    }

    private void showAddCourseDialog() {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_cours, null);

        // Create the AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        final EditText editTextNomCours = dialogView.findViewById(R.id.editTextNomCours);
        final EditText editTextProfCours = dialogView.findViewById(R.id.editTextProfCours);

        dialogBuilder.setTitle("Ajouter un cours")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    // Handle the positive button click
                    String nomCours = editTextNomCours.getText().toString();
                    String profCours = editTextProfCours.getText().toString();
                    addCoursToFirebase(nomCours, profCours);
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                    // Handle the negative button click
                    dialog.dismiss();
                });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void addCoursToFirebase(String nomCours, String profCours) {
        showLoadingDialog();
        // Generate a unique ID for each course
        String idCours = databaseReference.push().getKey();

        // Create a new course object
        CoursModel.Cours cours = new CoursModel.Cours(idCours, nomCours, profCours);

        // Save the course in Firebase under the unique ID
        if (idCours != null) {
            databaseReference.child(idCours).setValue(cours)
                    .addOnCompleteListener(task -> {
                        hideLoadingDialog();
                        if (task.isSuccessful()) {
                            // Course added successfully
                            Toast.makeText(AdminCoursActivity.this, "Course added successfully", Toast.LENGTH_SHORT).show();
                            loadCoursesFromFirebase();
                            // You can show a success message or update UI accordingly
                        } else {
                            // Failed to add course
                            // You can show an error message or handle failure
                        }
                    });
        }
    }

    private void loadCoursesFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear existing rows except for the header row
                int childCount = coursesTable.getChildCount();
                if (childCount > 1) {
                    coursesTable.removeViews(1, childCount - 1);
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CoursModel.Cours cours = snapshot.getValue(CoursModel.Cours.class);
                    if (cours != null) {
                        // Add a new row to the table for each course
                        TableRow row = new TableRow(AdminCoursActivity.this);

                        TextView nomCoursText = new TextView(AdminCoursActivity.this);
                        nomCoursText.setText(cours.getNomCours());
                        nomCoursText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        nomCoursText.setPadding(8, 8, 8, 8);

                        TextView profCoursText = new TextView(AdminCoursActivity.this);
                        profCoursText.setText(cours.getProfCours());
                        profCoursText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        profCoursText.setPadding(8, 8, 8, 8);

                        Button updateButton = new Button(AdminCoursActivity.this);
                        updateButton.setText("Update");
                        updateButton.setTextColor(Color.WHITE);
                        updateButton.setPadding(8, 8, 8, 8);
                        updateButton.setOnClickListener(v -> {
                            showUpdateCourseDialog(cours);
                        });

                        Button deleteButton = new Button(AdminCoursActivity.this);
                        deleteButton.setText("Delete");
                        deleteButton.setBackgroundColor(Color.RED);
                        deleteButton.setTextColor(Color.WHITE);
                        deleteButton.setPadding(8, 8, 8, 8);
                        deleteButton.setOnClickListener(v -> {
                            deleteCourseFromFirebase(cours.getIdCours());
                        });

                        // Set an OnClickListener to open AdminNoteActivity when a course is clicked
                        row.setOnClickListener(view -> {
                            Intent intent = new Intent(AdminCoursActivity.this, AdminNoteActivity.class);
                            intent.putExtra("coursId", cours.getIdCours()); // Pass the course ID to AdminNoteActivity
                            startActivity(intent);
                        });

                        // Add views to the row
                        row.addView(nomCoursText);
                        row.addView(profCoursText);
                        row.addView(updateButton);
                        row.addView(deleteButton);

                        // Add row to the table
                        coursesTable.addView(row);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void showUpdateCourseDialog(CoursModel.Cours cours) {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_cours, null);

        // Create the AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        final EditText editTextNomCours = dialogView.findViewById(R.id.editTextNomCours);
        final EditText editTextProfCours = dialogView.findViewById(R.id.editTextProfCours);

        editTextNomCours.setText(cours.getNomCours());
        editTextProfCours.setText(cours.getProfCours());

        dialogBuilder.setTitle("Modifier un cours")
                .setPositiveButton("Mettre à jour", (dialog, which) -> {
                    // Handle the positive button click
                    String nomCours = editTextNomCours.getText().toString();
                    String profCours = editTextProfCours.getText().toString();
                    updateCourseInFirebase(cours.getIdCours(), nomCours, profCours);
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                    // Handle the negative button click
                    dialog.dismiss();
                });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateCourseInFirebase(String idCours, String nomCours, String profCours) {
        DatabaseReference courseRef = databaseReference.child(idCours);
        courseRef.child("nomCours").setValue(nomCours);
        courseRef.child("profCours").setValue(profCours)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminCoursActivity.this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                        loadCoursesFromFirebase();
                    } else {
                        // Handle the failure
                    }
                });
    }

    private void deleteCourseFromFirebase(String idCours) {
        // Remove the course
        databaseReference.child(idCours).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove all notes associated with the course
                        notesReference.orderByChild("idCours").equalTo(idCours)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                                            noteSnapshot.getRef().removeValue();
                                        }
                                        Toast.makeText(AdminCoursActivity.this, "Course and associated notes deleted successfully", Toast.LENGTH_SHORT).show();
                                        loadCoursesFromFirebase();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // Handle possible errors
                                    }
                                });
                    } else {
                        // Handle the failure
                    }
                });
    }

    private AlertDialog loadingDialog;

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
