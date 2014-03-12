package org.jboss.aerogear.unifiedpush.model.jpa.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeviceTokenValidator.class)
@Documented
public @interface DeviceTokenCheck {

    String message() default "{org.jboss.aerogear.unifiedpush.model.constraints.devicetoken}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
