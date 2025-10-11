package com.task10.controller;

import com.task10.entity.Tenant;
import com.task10.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TenantService tenantService;

    @PostMapping("/tenants")
    public ResponseEntity<Tenant> createTenant(@RequestParam String name) {
        try {
            Tenant tenant = tenantService.createTenant(name);
            return ResponseEntity.ok(tenant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/tenants/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
//        tenantService.deleteTenant(id);
        return ResponseEntity.ok().build();
    }
}