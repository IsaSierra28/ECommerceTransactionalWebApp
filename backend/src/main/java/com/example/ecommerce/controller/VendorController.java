package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ErrorResponse;
import com.example.ecommerce.dto.VendorRequest;
import com.example.ecommerce.dto.VendorResponse;
import com.example.ecommerce.dto.VendorUpdateRequest;
import com.example.ecommerce.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@Tag(name = "Vendors", description = "Manage vendors (ADMIN only)")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    @Operation(summary = "List all vendors")
    public List<VendorResponse> getVendors() {
        return vendorService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one vendor")
    @ApiResponse(responseCode = "404", description = "Vendor not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public VendorResponse getVendor(@PathVariable Integer id) {
        return vendorService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a vendor", description = "Also creates the user account of the vendor (role VENDOR).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vendor created"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username, email or company already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VendorResponse> createVendor(@Valid @RequestBody VendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.createVendor(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a vendor")
    public VendorResponse updateVendor(@PathVariable Integer id, @Valid @RequestBody VendorUpdateRequest request) {
        return vendorService.updateVendor(id, request);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a vendor")
    public VendorResponse activateVendor(@PathVariable Integer id) {
        return vendorService.setActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a vendor", description = "The products of an inactive vendor cannot be bought.")
    public VendorResponse deactivateVendor(@PathVariable Integer id) {
        return vendorService.setActive(id, false);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vendor", description = "Only possible if the vendor has no products.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vendor deleted"),
            @ApiResponse(responseCode = "409", description = "The vendor has products",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteVendor(@PathVariable Integer id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }
}
