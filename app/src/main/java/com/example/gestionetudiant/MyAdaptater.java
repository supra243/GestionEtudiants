package com.example.gestionetudiant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gestionetudiant.activities.EtudiantProfileActivity;
import com.example.gestionetudiant.models.EtudiantModel;

import java.util.ArrayList;
import java.util.List;

public class MyAdaptater extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<EtudiantModel> dataList;
    public MyAdaptater(Context context, List<EtudiantModel> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(dataList.get(position).getImgUrl()).into(holder.recImage);
        holder.recNom.setText(dataList.get(position).getNom());
        holder.recPostNom.setText(dataList.get(position).getPostNom());
        holder.recPrenom.setText(dataList.get(position).getPrenom());
        holder.recEmail.setText(dataList.get(position).getEmail());

        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EtudiantProfileActivity.class);
                intent.putExtra("Image", dataList.get(holder.getAdapterPosition()).getImgUrl());
                intent.putExtra("nom", dataList.get(holder.getAdapterPosition()).getNom());
                intent.putExtra("postNom", dataList.get(holder.getAdapterPosition()).getPostNom());
                intent.putExtra("email", dataList.get(holder.getAdapterPosition()).getEmail());
                intent.putExtra("prenom", dataList.get(holder.getAdapterPosition()).getPrenom());
                intent.putExtra("motDePasse", dataList.get(holder.getAdapterPosition()).getMotDePasse());
                intent.putExtra("idEtudiant",dataList.get(holder.getAdapterPosition()).getIdEtudiant());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void searchDataList(ArrayList<EtudiantModel> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recNom, recPrenom, recPostNom, recEmail;
    CardView recCard;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recImage = itemView.findViewById(R.id.recImage);
        recNom = itemView.findViewById(R.id.recNom);
        recPrenom = itemView.findViewById(R.id.recPrenom);
        recPostNom = itemView.findViewById(R.id.recPostNom);
        recEmail = itemView.findViewById(R.id.recEmail);
        recCard = itemView.findViewById(R.id.recCard);
    }
}
