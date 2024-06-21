package controller;

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
}