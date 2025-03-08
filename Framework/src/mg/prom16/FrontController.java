package mg.prom16;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.*;
import Annotations.*;
import Annotations.security.Authenticated;
import Annotations.security.Public;
import Annotations.validation.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.text.DateFormat;  
import java.text.SimpleDateFormat;  

import com.google.gson.Gson;
// import com.thoughtworks.paranamer.AdaptiveParanamer;
// import com.thoughtworks.paranamer.Paranamer;

@MultipartConfig
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
    
    private void validateField(Field field, String fieldValue, String paramName, Map<String, String> errors) {
        if (field.isAnnotationPresent(Required.class) && (fieldValue == null || fieldValue.isEmpty())) {
            errors.put(paramName, field.getAnnotation(Required.class).message());
        } else if (field.isAnnotationPresent(TypeNumber.class) && fieldValue != null) {
            try {
                if (!fieldValue.equals("")) {
                    Double.valueOf(fieldValue.toString());
                }
            } catch (NumberFormatException e) {
                errors.put(paramName, field.getAnnotation(TypeNumber.class).message());
            }
        } else if (field.isAnnotationPresent(TypeDate.class) && fieldValue != null ) {
            DateFormat dateFormat = new SimpleDateFormat(field.getAnnotation(TypeDate.class).pattern());
            dateFormat.setLenient(false);
            try {
                if (!fieldValue.equals("")) {
                    dateFormat.parse(fieldValue);
                }
            } catch (Exception e) {
                errors.put(paramName, field.getAnnotation(TypeDate.class).message());
            }
        }
        
    }


    protected Object invoke_Method(HttpServletRequest request, HttpServletResponse response, HttpSession session, Mapping mapping) throws IOException, NoSuchMethodException, ServletException {
        Object returnValue = null;
        Map<String, String> validationErrors = new HashMap<>();
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
                    MySession mySession = new MySession(session);
                    args[i] = mySession;
                } else if (methodParams[i].isAnnotationPresent(RequestBody.class)) {
                    Class<?> paramType = methodParams[i].getType();
                    Object paramObject = paramType.getDeclaredConstructor().newInstance();
                    for (Field field : paramType.getDeclaredFields()) {
                        String paramName = field.isAnnotationPresent(FormParam.class) ? field.getAnnotation(FormParam.class).value() : field.getName();
                        // if (paramMap.containsKey(paramName)) {
                            field.setAccessible(true);
                            // String fieldValue = paramMap.get(paramName);
                            String fieldValue = request.getParameter(paramName);;
                            field.set(paramObject, fieldValue);

                            validateField(field, fieldValue, paramName, validationErrors);
                        // }
                    }
                    args[i] = paramObject;
                } else if (methodParams[i].isAnnotationPresent(Param.class)) {
                    String paramName = methodParams[i].getAnnotation(Param.class).name();
                    String paramValue = paramMap.get(paramName);
                    args[i] = paramValue;
                } else if (methodParams[i].isAnnotationPresent(FileParam.class)) {
                    String partName = methodParams[i].getAnnotation(FileParam.class).value();
                    Part part = request.getPart(partName);
            
                    String fileName = part.getSubmittedFileName();
                    String uploadDir = getServletContext().getRealPath("") + File.separator + "uploads";
                    File uploadDirFile = new File(uploadDir);
                    
                    if (!uploadDirFile.exists()) {
                        uploadDirFile.mkdirs();
                    }

                    File file = new File(uploadDir, fileName);
                    try (InputStream fileContent = part.getInputStream()) {
                        Files.copy(fileContent, file.toPath());
                    }

                    String relativeFilePath = "uploads/" + fileName;
                    args[i] = relativeFilePath;
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

            if (!validationErrors.isEmpty()) {
                if (method.isAnnotationPresent(ErrorPath.class)) {
                    String refererUrl = method.getAnnotation(ErrorPath.class).value();;
                    if (refererUrl != null) {
                        ModelView modelView = new ModelView();
                        modelView.setUrl(refererUrl); 
                        modelView.setValidationErrors(validationErrors);
                        return modelView;
                    } else {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Aucune URL de référence disponible.");
                    }
                } else {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        Gson gson = new Gson();
                        String jsonErrors = gson.toJson(validationErrors);
                        out.print(jsonErrors);
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

    protected void readFile( HttpServletResponse response, String url) throws FileNotFoundException, IOException {
        if (url.startsWith("/uploads/")) {
            String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
            String filePath = uploadPath + File.separator + url.substring("/uploads/".length());
    
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                response.setContentType(getServletContext().getMimeType(file.getName()));
                response.setContentLength((int) file.length());
    
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                return;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le fichier demandé est introuvable : " + url);
                return;
            }
        }
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI().substring(request.getContextPath().length());

        readFile(response, url);

        Mapping mapping = urlMappings.get(url);

        if (mapping == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucune méthode associée à l'URL: \"" + url + "\" pour la méthode HTTP: " + request.getMethod());
            return;
        }

        Verb verb = mapping.getByAction(request.getMethod());
        if (verb == null) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Aucune méthode HTTP " + request.getMethod() + " pour l'URL " + url);
            return;
        }

        HttpSession session = request.getSession();
        Method method = verb.getMethod();
        Class<?> controllerClass;
        try {
            controllerClass = Class.forName(verb.getClassName());
        } catch (ClassNotFoundException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossible de charger la classe: " + verb.getClassName());
            return;
        }

        boolean isClassPublic = controllerClass.isAnnotationPresent(Public.class);
        boolean isClassAuthenticated = controllerClass.isAnnotationPresent(Authenticated.class);
        boolean isMethodPublic = method.isAnnotationPresent(Public.class);
        boolean isMethodAuthenticated = method.isAnnotationPresent(Authenticated.class);

        if (isMethodPublic) {}
        else if (isMethodAuthenticated) {
            if (session == null || session.getAttribute("auth") == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise pour accéder à l'URL: \"" + url + "\"");
                return;
            }

            String roleRequis = method.getAnnotation(Authenticated.class).value();
            String roleSession = (String) session.getAttribute("role");

            if (!roleRequis.equals("") && !roleRequis.equals(roleSession)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès refusé : privilèges insuffisants pour accéder à cette ressource.");
                return;
            }
        } else if (isClassAuthenticated) {
            if (session == null || session.getAttribute("auth") == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise pour accéder à l'URL: \"" + url + "\"");
                return;
            }
    
            String requiredRole = controllerClass.getAnnotation(Authenticated.class).value();
            String sessionRole = (String) session.getAttribute("role");
    
            if (!requiredRole.isEmpty() && !requiredRole.equals(sessionRole)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès refusé : privilèges insuffisants pour accéder à cette ressource.");
                return;
            }
        } else if (isClassPublic) { } 
        else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès interdit à l'URL: \"" + url + "\"");
            return;
        }

        try {
            Object returnValue = invoke_Method(request, response, session, mapping);
            Gson gson = new Gson();

            if (verb.getMethod().isAnnotationPresent(Restapi.class)) {
                response.setContentType("application/json");
                try (PrintWriter out = response.getWriter()) {
                    if (returnValue instanceof ModelView) {
                        out.print(gson.toJson(((ModelView) returnValue).getData()));
                    } else {
                        out.print(gson.toJson(returnValue));
                    }
                }
            } else {
                response.setContentType("text/html;charset=UTF-8");

                if (returnValue instanceof String) {
                    if (((String) returnValue).startsWith("redirect")) {
                        String redirectUrl = ((String) returnValue).split(":")[1];

                        RequestDispatcher dispatcher = request.getRequestDispatcher(redirectUrl);
                        dispatcher.forward(request, response);
                    } else {
                        try (PrintWriter out = response.getWriter()) {
                            out.println("<p>Contenu de la méthode <strong>" + verb.method_to_string() + "</strong> : " + returnValue + "</p>");
                        }
                    }
                } else if (returnValue instanceof ModelView) {
                    ModelView modelView = (ModelView) returnValue;
                    request.setAttribute("validationErrors", modelView.getValidationErrors());

                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                    dispatcher.forward(request, response);
                } else if (returnValue == null) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "La méthode \"" + verb.method_to_string() + "\" a retourné une valeur NULL.");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Type de retour non supporté : " + returnValue.getClass().getName());
                }
            }
        } catch (NoSuchMethodException | IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de l'invocation de la méthode \"" + verb.method_to_string() + "\" : " + e.getMessage());
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
