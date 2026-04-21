package com.propinsi.backend.mengelola_lomba.controller;

import com.propinsi.backend.mengelola_lomba.model.StatusLomba;
import com.propinsi.backend.mengelola_lomba.restdto.request.AssignJuriRequest;
import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaDetailResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;
import com.propinsi.backend.mengelola_lomba.restdto.response.UserSummaryResponse;
import com.propinsi.backend.mengelola_lomba.service.LombaService;
import com.propinsi.backend.repository.UserRepository;
import com.propinsi.backend.model.User;
import com.propinsi.backend.restdto.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lomba")
public class LombaController {

    @Autowired
    private LombaService lombaService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')") 
    @PostMapping
    public ResponseEntity<LombaResponse> createLomba(@Valid @RequestBody LombaRequest request) {
        LombaResponse response = lombaService.createLomba(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LombaResponse> getLombaById(@PathVariable UUID id) {
        return ResponseEntity.ok(lombaService.getLombaById(id));
    }

    @GetMapping
    public ResponseEntity<List<LombaResponse>> getAllLomba(
            @RequestParam(required = false) String jenisBurung,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String nama) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean includeAll = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_KOORDINATOR_LOMBA"));
        return ResponseEntity.ok(lombaService.getAllLomba(jenisBurung, status, sortBy, sortDir, nama, includeAll));
    }

    @GetMapping("/by-juri")
    public ResponseEntity<List<LombaResponse>> getLombaByJuri(
            @RequestParam(required = false) String jenisBurung,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String nama) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Get current user ID from authentication
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User tidak ter-autentikasi");
        }
        
        // Get the username from the principal and find the user
        String username = auth.getName();
        // Assuming authentication principal is username, we need to get the user ID
        // This requires injecting UserRepository to find the user by username
        
        // Alternative: if your Authentication has user details with ID, extract it from there
        // For now, we'll need to get it from the Authentication principal
        // Let's assume the principal is a UserDetails object with username
        
        // Get the current authenticated user
        Long userId = null;
        Object principal = auth.getPrincipal();
        
        // If principal has getId method (custom UserDetails), use it
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // This is a standard UserDetails, we need another way to get ID
            // We'll use the username to find the user
            String principalUsername = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            userId = userRepository.findByUsername(principalUsername)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User tidak ditemukan"))
                    .getId();
        }
        
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tidak dapat mendapatkan ID user");
        }
        
        return ResponseEntity.ok(lombaService.getLombaByJuri(userId, jenisBurung, status, sortBy, sortDir, nama));
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<LombaResponse> updateLomba(
            @PathVariable UUID id, 
            @Valid @RequestBody LombaRequest request) {
        LombaResponse response = lombaService.updateLomba(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @PostMapping("/{lombaId}/assign-juri")
    public ResponseEntity<LombaResponse> assignJuri(
            @PathVariable UUID lombaId,
            @Valid @RequestBody AssignJuriRequest request
    ) {
        LombaResponse response = lombaService.assignJuriToLomba(lombaId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @DeleteMapping("/{lombaId}/remove-juri/{juriId}")
    public ResponseEntity<LombaResponse> removeJuri(
            @PathVariable UUID lombaId,
            @PathVariable Long juriId
    ) {
        LombaResponse response = lombaService.removeJuriFromLomba(lombaId, juriId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @GetMapping("/available-juri")
    public ResponseEntity<List<UserSummaryResponse>> getAvailableJuri() {
        return ResponseEntity.ok(lombaService.getAvailableJuri());
    }

    @PreAuthorize("hasAnyRole('KOORDINATOR_LOMBA', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteLomba(@PathVariable UUID id) {
        lombaService.deleteLomba(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Lomba berhasil dihapus"));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<LombaDetailResponse> getLombaDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(lombaService.getLombaDetail(id, currentUser));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        String statusString = request.get("status");
        StatusLomba status = StatusLomba.valueOf(statusString); 
        lombaService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}