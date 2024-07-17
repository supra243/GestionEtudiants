package com.example.gestionetudiant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionetudiant.MyAdaptater;
import com.example.gestionetudiant.R;
import com.example.gestionetudiant.models.EtudiantModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListesEtudiantActivity extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView recyclerView;
    List<EtudiantModel> datalist;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    SearchView searchView;
    MyAdaptater adaptater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listes_etudiant);

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(ListesEtudiantActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(ListesEtudiantActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        datalist = new ArrayList<>();

        adaptater = new MyAdaptater(ListesEtudiantActivity.this, datalist);
        recyclerView.setAdapter(adaptater);

        databaseReference = FirebaseDatabase.getInstance().getReference("etudiant");

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                datalist.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    EtudiantModel etudiant = itemSnapshot.getValue(EtudiantModel.class);
                    etudiant.setIdEtudiant(itemSnapshot.getKey());
                    datalist.add(etudiant);
                }
                adaptater.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListesEtudiantActivity.this, AjouterEtudiantActivity.class);
                startActivity(intent);
            }
        });
    }
    public void searchList(String text){
        ArrayList<EtudiantModel> searchList = new ArrayList<>();
        for (EtudiantModel etudiant: datalist){
            if(etudiant.getNom().toLowerCase().contains(text.toLowerCase()) ||
                    etudiant.getPrenom().toLowerCase().contains(text.toLowerCase()) ||
                    etudiant.getPostNom().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(etudiant);
            }
        }
        adaptater.searchDataList(searchList);
    }

}
