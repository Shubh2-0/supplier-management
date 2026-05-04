package com.brightcore.suppliermanagement.controller;

import com.brightcore.suppliermanagement.dto.ApiResponse;
import com.brightcore.suppliermanagement.dto.PageResponse;
import com.brightcore.suppliermanagement.dto.SupplierDto;
import com.brightcore.suppliermanagement.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier Management endpoints")
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "Add a new supplier", description = "Creates a supplier and publishes a SUPPLIER_CREATED event")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Supplier created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<SupplierDto.Response>> add(
            @Valid @RequestBody SupplierDto.CreateRequest request) {

        SupplierDto.Response created = supplierService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/suppliers/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok("Supplier created", created));
    }

    @Operation(summary = "Update an existing supplier", description = "Replaces supplier fields and publishes a SUPPLIER_UPDATED event")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already taken by another supplier")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<SupplierDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierDto.UpdateRequest request) {

        SupplierDto.Response updated = supplierService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Supplier updated", updated));
    }

    @Operation(summary = "List suppliers (paginated)",
               description = "Returns a paginated list of suppliers. Supports `page`, `size`, and `sort=field,asc|desc`.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SupplierDto.Response>>> list(
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<SupplierDto.Response> page = supplierService.list(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Suppliers fetched", PageResponse.from(page)));
    }

    @Operation(summary = "Get supplier by id", description = "Retrieves a supplier and publishes a SUPPLIER_RETRIEVED event")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto.Response>> getById(@PathVariable Long id) {
        SupplierDto.Response supplier = supplierService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Supplier retrieved", supplier));
    }

    @Operation(summary = "Delete a supplier", description = "Deletes the supplier and publishes a SUPPLIER_DELETED event")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Supplier deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
