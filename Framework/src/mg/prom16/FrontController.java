package mg.prom16;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FrontController extends HttpServlet {

    protected List<String> list_controller = new ArrayList<>();

    public void getControllerList(String package_name) throws ClassNotFoundException {
        String bin_path = "WEB-INF/classes/" + package_name.replace(".", "/");

        bin_path = getServletContext().getRealPath(bin_path);

        File b = new File(bin_path);

        list_controller.clear();
        
        for (File onefile : b.listFiles()) {
            if (onefile.isFile() && onefile.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(package_name + "." + onefile.getName().split(".class")[0]);
                if (clazz.isAnnotationPresent(Annotations.Controller.class))
                list_controller.add(clazz.getName());
            }
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }

    protected void processRequest(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try {
            getControllerList(getServletContext().getInitParameter("controllerPackage"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (PrintWriter out = response.getWriter()) {
            //out.println(request.getRequestURL().toString());
            // or out.println(request.getRequestURI());

            out.println("<ul>");
            for (String controller : list_controller) {
                out.println("<li>"+controller+"</li>");
            }
            out.println("<ul>");
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
