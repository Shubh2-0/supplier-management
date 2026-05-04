package com.brightcore.suppliermanagement;

import com.brightcore.suppliermanagement.dto.SupplierDto;
import com.brightcore.suppliermanagement.entity.Supplier;
import com.brightcore.suppliermanagement.event.EventType;
import com.brightcore.suppliermanagement.event.SupplierEvent;
import com.brightcore.suppliermanagement.exception.DuplicateResourceException;
import com.brightcore.suppliermanagement.exception.ResourceNotFoundException;
import com.brightcore.suppliermanagement.kafka.SupplierKafkaProducer;
import com.brightcore.suppliermanagement.mapper.SupplierMapper;
import com.brightcore.suppliermanagement.repository.SupplierRepository;
import com.brightcore.suppliermanagement.service.impl.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock SupplierRepository repository;
    @Mock SupplierKafkaProducer producer;

    SupplierMapper mapper = new SupplierMapper();

    @InjectMocks
    SupplierServiceImpl service;

    @BeforeEach
    void wireMapper() {
        // Re-create with the real mapper instance (Mockito won't inject a non-mock).
        service = new SupplierServiceImpl(repository, mapper, producer);
    }

    private Supplier sampleEntity() {
        Instant now = Instant.now();
        return Supplier.builder()
                .id(1L)
                .name("Acme Textiles")
                .email("contact@acme.example")
                .phoneNumber("+91 9876543210")
                .companyName("Acme Pvt Ltd")
                .address("Mumbai")
                .country("India")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void create_shouldPersistAndPublishCreatedEvent() {
        SupplierDto.CreateRequest req = SupplierDto.CreateRequest.builder()
                .name("Acme Textiles")
                .email("Contact@Acme.Example")
                .phoneNumber("+91 9876543210")
                .companyName("Acme Pvt Ltd")
                .address("Mumbai")
                .country("India")
                .build();

        when(repository.existsByEmailIgnoreCase("contact@acme.example")).thenReturn(false);
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            s.setId(1L);
            s.setCreatedAt(Instant.now());
            s.setUpdatedAt(Instant.now());
            return s;
        });

        SupplierDto.Response response = service.create(req);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("contact@acme.example");

        ArgumentCaptor<SupplierEvent> captor = ArgumentCaptor.forClass(SupplierEvent.class);
        verify(producer, times(1)).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.SUPPLIER_CREATED);
        assertThat(captor.getValue().getSupplierId()).isEqualTo(1L);
    }

    @Test
    void create_shouldRejectDuplicateEmail() {
        SupplierDto.CreateRequest req = SupplierDto.CreateRequest.builder()
                .name("Dup")
                .email("dup@example.com")
                .build();
        when(repository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(DuplicateResourceException.class);

        verify(producer, never()).publish(any());
    }

    @Test
    void getById_shouldReturnAndPublishRetrievedEvent() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity()));

        SupplierDto.Response response = service.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);

        ArgumentCaptor<SupplierEvent> captor = ArgumentCaptor.forClass(SupplierEvent.class);
        verify(producer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.SUPPLIER_RETRIEVED);
    }

    @Test
    void getById_shouldThrowWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(producer, never()).publish(any());
    }

    @Test
    void update_shouldApplyChangesAndPublishUpdatedEvent() {
        Supplier existing = sampleEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierDto.UpdateRequest req = SupplierDto.UpdateRequest.builder()
                .name("Acme Textiles Renamed")
                .email("contact@acme.example")
                .phoneNumber("+91 9999999999")
                .companyName("Acme Pvt Ltd")
                .address("Pune")
                .country("India")
                .active(false)
                .build();

        SupplierDto.Response response = service.update(1L, req);

        assertThat(response.getName()).isEqualTo("Acme Textiles Renamed");
        assertThat(response.getActive()).isFalse();

        ArgumentCaptor<SupplierEvent> captor = ArgumentCaptor.forClass(SupplierEvent.class);
        verify(producer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.SUPPLIER_UPDATED);
    }

    @Test
    void delete_shouldRemoveAndPublishDeletedEvent() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity()));

        service.delete(1L);

        verify(repository).delete(any(Supplier.class));
        ArgumentCaptor<SupplierEvent> captor = ArgumentCaptor.forClass(SupplierEvent.class);
        verify(producer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.SUPPLIER_DELETED);
        assertThat(captor.getValue().getSupplierId()).isEqualTo(1L);
    }
}
