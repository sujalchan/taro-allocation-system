package nz.ac.aut.comp713.allocation_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nz.ac.aut.comp713.allocation_service.dto.WeeklyAllocationRequest;
import nz.ac.aut.comp713.allocation_service.dto.WeeklyAllocationResponse;
import nz.ac.aut.comp713.allocation_service.service.WeeklyAllocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import nz.ac.aut.comp713.allocation_service.dto.ApiError;

@RestController
@RequestMapping("/api/v1/allocations")
public class WeeklyAllocationController {

        private final WeeklyAllocationService weeklyAllocationService;

        public WeeklyAllocationController(WeeklyAllocationService weeklyAllocationService) {
                this.weeklyAllocationService = weeklyAllocationService;
        }

        // get all weekly allocations
        @Operation(summary = "Retrieve all weekly allocations")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Weekly allocations retrieved successfully"),
                        @ApiResponse(responseCode = "503", description = "Customer service is unavailable", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        })
        @GetMapping
        public ResponseEntity<List<WeeklyAllocationResponse>> getAllWeeklyAllocations() {
                return ResponseEntity.ok(weeklyAllocationService.getAllWeeklyAllocations());
        }

        // get a weekly allocation by id
        @Operation(summary = "Retrieve a weekly allocation by ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Weekly allocation retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Weekly allocation not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "503", description = "Customer service is unavailable", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<WeeklyAllocationResponse> getWeeklyAllocationById(@PathVariable Long id) {
                return ResponseEntity.ok(weeklyAllocationService.getWeeklyAllocationById(id));
        }

        // create a new weekly allocation
        @Operation(summary = "Create a weekly allocation")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Weekly allocation created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid allocation data or duplicate taro type", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "404", description = "Customer or taro type not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "409", description = "Weekly allocation already exists for the customer and week", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "503", description = "Customer service is unavailable", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        })
        @PostMapping
        public ResponseEntity<WeeklyAllocationResponse> createWeeklyAllocation(
                        @Valid @RequestBody WeeklyAllocationRequest request) {

                WeeklyAllocationResponse response = weeklyAllocationService.createWeeklyAllocation(request);
                URI location = URI.create("/api/v1/allocations/" + response.id());
                return ResponseEntity
                                .created(location)
                                .body(response);
        }

        // update an existing weekly allocation
        @Operation(summary = "Update an existing weekly allocation")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Weekly allocation updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid allocation data or duplicate taro type", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "404", description = "Allocation, customer, or taro type not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "409", description = "Another weekly allocation already exists for the customer and week", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "503", description = "Customer service is unavailable", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
        })
        @PutMapping("/{id}")
        public ResponseEntity<WeeklyAllocationResponse> updateWeeklyAllocation(
                        @PathVariable Long id,
                        @Valid @RequestBody WeeklyAllocationRequest request) {

                return ResponseEntity.ok(
                                weeklyAllocationService.updateWeeklyAllocation(
                                                id,
                                                request));
        }

        // delete a weekly allocation
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteWeeklyAllocation(@PathVariable Long id) {
                weeklyAllocationService.deleteWeeklyAllocation(id);
                return ResponseEntity.noContent().build();
        }
}