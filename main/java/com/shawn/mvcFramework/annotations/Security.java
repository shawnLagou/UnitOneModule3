package com.shawn.mvcFramework.annotations;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Security {

    enum EmptyRoleSemantic {
        /**
         * access is to be permitted independent of authentication state and
         * identity.
         */
        PERMIT,
        /**
         * access is to be denied independent of authentication state and
         * identity.
         */
        DENY
    }

    String[] httpMethodConstraints() default {};

}
