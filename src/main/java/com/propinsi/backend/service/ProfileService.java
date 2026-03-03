package com.propinsi.backend.service;

import com.propinsi.backend.restdto.request.UpdatePasswordRequest;
import com.propinsi.backend.restdto.response.UserProfileResponse;

public interface ProfileService {
    UserProfileResponse getUserProfile(String username);
    void updatePassword(String username, UpdatePasswordRequest request);
}