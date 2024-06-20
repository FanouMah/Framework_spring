package models;

import Annotations.FormParam;

public class Employe {
    @FormParam("id")
    private String id;
    
    @FormParam("nom")
    private String nom;
    
    @FormParam("prenom")
    private String prenom;

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
}
