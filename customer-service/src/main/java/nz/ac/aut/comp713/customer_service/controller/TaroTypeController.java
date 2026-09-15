package nz.ac.aut.comp713.customer_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import nz.ac.aut.comp713.customer_service.dto.TaroTypeRequest;
import nz.ac.aut.comp713.customer_service.dto.TaroTypeResponse;
import nz.ac.aut.comp713.customer_service.service.TaroTypeService;

@RestController
@RequestMapping("/api/v1/taro-types")
public class TaroTypeController {

    private final TaroTypeService taroTypeService;

    public TaroTypeController(TaroTypeService taroTypeService) {
        this.taroTypeService = taroTypeService;
    }

    // get all taro types
    @GetMapping
    public List<TaroTypeResponse> getAllTaroTypes() {
        return taroTypeService.getAllTaroTypes();
    }

    // get a taro type by id
    @GetMapping("/{id}")
    public TaroTypeResponse getTaroTypeById(@PathVariable Long id) {
        return taroTypeService.getTaroTypeById(id);
    }

    // create a new taro type
    @PostMapping
    public ResponseEntity<TaroTypeResponse> createTaroType(
            @Valid @RequestBody TaroTypeRequest request) {

        TaroTypeResponse created = taroTypeService.createTaroType(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(created);
    }

    // update an existing taro type
    @PutMapping("/{id}")
    public ResponseEntity<TaroTypeResponse> updateTaroType(
            @PathVariable Long id,
            @Valid @RequestBody TaroTypeRequest request) {

        TaroTypeResponse updated = taroTypeService.updateTaroType(id, request);

        return ResponseEntity.ok(updated);
    }
}