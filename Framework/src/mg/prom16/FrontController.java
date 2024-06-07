package mg.prom16;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import Annotations.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FrontController extends HttpServlet {

    protected List<Class<?>> list_controller = new ArrayList<>();
    protected Map<String, Mapping> urlMappings = new HashMap<>();

    protected void getControllerList(String package_name) throws ServletException, ClassNotFoundException {
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
                        if (urlMappings.containsKey(key)) {
                            throw new ServletException("La methode '"+urlMappings.get(key).getMethodName()+"' possede deja l'URL '"+key+"' comme annotation, donc elle ne peux pas etre assigner a la methode '"+mapping.getMethodName()+"'");
                        }                   
                        else{
                            urlMappings.put(key, mapping);
                        }
                    }
                }
            }
        }
    }

    protected Object invoke_Method(String className, String methodName) {
        Object returnValue = null;
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            returnValue = method.invoke(instance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            String package_name = "controllerPackage"; 
            String pack = getServletContext().getInitParameter(package_name);
            if (pack == null) {
                throw new ServletException("Le package '"+package_name+"' n'est pas reconnu.");
            } else {
                getControllerList(pack);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String url = request.getRequestURI().substring(request.getContextPath().length());
        
        Mapping mapping = urlMappings.get(url);

        if (mapping != null) {
            // out.println("<p><strong>URL :</strong> " + url +"</p>");
            // out.println("<p><strong>Assosier a :</strong> " + mapping+"</p>");
            //out.println("<p>Contenue de la methode <strong>"+mapping.getMethodName()+"</strong> : "+invoke_Method(mapping.getClassName(), mapping.getMethodName())+"</p>");

            Object returnValue = invoke_Method(mapping.getClassName(), mapping.getMethodName());

            if (returnValue instanceof String) {
                try (PrintWriter out = response.getWriter()) {
                    out.println("<p>Contenue de la methode <strong>"+mapping.getMethodName()+"</strong> : "+(String) returnValue+"</p>");
                }
            } else if (returnValue instanceof ModelView) {
                ModelView modelView = (ModelView) returnValue;
                String viewUrl = modelView.getUrl();
                HashMap<String, Object> data = modelView.getData();

                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }

                RequestDispatcher dispatcher = request.getRequestDispatcher(viewUrl);
                dispatcher.forward(request, response);
                
            } else {
                throw new ServletException("Le type de retour de l'objet'"+returnValue+"' n'est pas pris en charge par le Framework");
            }
        } else {
            throw new ServletException("Pas de methode Get associer a l'URL: '" + url +"'");
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
