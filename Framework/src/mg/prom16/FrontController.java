package mg.prom16;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import Annotations.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.Gson;
// import com.thoughtworks.paranamer.AdaptiveParanamer;
// import com.thoughtworks.paranamer.Paranamer;

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
                if (clazz.isAnnotationPresent(Controller.class)) {
                    list_controller.add(clazz);
                }
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(Url.class)) {
                        String url = method.getAnnotation(Url.class).value();
    
                        // Vérifier la présence des annotations GET et POST
                        boolean isGetPresent = method.isAnnotationPresent(Get.class);
                        boolean isPostPresent = method.isAnnotationPresent(Post.class);
    
                        // Si ni GET ni POST ne sont présents, on associe par défaut à GET
                        if (!isGetPresent && !isPostPresent) {
                            isGetPresent = true; // Par défaut GET
                        }
    
                        // Gérer les verbes HTTP GET et POST
                        if (isGetPresent) {
                            addMapping(url, clazz, method, "GET");
                        }
                        if (isPostPresent) {
                            addMapping(url, clazz, method, "POST");
                        }
                    }
                }
            }
        }
    }
    
    // Méthode pour ajouter un mapping URL -> Méthode avec gestion des verbes HTTP
    private void addMapping(String url, Class<?> clazz, Method method, String verb) throws ServletException {
        if (urlMappings.containsKey(url)) {
            Mapping mapping = urlMappings.get(url);
            try {
                mapping.addVerb(new Verb(clazz.getName(), method, verb));
            } catch (Exception e) {
                throw new ServletException(
                    e + " La methode '" + mapping.getByAction(verb).getMethod().getName() +
                    "' avec URL '" + url + "' est deja utilisee pour la methode " +
                    mapping.getByAction(verb).getVerbAction() + ". Conflit avec la methode '" +
                    method.getName() + "' avec URL '" + url + "' pour le verbe '" + verb + "'.");
            }
        } else {
            Mapping map = new Mapping();
            try {
                map.addVerb(new Verb(clazz.getName(), method, verb));
            } catch (Exception e) {
                throw new ServletException(e);
            }
            urlMappings.put(url, map);
        }
    }
    

    protected Object invoke_Method(HttpServletRequest request, Mapping mapping) throws IOException, NoSuchMethodException {
        Object returnValue = null;
        try {
            Verb verb = mapping.getByAction(request.getMethod());
            Class<?> clazz = Class.forName(verb.getClassName());
            Method method = verb.getMethod();
            method.setAccessible(true);
    
            Parameter[] methodParams = method.getParameters();
            Object[] args = new Object[methodParams.length];
    
            Enumeration<String> params = request.getParameterNames();
            Map<String, String> paramMap = new HashMap<>();

            // Paranamer paranamer = new AdaptiveParanamer();
            // String[] parameterMethodNames = paranamer.lookupParameterNames(method);
    
            while (params.hasMoreElements()) {
                String paramName = params.nextElement();
                paramMap.put(paramName, request.getParameter(paramName));
            }
    
            for (int i = 0; i < methodParams.length; i++) {
                if (methodParams[i].getType().equals(MySession.class)) {
                    HttpSession session = request.getSession();
                    MySession mySession = new MySession(session);
                    args[i] = mySession;
                } else if (methodParams[i].isAnnotationPresent(RequestBody.class)) {
                    Class<?> paramType = methodParams[i].getType();
                    Object paramObject = paramType.getDeclaredConstructor().newInstance();
                    for (Field field : paramType.getDeclaredFields()) {
                        String paramName = field.isAnnotationPresent(FormParam.class) ? field.getAnnotation(FormParam.class).value() : field.getName();
                        if (paramMap.containsKey(paramName)) {
                            field.setAccessible(true);
                            field.set(paramObject, paramMap.get(paramName));
                        }
                    }
                    args[i] = paramObject;
                } else if (methodParams[i].isAnnotationPresent(Param.class)) {
                    String paramName = methodParams[i].getAnnotation(Param.class).name();
                    String paramValue = paramMap.get(paramName);
                    args[i] = paramValue;
                } else {

                    // if (paramMap.containsKey(parameterMethodNames[i])) {
                    //     args[i] = paramMap.get(parameterMethodNames[i]);
                    // } else {
                    //     args[i] = null;
                    // }

                    if (paramMap.containsKey(methodParams[i].getName())) {
                        args[i] = paramMap.get(methodParams[i].getName());
                    } else {
                        args[i] = null;
                    }
                }
            }
    
            Object instance = clazz.getDeclaredConstructor().newInstance();
            returnValue = method.invoke(instance, args);
            
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
                throw new ServletException("Le package \""+package_name+"\" n'est pas reconnu.");
            } else {
                getControllerList(pack);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = request.getRequestURI().substring(request.getContextPath().length());
        
        Mapping mapping = urlMappings.get(url);

        if (mapping != null) {
            if (mapping.getByAction(request.getMethod()) != null) {
                Verb verb = mapping.getByAction(request.getMethod());
                // out.println("<p><strong>URL :</strong> " + url +"</p>");
                // out.println("<p><strong>Assosier a :</strong> " + mapping+"</p>");
                //out.println("<p>Contenue de la methode <strong>"+mapping.getMethodName()+"</strong> : "+invoke_Method(mapping.getClassName(), mapping.getMethodName())+"</p>");
                
                try {
                    Object returnValue = invoke_Method(request, mapping);
                    Gson gson = new Gson();
                    if (verb.getMethod().isAnnotationPresent(Restapi.class)) {
                        response.setContentType("application/json");
                        try (PrintWriter out = response.getWriter()) {
                            if (returnValue instanceof ModelView) {
                                ModelView modelView = (ModelView) returnValue;
                                HashMap<String, Object> data = modelView.getData();
                        
                                String jsonData = gson.toJson(data);
                        
                                out.print(jsonData);
                            } else {
                                String jsonData = gson.toJson(returnValue);
                                out.print(jsonData);
                            }
                        }                    
                    }
                    else {
                        response.setContentType("text/html;charset=UTF-8");
                        if (returnValue instanceof String) {
                            // if (((String) returnValue).startsWith("redirect")) {
                            //     String redirectUrl = ((String) returnValue).split(":")[1];
                                
                            //     RequestDispatcher dispatcher = request.getRequestDispatcher(redirectUrl);
                            //     dispatcher.forward(request, response);
                            // }
                            // else{
                                try (PrintWriter out = response.getWriter()) {
                                    out.println("<p>Contenue de la methode <strong>"+verb.method_to_string()+"</strong> : "+(String) returnValue+"</p>");
                                }
                            // }
                        } else if (returnValue instanceof ModelView) {
                            ModelView modelView = (ModelView) returnValue;
                            String viewUrl = modelView.getUrl();
                            HashMap<String, Object> data = modelView.getData();
            
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                request.setAttribute(entry.getKey(), entry.getValue());
                            }
            
                            RequestDispatcher dispatcher = request.getRequestDispatcher(viewUrl);
                            dispatcher.forward(request, response);
                            
                        } else if (returnValue == null) {
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"La methode \""+verb.method_to_string()+"\" retourne une valeur NULL");
                        } else {
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Le type de retour de l'objet \""+returnValue.getClass().getName()+"\" n'est pas pris en charge par le Framework");
                        }
                    }
        
                } catch (NoSuchMethodException | IOException e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Erreur lors de l'invocation de la methode \""+verb.method_to_string()+"\"");
                }
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"Auccune methode HTTP " + request.getMethod() + " pour l'URL " + url);
            }
            
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,"Pas de methode associee a l'URL: \"" + url + "\" pour la methode HTTP: " + request.getMethod());
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
