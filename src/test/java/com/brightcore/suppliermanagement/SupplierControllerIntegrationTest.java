package com.brightcore.suppliermanagement;

import com.brightcore.suppliermanagement.dto.SupplierDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"supplier-events"})
class SupplierControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    @WithMockUser(roles = "ADMIN")
    void fullCrudFlow() throws Exception {
        SupplierDto.CreateRequest create = SupplierDto.CreateRequest.builder()
                .name("Acme Textiles")
                .email("acme-it@example.com")
                .phoneNumber("+91 9876543210")
                .companyName("Acme Pvt Ltd")
                .country("India")
                .build();

        String createdJson = mvc.perform(post("/api/v1/suppliers/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn().getResponse().getContentAsString();

        Long id = json.readTree(createdJson).path("data").path("id").asLong();

        mvc.perform(get("/api/v1/suppliers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("acme-it@example.com"));

        SupplierDto.UpdateRequest update = SupplierDto.UpdateRequest.builder()
                .name("Acme Textiles Renamed")
                .email("acme-it@example.com")
                .phoneNumber("+91 9000000000")
                .companyName("Acme Pvt Ltd")
                .country("India")
                .active(true)
                .build();

        mvc.perform(put("/api/v1/suppliers/update/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Acme Textiles Renamed"));

        mvc.perform(delete("/api/v1/suppliers/delete/" + id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/suppliers/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listShouldReturnPagedResponse() throws Exception {
        // seed one
        SupplierDto.CreateRequest create = SupplierDto.CreateRequest.builder()
                .name("Page Co").email("page@example.com").country("India").build();
        mvc.perform(post("/api/v1/suppliers/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(create)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/suppliers?page=0&size=10&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldReturn400OnValidationError() throws Exception {
        SupplierDto.CreateRequest bad = SupplierDto.CreateRequest.builder()
                .name("")
                .email("not-an-email")
                .build();

        mvc.perform(post("/api/v1/suppliers/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
