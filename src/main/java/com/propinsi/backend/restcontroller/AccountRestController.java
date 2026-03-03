package com.propinsi.backend.restcontroller;

import com.propinsi.backend.restdto.response.BaseResponse;
import com.propinsi.backend.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountRestController {

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