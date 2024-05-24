package mg.prom16;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import Annotations.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FrontController extends HttpServlet {

    protected List<Class<?>> list_controller = new ArrayList<>();
    protected Map<String, Mapping> urlMappings = new HashMap<>();

    protected void getControllerList(String package_name) throws ClassNotFoundException {
        String bin_path = "WEB-INF/classes/" + package_name.replace(".", "/");

        bin_path = getServletContext().getRealPath(bin_path);

        File b = new File(bin_path);

        list_controller.clear();
        
        for (File onefile : b.listFiles()) {
            if (onefile.isFile() && onefile.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(package_name + "." + onefile.getName().split(".class")[0]);
                if (clazz.isAnnotationPresent(Controller.class))
                
                list_controller.add(clazz);

                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(Get.class)) {
                        Mapping mapping = new Mapping(clazz.getName(), method.getName());
                        // String key = "/"+clazz.getSimpleName()+"/"+method.getName();   
                        String key = method.getAnnotation(Get.class).value();                     
                        urlMappings.put(key, mapping);
                    }
                }
            }
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            getControllerList(getServletContext().getInitParameter("controllerPackage"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String url = request.getRequestURI().substring(request.getContextPath().length());
        
        try (PrintWriter out = response.getWriter()) {

            Mapping mapping = urlMappings.get(url);

            if (mapping != null) {
                out.println("URL : " + url +"</br>");
                out.println("Assosier a : " + mapping);
            } else {
                out.println("Pas de methode Get associer a l'URL: " + url);
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
