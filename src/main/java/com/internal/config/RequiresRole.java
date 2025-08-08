package com.internal.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RequiresRole {
    /**
     * The required roles for accessing the annotated method or class.
     * @return Array of role names required for access
     */
    String[] value();

    /**
     * If true, user needs any of the roles specified.
     * If false, user needs all roles specified.
     * Default is false (requiring all roles).
     * @return Access control logic type
     */
    boolean anyRole() default false;

    /**
     * Custom message to be shown when access is denied due to insufficient roles.
     * @return Custom access denied message
     */
    String message() default "You do not have the required permissions to access this resource";
}