package com.example.gestionetudiant.models;

public class EtudiantModel {
    private String nom;
    private String postNom;
    private String prenom;
    private String motDePasse;
    private String imgUrl;
    private String email;

    public String getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(String idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    private String idEtudiant;

    public String getNom() {
        return nom;
    }

    public String getPostNom() {
        return postNom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getEmail() {
        return email;
    }

    public EtudiantModel(String nom, String postNom, String prenom, String motDePasse, String imgUrl, String email) {
        this.nom = nom;
        this.postNom = postNom;
        this.prenom = prenom;
        this.motDePasse = motDePasse;
        this.imgUrl = imgUrl;
        this.email = email;
    }

    public EtudiantModel() {
    }
}
