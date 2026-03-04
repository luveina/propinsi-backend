package com.propinsi.backend.mengelola_lomba.controller;

import com.propinsi.backend.mengelola_lomba.model.Lomba;
import com.propinsi.backend.mengelola_lomba.restdto.request.LombaRequest;
import com.propinsi.backend.mengelola_lomba.restdto.response.LombaResponse;
import com.propinsi.backend.mengelola_lomba.service.LombaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lomba")
public class LombaController {

    @Autowired
    private LombaService lombaService;

    @PostMapping
    public ResponseEntity<LombaResponse> createLomba(@Valid @RequestBody LombaRequest request) {
        return new ResponseEntity<>(lombaService.createLomba(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LombaResponse> getLombaById(@PathVariable UUID id) {
        return ResponseEntity.ok(lombaService.getLombaById(id));
    }
}