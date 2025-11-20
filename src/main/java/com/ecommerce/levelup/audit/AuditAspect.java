package com.ecommerce.levelup.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("within(com.ecommerce.levelup..controller..*)")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attrs instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) attrs).getRequest();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";
        String roles = "";
        if (auth != null) {
            roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
        }

        String path = request != null ? request.getRequestURI() : "-";
        String method = request != null ? request.getMethod() : "-";

        String action = pjp.getSignature().getDeclaringType().getSimpleName() + "#" + pjp.getSignature().getName();
        String argsString = "";
        try {
            argsString = objectMapper.writeValueAsString(pjp.getArgs());
        } catch (JsonProcessingException e) {
            argsString = "[unable to serialize args]";
        }

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setRoles(roles);
        log.setHttpMethod(method);
        log.setPath(path);
        log.setAction(action);
        log.setArguments(argsString);
        log.setTimestamp(LocalDateTime.now());

        try {
            Object result = pjp.proceed();
            log.setSuccess(true);
            // Optionally store result summary
            auditService.save(log);
            return result;
        } catch (Throwable t) {
            log.setSuccess(false);
            log.setDetails(t.getMessage());
            auditService.save(log);
            throw t;
        }
    }
}
