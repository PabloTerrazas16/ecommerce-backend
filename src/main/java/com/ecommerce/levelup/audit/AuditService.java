package com.ecommerce.levelup.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    public AuditLog save(AuditLog log) {
        return repository.save(log);
    }

    public List<AuditLog> findAll() {
        return repository.findAll();
    }
}
