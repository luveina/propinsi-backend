package com.propinsi.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_blacklist")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class JwtBlacklist {
    @Id
    private String token; 
    private LocalDateTime expiryDate;
}