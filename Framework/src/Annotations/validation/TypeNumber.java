package Annotations.validation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeNumber {
    String message() default "Ce champ doit être un nombre.";
}
