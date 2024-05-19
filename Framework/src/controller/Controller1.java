package controller;

import Annotations.*;

@Controller
public class Controller1 {

    @Get(value = "/Controller1/method1")
    public void method1() { }

    @Get
    public void method2() { }

    public void method3() { }
}