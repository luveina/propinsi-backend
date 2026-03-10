package com.propinsi.backend.mengelola_lomba.restdto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String fullName;
    private String role;
}
