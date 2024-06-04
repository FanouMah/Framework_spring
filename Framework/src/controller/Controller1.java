package controller;

import Annotations.*;
import mg.prom16.ModelView;

@Controller
public class Controller1 {

    @Get(value = "/hello")
    public String method1() {
        return "hello world";
    }

    @Get(value = "/pageNotFound")
    public ModelView method2() { 
        ModelView modelView = new ModelView();
        modelView.setUrl("/views/ErrorPage.jsp");
        modelView.addObject("message", "Page Not Found");
        modelView.addObject("code", 404);
        return modelView;
    }

    @Get(value = "/date")
    public java.util.Date method3() {
        return new java.util.Date();
    }
}