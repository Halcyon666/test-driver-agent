package com.example.testdriver.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MoneyControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/money/growth returns calculated amount")
    void applyGrowthReturnsCalculatedResponse() throws Exception {
        String payload = """
                {
                  "amount": 100.00,
                  "currency": "USD",
                  "ratePercent": 10
                }
                """;

        mockMvc.perform(post("/api/money/growth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(110.0))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("POST /api/money/growth returns 400 with field errors for invalid body")
    void applyGrowthValidationFailure() throws Exception {
        String payload = """
                {
                  "amount": -1,
                  "currency": "   ",
                  "ratePercent": -5
                }
                """;

        mockMvc.perform(post("/api/money/growth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    @DisplayName("POST /api/money/growth returns 400 when domain validation fails")
    void applyGrowthDomainFailure() throws Exception {
        String payload = """
                {
                  "amount": 100,
                  "currency": "USD",
                  "ratePercent": 99999999999999999999999999999
                }
                """;

        mockMvc.perform(post("/api/money/growth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
