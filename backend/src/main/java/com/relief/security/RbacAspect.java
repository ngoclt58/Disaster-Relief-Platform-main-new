package com.relief.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class RbacAspect {
    
    @Autowired
    private RbacService rbacService;
    
    @Around("@annotation(com.relief.security.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        
        Permission[] requiredPermissions = annotation.value();
        boolean requireAll = annotation.requireAll();
        
        boolean hasAccess;
        if (requireAll) {
            hasAccess = rbacService.hasAllPermissions(requiredPermissions);
        } else {
            hasAccess = rbacService.hasAnyPermission(requiredPermissions);
        }
        
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Insufficient permissions. Required: " + Arrays.toString(requiredPermissions));
        }
        
        return joinPoint.proceed();
    }
    
    @Around("@annotation(com.relief.security.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresRole annotation = method.getAnnotation(RequiresRole.class);
        
        Role[] requiredRoles = annotation.value();
        boolean requireAll = annotation.requireAll();
        
        boolean hasAccess;
        if (requireAll) {
            hasAccess = Arrays.stream(requiredRoles).allMatch(rbacService::hasRole);
        } else {
            hasAccess = rbacService.hasAnyRole(requiredRoles);
        }
        
        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Insufficient role. Required: " + Arrays.toString(requiredRoles));
        }
        
        return joinPoint.proceed();
    }
}
