package Annotations.validation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeDate {
    String message() default "Format de date invalide.";
    String pattern() default "yyyy-MM-dd";
}
