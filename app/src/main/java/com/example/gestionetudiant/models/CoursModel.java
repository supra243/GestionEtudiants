package com.example.gestionetudiant.models;

public class CoursModel {
    public static class Cours {

        private String idCours;
        private String nomCours;
        private String profCours;
        // Constructeur sans argument (pour la deserialisation)
        public Cours() {
            // Vous pouvez initialiser les champs avec des valeurs par défaut ici si nécessaire
        }
        public Cours(String idCours, String nomCours, String profCours) {
            this.idCours = idCours;
            this.nomCours = nomCours;
            this.profCours = profCours;
        }

        public String getIdCours() {
            return idCours;
        }

        public void setIdCours(String idCours) {
            this.idCours = idCours;
        }

        public String getNomCours() {
            return nomCours;
        }

        public void setNomCours(String nomCours) {
            this.nomCours = nomCours;
        }

        public String getProfCours() {
            return profCours;
        }

        public void setProfCours(String profCours) {
            this.profCours = profCours;
        }

        @Override
        public String toString() {
            return "Cours{" +
                    "idCours=" + idCours +
                    ", nomCours='" + nomCours + '\'' +
                    ", profCours='" + profCours + '\'' +
                    '}';
        }
    }
    public static class Note {
        private String idNote;
        private String idEtudiant;
        private String idCours;
        private int valeurNote; // par exemple, la valeur de la note sur 20

        public Note() {
            // Constructeur vide requis pour Firebase
        }

        public Note(String idNote, String idEtudiant, String idCours, int valeurNote) {
            this.idNote = idNote;
            this.idEtudiant = idEtudiant;
            this.idCours = idCours;
            this.valeurNote = valeurNote;
        }

        // Getters and Setters
        public String getIdNote() {
            return idNote;
        }

        public void setIdNote(String idNote) {
            this.idNote = idNote;
        }

        public String getIdEtudiant() {
            return idEtudiant;
        }

        public void setIdEtudiant(String idEtudiant) {
            this.idEtudiant = idEtudiant;
        }

        public String getIdCours() {
            return idCours;
        }

        public void setIdCours(String idCours) {
            this.idCours = idCours;
        }

        public int getValeurNote() {
            return valeurNote;
        }

        public void setValeurNote(int valeurNote) {
            this.valeurNote = valeurNote;
        }
    }

}
