package com.ecommerce.levelup.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogRepository repository;

    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "timestamp,desc") String sort
    ) {
      
        String[] sortParts = sort.split(",");
        Sort.Direction dir = Sort.Direction.DESC;
        String sortField = "timestamp";
        if (sortParts.length > 0 && !sortParts[0].isBlank()) {
            sortField = sortParts[0];
        }
        if (sortParts.length > 1) {
            dir = Sort.Direction.fromString(sortParts[1]);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortField));

        Specification<AuditLog> spec = Specification.where(null);

        if (username != null && !username.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }

        if (path != null && !path.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("path")), "%" + path.toLowerCase() + "%"));
        }

        if (success != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("success"), success));
        }

        if (from != null) {
            try {
                LocalDateTime fromDt = LocalDateTime.parse(from);
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), fromDt));
            } catch (DateTimeParseException e) {
              
            }
        }

        if (to != null) {
            try {
                LocalDateTime toDt = LocalDateTime.parse(to);
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), toDt));
            } catch (DateTimeParseException e) {
              
            }
        }

        Page<AuditLog> p = repository.findAll(spec, pageable);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/logs/user/{username}")
    public ResponseEntity<Page<AuditLog>> getByUsername(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @PathVariable String username
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<AuditLog> spec = (root, query, cb) -> 
            cb.equal(cb.lower(root.get("username")), username.toLowerCase());
        
        Page<AuditLog> logs = repository.findAll(spec, pageable);
        return ResponseEntity.ok(logs);
    }

   
    @GetMapping("/logs/failed")
    public ResponseEntity<Page<AuditLog>> getFailedOperations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<AuditLog> spec = (root, query, cb) -> cb.equal(root.get("success"), false);
        
        Page<AuditLog> logs = repository.findAll(spec, pageable);
        return ResponseEntity.ok(logs);
    }

   
    @GetMapping("/logs/recent")
    public ResponseEntity<Page<AuditLog>> getRecentLogs() {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = repository.findAll(pageable);
        return ResponseEntity.ok(logs);
    }
}
