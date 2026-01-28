package com.ifconnected.controller;

import com.ifconnected.model.DTO.CreateLostFoundItemDTO;
import com.ifconnected.model.JPA.LostFoundItem;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.LostFoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Achados e Perdidos", description="Módulo JPA puro (DAWII)")
@RestController
@RequestMapping("/api/lost-found")
public class LostFoundController {

    private final LostFoundService service;

    public LostFoundController(LostFoundService service) {
        this.service = service;
    }

    @Operation(summary="Criar item de achados/perdidos")
    @PostMapping
    public LostFoundItem create(
            @AuthenticationPrincipal UserLoginInfo principal,
            @RequestBody CreateLostFoundItemDTO dto
    ) {
        return service.create(principal.getId(), dto);
    }

    @Operation(summary="Listar todos os itens")
    @GetMapping
    public List<LostFoundItem> listAll() {
        return service.listAll();
    }

    @Operation(summary="Buscar por texto (title/description)")
    @GetMapping("/search")
    public List<LostFoundItem> search(@RequestParam String q) {
        return service.search(q);
    }
}
