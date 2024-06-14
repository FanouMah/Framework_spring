package mg.prom16;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Mapping {
    private String className;
    private Method method;

    public Mapping(String className, Method method) {
        this.className = className;
        this.method = method;
    }

    public String getClassName() {
        return className;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "className='" + className + '\'' +
                ", methodName='" + method.getName() + '\'' +
                '}';
    }

    public String method_to_string() {
        StringBuilder methodString = new StringBuilder();
        methodString.append(method.getName()).append("(");

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            methodString.append(parameters[i].getType().getSimpleName()+" "+parameters[i].getName());
            if (i < parameters.length - 1) {
                methodString.append(", ");
            }
        }

        methodString.append(")");
        return methodString.toString();
    }
}

