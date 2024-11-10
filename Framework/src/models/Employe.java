package models;

import Annotations.FormParam;
import Annotations.validation.Required;
import Annotations.validation.TypeDate;
import Annotations.validation.TypeNumber;

public class Employe {

    @Required
    @TypeNumber(message = "L'ID doit être un nombre.")
    @FormParam("id")
    private String id;
    
    @Required(message = "Le nom est requis.")
    @FormParam("nom")
    private String nom;

    @TypeDate(message = "La date d'embauche doit être au format 'yyyy-MM-dd'.")
    @FormParam("date-embauche")
    private String dateEmbauche;
    
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
