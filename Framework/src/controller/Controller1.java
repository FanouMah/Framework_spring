package controller;

import java.util.Vector;

import Annotations.*;
import mg.prom16.ModelView;
import models.Employe;

@Controller
public class Controller1 {

    @Get(value = "/message")
    public String get_message(String message) {
        return message;
    }

    @Get(value = "/pageNotFound")
    public ModelView pageNotFound() { 
        ModelView modelView = new ModelView();
        modelView.setUrl("/views/ErrorPage.jsp");
        modelView.addObject("message", "Page Not Found");
        modelView.addObject("code", 404);
        return modelView;
    }

    @Get(value = "/date")
    public java.util.Date get_Date() {
        return new java.util.Date();
    }

    // @Get(value = "/employe")
    // public ModelView get_employe(@Param(name = "id")String id, @Param(name = "nom")String nom, @Param(name = "prenom")String prenom){
    //     ModelView mv = new ModelView();
    //     mv.setUrl("/views/Employe.jsp");
    //     mv.addObject("id", id);
    //     mv.addObject("nom", nom);
    //     mv.addObject("prenom", prenom);
    //     return mv;
    // }

    @Get(value = "/employe")
    public ModelView get_employe(@RequestBody Employe employe) {
        ModelView mv = new ModelView();
        mv.setUrl("/views/Employe.jsp");
        mv.addObject("employe", employe);
        return mv;
    }

    @Restapi
    @Get(value = "/listemploye")
    public ModelView list_employe() {
        ModelView mv = new ModelView();
        // mv.setUrl("/views/ListEmploye.jsp");

        Employe employe1 = new Employe();
        employe1.setId("1");
        employe1.setNom("RABIARIMAHATRA");
        employe1.setPrenom("Lucas");

        Employe employe2 = new Employe();
        employe2.setId("2");
        employe2.setNom("MAHAFALIARIMBOLA");
        employe2.setPrenom("Fanomezantsoa");

        Employe employe3 = new Employe();
        employe3.setId("1");
        employe3.setNom("HAJARISON");
        employe3.setPrenom("Mickael");
        
        Vector<Employe> listEmploye = new Vector<>();
        listEmploye.add(employe1);
        listEmploye.add(employe2);
        listEmploye.add(employe3);

        mv.addObject("listemploye", listEmploye);
        return mv;
    }

    @Restapi
    @Get(value = "/fanou")
    public Employe get_Employe(){
        Employe employe2 = new Employe();
        employe2.setId("2");
        employe2.setNom("MAHAFALIARIMBOLA");
        employe2.setPrenom("Fanomezantsoa");

        return employe2;
    }
}