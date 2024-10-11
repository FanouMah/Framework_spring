package mg.prom16;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
// import com.thoughtworks.paranamer.AdaptiveParanamer;
// import com.thoughtworks.paranamer.Paranamer;

public class Verb {
    private String className;
    private Method method;
    private String verbAction;

    public Verb(String className, Method method, String verbAction) {
        this.className = className;
        this.method = method;
        this.verbAction = verbAction;
    }

    public String getClassName() {
        return className;
    }

    public Method getMethod() {
        return method;
    }

    public String getVerbAction() {
        return verbAction;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "className='" + className + '\'' +
                ", methodName='" + method.getName() + '\'' +
                ", verbAction='" + verbAction + '\'' +
                '}';
    }

    public String method_to_string() {
        StringBuilder methodString = new StringBuilder();
        methodString.append(method.getName()).append("(");

        Parameter[] parameters = method.getParameters();

        // Paranamer paranamer = new AdaptiveParanamer();
        // String[] parameterMethodNames = paranamer.lookupParameterNames(method);
        
        for (int i = 0; i < parameters.length; i++) {
            // methodString.append(parameters[i].getType().getSimpleName()+" "+parameterMethodNames[i]);
            methodString.append(parameters[i].getType().getSimpleName()+" "+parameters[i].getName());
            if (i < parameters.length - 1) {
                methodString.append(", ");
            }
        }

        methodString.append(")");
        return methodString.toString();
    }
}
