package com.propinsi.backend.pendaftaran_lomba.restdto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    @JsonProperty("gantangan_no")
    private Integer gantanganNo;
    
    @JsonProperty("participant_id")
    private UUID participantId;
    
    @JsonProperty("participant_name")
    private String participantName;
    
    @JsonProperty("bird_type")
    private String birdType;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    @JsonProperty("is_present")
    private Boolean isPresent;
    
    @JsonProperty("class")
    private String classValue;
}
