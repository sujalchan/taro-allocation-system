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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import nz.ac.aut.comp713.customer_service.dto.TaroTypeRequest;
import nz.ac.aut.comp713.customer_service.dto.TaroTypeResponse;
import nz.ac.aut.comp713.customer_service.service.TaroTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import nz.ac.aut.comp713.customer_service.dto.ApiError;

@RestController
@RequestMapping("/api/v1/taro-types")
public class TaroTypeController {

        private final TaroTypeService taroTypeService;

        public TaroTypeController(TaroTypeService taroTypeService) {
                this.taroTypeService = taroTypeService;
        }

        // get all taro types
        @Operation(summary = "Retrieve all taro types")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Taro types retrieved successfully")
        })
        @GetMapping
        public List<TaroTypeResponse> getAllTaroTypes(
                        @RequestParam(required = false) String search) {

                return taroTypeService.getAllTaroTypes(search);
        }

        // get a taro type by id
        @Operation(summary = "Retrieve a taro type by ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Taro type retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Taro type not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        })
        @GetMapping("/{id}")
        public TaroTypeResponse getTaroTypeById(@PathVariable Long id) {
                return taroTypeService.getTaroTypeById(id);
        }

        // create a new taro type
        @Operation(summary = "Create a taro type")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Taro type created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid taro type data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "409", description = "Taro type already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        })
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
        @Operation(summary = "Update an existing taro type")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Taro type updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid taro type data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "404", description = "Taro type not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "409", description = "Taro type name already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        })
        @PutMapping("/{id}")
        public ResponseEntity<TaroTypeResponse> updateTaroType(
                        @PathVariable Long id,
                        @Valid @RequestBody TaroTypeRequest request) {

                TaroTypeResponse updated = taroTypeService.updateTaroType(id, request);

                return ResponseEntity.ok(updated);
        }
}