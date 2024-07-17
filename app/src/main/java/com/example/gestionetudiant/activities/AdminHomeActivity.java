package com.example.gestionetudiant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gestionetudiant.R;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        ConstraintLayout etudiantAdminLayout = findViewById(R.id.etudiantAdminLayout);
        ConstraintLayout coursAdminLayout = findViewById(R.id.coursAdminLayout);

        etudiantAdminLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, ListesEtudiantActivity.class);
                startActivity(intent);
            }
        });

        coursAdminLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminCoursActivity.class);
                startActivity(intent);
            }
        });
    }
}
