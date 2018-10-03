package com.branegy.service.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.branegy.dbmaster.core.Permission.Role;

/**
 * By default all properties look like AND operation.
 * 
 * @CheckedAccess - any authentication user
 * @CheckedAccess(admin=true) - any authentication admin
 * @CheckedAccess(createProject=true) - any authentication with can create project
 * @CheckedAccess(createProject=true, admin=true) - any authentication admin with can create project
 *              (degenerated case)
 * @CheckedAccess(createProject=true, admin=true) - any authentication admin with can create project
 * @CheckedAccess(roles={Role.READONLY,Role.CONTRIBUTOR,Role.FULL_CONTROL}) - user with any role in proect
 * @CheckedAccess(admin=true, roles={Role.READONLY,Role.CONTRIBUTOR,Role.FULL_CONTROL})
 *      user with any role in proect and admin
 * etc
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CheckedAccess {
    boolean admin() default false;
    boolean createProject() default false;
    Role[] roles() default {};
}