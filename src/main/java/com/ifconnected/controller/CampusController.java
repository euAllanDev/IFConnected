package com.ifconnected.controller;

import com.ifconnected.model.DTO.CampusDTO;
import com.ifconnected.service.CampusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CampusController {

    private final CampusService campusService;

    public CampusController(CampusService campusService) {
        this.campusService = campusService;
    }

    @GetMapping("/campus")
    public List<CampusDTO> getAllCampuses() {
        return campusService.getAll();
    }
}