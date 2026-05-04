package com.brightcore.suppliermanagement.service;

import com.brightcore.suppliermanagement.dto.SupplierDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    SupplierDto.Response create(SupplierDto.CreateRequest request);

    SupplierDto.Response update(Long id, SupplierDto.UpdateRequest request);

    SupplierDto.Response getById(Long id);

    void delete(Long id);

    Page<SupplierDto.Response> list(Pageable pageable);
}
