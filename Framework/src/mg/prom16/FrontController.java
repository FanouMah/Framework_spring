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
                if (clazz.isAnnotationPresent(Controller.class))
                    list_controller.add(clazz);

                for (Method method : clazz.getMethods()) {
                    String url = null;
                    String verb = null;
    
                    if (method.isAnnotationPresent(Get.class)) {
                        url = method.getAnnotation(Get.class).value();
                        verb = "GET";
                    } else if (method.isAnnotationPresent(Post.class)) {
                        url = method.getAnnotation(Post.class).value();
                        verb = "POST";
                    } else {
                        continue;
                    }
    
                    if (urlMappings.containsKey(url) && !urlMappings.get(url).getVerb().equals(verb)) {
                        throw new ServletException("La méthode '" + urlMappings.get(url).getMethod().getName() + 
                            "' avec URL '" + url + "' est déjà utilisée pour la méthode " + 
                            urlMappings.get(url).getVerb() + ". Conflit avec la méthode '" + 
                            method.getName() + "' avec URL '" + url + "' pour le verbe '" + verb + "'.");
                    } else {
                        urlMappings.put(url, new Mapping(clazz.getName(), method, verb));
                    }
                }
            }
        }
    }

    protected Object invoke_Method(HttpServletRequest request, String className, Method method) throws IOException, NoSuchMethodException {
        Object returnValue = null;
        try {
            Class<?> clazz = Class.forName(className);
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
        String verb = request.getMethod();

        if (mapping != null && mapping.getVerb().toUpperCase().equals(verb.toUpperCase())) {
            // out.println("<p><strong>URL :</strong> " + url +"</p>");
            // out.println("<p><strong>Assosier a :</strong> " + mapping+"</p>");
            //out.println("<p>Contenue de la methode <strong>"+mapping.getMethodName()+"</strong> : "+invoke_Method(mapping.getClassName(), mapping.getMethodName())+"</p>");
            
            try {
                Object returnValue = invoke_Method(request, mapping.getClassName(), mapping.getMethod());
                Gson gson = new Gson();
                if (mapping.getMethod().isAnnotationPresent(Restapi.class)) {
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
                                out.println("<p>Contenue de la methode <strong>"+mapping.method_to_string()+"</strong> : "+(String) returnValue+"</p>");
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
                        throw new ServletException("La methode \""+mapping.method_to_string()+"\" retourne une valeur NULL");
                    } else {
                        throw new ServletException("Le type de retour de l'objet \""+returnValue.getClass().getName()+"\" n'est pas pris en charge par le Framework");
                    }
                }
    
            } catch (NoSuchMethodException | IOException e) {
                throw new ServletException("Erreur lors de l'invocation de la methode \""+mapping.method_to_string()+"\"", null);
            }
        } else {
            throw new ServletException("Pas de méthode associée à l'URL: \"" + url + "\" pour la méthode HTTP: " + verb);
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
