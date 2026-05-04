package com.brightcore.suppliermanagement.mapper;

import com.brightcore.suppliermanagement.dto.SupplierDto;
import com.brightcore.suppliermanagement.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toEntity(SupplierDto.CreateRequest req) {
        return Supplier.builder()
                .name(req.getName().trim())
                .email(req.getEmail().trim().toLowerCase())
                .phoneNumber(req.getPhoneNumber())
                .companyName(req.getCompanyName())
                .address(req.getAddress())
                .country(req.getCountry())
                .active(true)
                .build();
    }

    public void applyUpdate(Supplier target, SupplierDto.UpdateRequest req) {
        target.setName(req.getName().trim());
        target.setEmail(req.getEmail().trim().toLowerCase());
        target.setPhoneNumber(req.getPhoneNumber());
        target.setCompanyName(req.getCompanyName());
        target.setAddress(req.getAddress());
        target.setCountry(req.getCountry());
        if (req.getActive() != null) {
            target.setActive(req.getActive());
        }
    }

    public SupplierDto.Response toResponse(Supplier s) {
        return SupplierDto.Response.builder()
                .id(s.getId())
                .name(s.getName())
                .email(s.getEmail())
                .phoneNumber(s.getPhoneNumber())
                .companyName(s.getCompanyName())
                .address(s.getAddress())
                .country(s.getCountry())
                .active(s.getActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
