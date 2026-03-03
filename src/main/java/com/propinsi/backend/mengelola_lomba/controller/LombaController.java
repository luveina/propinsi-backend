package com.propinsi.backend.mengelola_lomba.controller;

import com.propinsi.backend.mengelola_lomba.model.Lomba;
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



    // PBI-13: Membuat Lomba
    @PostMapping
    public ResponseEntity<Lomba> createLomba(@Valid @RequestBody Lomba lomba) {
        Lomba createdLomba = lombaService.createLomba(lomba);
        return new ResponseEntity<>(createdLomba, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lomba> getLombaById(@PathVariable UUID id) {
        Lomba lomba = lombaService.getLombaById(id);
        return new ResponseEntity<>(lomba, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Lomba>> getAllLomba() {
        List<Lomba> listLomba = lombaService.getAllLomba();
        return new ResponseEntity<>(listLomba, HttpStatus.OK);
    }
}