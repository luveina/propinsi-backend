package com.propinsi.backend.restcontroller;

import com.propinsi.backend.model.User;
import com.propinsi.backend.restdto.request.AdminRegisterRequest;
import com.propinsi.backend.restdto.response.BaseResponse;
import com.propinsi.backend.service.AccountService;
import com.propinsi.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountRestController {
    @Autowired
    private AdminService adminService;

    /**
     * Daftar semua akun (hanya admin yang boleh mengakses).
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<User>>> getAll() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(BaseResponse.success(users, "Data akun berhasil diambil"));
    }

    /**
     * Buat akun baru; username harus unik.
     */
    @PostMapping
    public ResponseEntity<BaseResponse<User>> create(@Valid @RequestBody AdminRegisterRequest req,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors);
        }

        String adminName = SecurityContextHolder.getContext().getAuthentication().getName();
        User created;
        try {
            created = adminService.createAccount(req, adminName);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(created, "Akun berhasil dibuat"));
    }

    /**
     * Update data akun (nama, telepon, role).
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<User>> update(@PathVariable Long id,
                                                      @Valid @RequestBody AdminRegisterRequest req,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors);
        }

        String adminName = SecurityContextHolder.getContext().getAuthentication().getName();
        User updated;
        try {
            updated = adminService.updateAccount(id, req, adminName);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        if (updated == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Akun tidak ditemukan");
        }
        return ResponseEntity.ok(BaseResponse.success(updated, "Akun berhasil diperbarui"));
    }

    /**
     * Soft delete / deactivate akun. Change status to "Inactive".
     * Tidak boleh menghapus akun dengan role ADMIN.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deactivate(@PathVariable Long id) {
        // Cek apakah user yang akan dihapus adalah ADMIN
        User userToDelete = adminService.findById(id);
        if (userToDelete != null && userToDelete.getRole().getLabel().equals("Admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak dapat menghapus akun dengan role ADMIN");
        }
        
        String adminName = SecurityContextHolder.getContext().getAuthentication().getName();
        User deact = adminService.deactivateUser(id, adminName);
        if (deact == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Akun tidak ditemukan");
        }
        return ResponseEntity.ok(BaseResponse.success(null, "Akun berhasil dinonaktifkan"));
    }

    @Autowired
    private AccountService accountService;

    // PBI-10: Reset Password (Hanya Admin)
    @PreAuthorize("hasRole('ADMIN')") // Proteksi khusus Admin (Sesuai AC BE point 1)
    @PutMapping("/{id}/reset")
    public ResponseEntity<BaseResponse<Void>> resetPassword(@PathVariable Long id) {
        accountService.resetPassword(id);
        
        // Return 200 OK (Sesuai AC BE point 3)
        return ResponseEntity.ok(BaseResponse.success(null, "Password akun berhasil direset ke default"));
    }
}