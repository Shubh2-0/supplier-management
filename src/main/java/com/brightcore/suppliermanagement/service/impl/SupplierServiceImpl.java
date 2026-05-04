package com.brightcore.suppliermanagement.service.impl;

import com.brightcore.suppliermanagement.dto.SupplierDto;
import com.brightcore.suppliermanagement.entity.Supplier;
import com.brightcore.suppliermanagement.event.EventType;
import com.brightcore.suppliermanagement.event.SupplierEvent;
import com.brightcore.suppliermanagement.exception.DuplicateResourceException;
import com.brightcore.suppliermanagement.exception.ResourceNotFoundException;
import com.brightcore.suppliermanagement.kafka.SupplierKafkaProducer;
import com.brightcore.suppliermanagement.mapper.SupplierMapper;
import com.brightcore.suppliermanagement.repository.SupplierRepository;
import com.brightcore.suppliermanagement.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;
    private final SupplierMapper mapper;
    private final SupplierKafkaProducer producer;

    @Override
    @Transactional
    public SupplierDto.Response create(SupplierDto.CreateRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Supplier with email '" + email + "' already exists");
        }

        Supplier saved = repository.save(mapper.toEntity(request));
        SupplierDto.Response response = mapper.toResponse(saved);

        producer.publish(SupplierEvent.of(EventType.SUPPLIER_CREATED, saved.getId(), response));
        log.debug("Created supplier id={} email={}", saved.getId(), saved.getEmail());
        return response;
    }

    @Override
    @Transactional
    public SupplierDto.Response update(Long id, SupplierDto.UpdateRequest request) {
        Supplier existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + id));

        String newEmail = request.getEmail().trim().toLowerCase();
        if (!existing.getEmail().equalsIgnoreCase(newEmail)
                && repository.existsByEmailIgnoreCase(newEmail)) {
            throw new DuplicateResourceException("Another supplier already uses email '" + newEmail + "'");
        }

        mapper.applyUpdate(existing, request);
        Supplier saved = repository.save(existing);
        SupplierDto.Response response = mapper.toResponse(saved);

        producer.publish(SupplierEvent.of(EventType.SUPPLIER_UPDATED, saved.getId(), response));
        log.debug("Updated supplier id={}", saved.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDto.Response getById(Long id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + id));

        SupplierDto.Response response = mapper.toResponse(supplier);
        producer.publish(SupplierEvent.of(EventType.SUPPLIER_RETRIEVED, supplier.getId(), response));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDto.Response> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + id));

        SupplierDto.Response snapshot = mapper.toResponse(supplier);
        repository.delete(supplier);

        producer.publish(SupplierEvent.of(EventType.SUPPLIER_DELETED, id, snapshot));
        log.debug("Deleted supplier id={}", id);
    }
}
