package com.ecommerce.levelup.user.service;

import com.ecommerce.levelup.user.dto.CreateRoleRequest;
import com.ecommerce.levelup.user.dto.RoleDTO;
import com.ecommerce.levelup.user.model.Role;
import com.ecommerce.levelup.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        return convertToDTO(role);
    }

    
    @Transactional
    public RoleDTO createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + request.getName());
        }

        if (!request.getName().startsWith("ROLE_")) {
            throw new RuntimeException("El nombre del rol debe empezar con ROLE_");
        }

        Role role = new Role();
        role.setName(request.getName().toUpperCase());
        role.setDescription(request.getDescription());

        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    @Transactional
    public RoleDTO updateRole(Long id, CreateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        if (!role.getName().equals(request.getName()) && 
            roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + request.getName());
        }

        if (role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_USER")) {
            throw new RuntimeException("No se puede modificar el rol del sistema: " + role.getName());
        }

        role.setName(request.getName().toUpperCase());
        role.setDescription(request.getDescription());

        Role updatedRole = roleRepository.save(role);
        return convertToDTO(updatedRole);
    }

    
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        if (role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_USER")) {
            throw new RuntimeException("No se puede eliminar el rol del sistema: " + role.getName());
        }

        if (!role.getUsers().isEmpty()) {
            throw new RuntimeException("No se puede eliminar el rol porque tiene " + 
                role.getUsers().size() + " usuario(s) asociado(s)");
        }

        roleRepository.delete(role);
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setUserCount(role.getUsers() != null ? role.getUsers().size() : 0);
        return dto;
    }
}
