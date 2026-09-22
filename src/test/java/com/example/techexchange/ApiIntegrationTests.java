package com.example.techexchange;

import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.UserRole;
import com.example.techexchange.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Test
    void profileUpdateAndOwnershipChecks() throws Exception {
        String ownerToken = registerAndGetToken("owner1@test.local", "dev-owner1");
        long ownerId = myProfile(ownerToken).get("id").asLong();

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "dev-owner1-updated"))))
                .andExpect(status().isOk());

        JsonNode updatedMe = myProfile(ownerToken);
        assertThat(updatedMe.get("fullName").asText()).isEqualTo("dev-owner1-updated");
        assertThat(updatedMe.get("averageRating").asDouble()).isEqualTo(0.0);

        long deviceId = createDevice(ownerToken, "Owner Laptop", "LAPTOP").get("id").asLong();

        String otherToken = registerAndGetToken("other1@test.local", "dev-other1");

        mockMvc.perform(put("/api/devices/{id}", deviceId)
                        .header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(devicePayload("Hacked", "LAPTOP"))))
                .andExpect(status().isForbidden());

        JsonNode ownerProfile = userProfile(otherToken, ownerId);
        assertThat(ownerProfile.get("email").asText()).isEqualTo("owner1@test.local");
    }

    @Test
    void exchangeLifecycleAutoDeclineAndReviews() throws Exception {
        String ownerToken = registerAndGetToken("owner2@test.local", "dev-owner2");
        JsonNode ownerProfile = myProfile(ownerToken);
        long ownerId = ownerProfile.get("id").asLong();

        String requester1Token = registerAndGetToken("requester1@test.local", "dev-requester1");
        long requester1Id = myProfile(requester1Token).get("id").asLong();

        String requester2Token = registerAndGetToken("requester2@test.local", "dev-requester2");

        long targetId = createDevice(ownerToken, "Target Phone", "SMARTPHONE").get("id").asLong();
        long offered1Id = createDevice(requester1Token, "Offer Laptop", "LAPTOP").get("id").asLong();
        long offered2Id = createDevice(requester2Token, "Offer Console", "GAMING").get("id").asLong();

        long req1Id = createExchangeRequest(requester1Token, targetId, offered1Id, "exchange 1").get("id").asLong();
        long req2Id = createExchangeRequest(requester2Token, targetId, offered2Id, "exchange 2").get("id").asLong();

        JsonNode accepted = updateExchangeStatus(ownerToken, req1Id, "ACCEPTED");
        assertThat(accepted.get("status").asText()).isEqualTo("ACCEPTED");

        JsonNode requester2List = listExchangeRequests(requester2Token);
        JsonNode req2 = findById(requester2List, req2Id);
        assertThat(req2.get("status").asText()).isEqualTo("DECLINED");

        JsonNode issued = updateExchangeStatus(ownerToken, req1Id, "ISSUED");
        assertThat(issued.get("status").asText()).isEqualTo("ISSUED");

        JsonNode returned = updateExchangeStatus(requester1Token, req1Id, "RETURNED");
        assertThat(returned.get("status").asText()).isEqualTo("RETURNED");

        JsonNode targetDevice = getDevice(ownerToken, targetId);
        assertThat(targetDevice.get("status").asText()).isEqualTo("EXCHANGED");

        JsonNode reviewToOwner = createReview(requester1Token, req1Id, 5, "great trade");
        assertThat(reviewToOwner.get("toUserId").asLong()).isEqualTo(ownerId);

        JsonNode reviewToRequester = createReview(ownerToken, req1Id, 4, "smooth process");
        assertThat(reviewToRequester.get("toUserId").asLong()).isEqualTo(requester1Id);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", bearer(requester1Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "exchangeRequestId", req1Id,
                                "rating", 5,
                                "comment", "duplicate"
                        ))))
                .andExpect(status().isConflict());

        JsonNode ownerAfterReviews = userProfile(requester1Token, ownerId);
        assertThat(ownerAfterReviews.get("reviewsCount").asLong()).isEqualTo(1L);
        assertThat(ownerAfterReviews.get("averageRating").asDouble()).isEqualTo(5.0);
    }

    @Test
    void notificationsAndAdminModeration() throws Exception {
        String ownerToken = registerAndGetToken("owner3@test.local", "dev-owner3");
        String requesterToken = registerAndGetToken("requester3@test.local", "dev-requester3");

        long targetId = createDevice(ownerToken, "Target Camera", "CAMERA").get("id").asLong();
        long offeredId = createDevice(requesterToken, "Offer Tablet", "TABLET").get("id").asLong();

        long requestId = createExchangeRequest(requesterToken, targetId, offeredId, "please exchange").get("id").asLong();

        JsonNode ownerNotifications = listNotifications(ownerToken);
        assertThat(ownerNotifications.isArray()).isTrue();
        assertThat(ownerNotifications.get(0).get("type").asText()).isEqualTo("EXCHANGE_REQUEST_CREATED");

        createMessage(requesterToken, requestId, "hello from requester");
        JsonNode ownerNotificationsAfterMessage = listNotifications(ownerToken);
        assertThat(ownerNotificationsAfterMessage.get(0).get("type").asText()).isIn("NEW_MESSAGE", "EXCHANGE_REQUEST_CREATED");

        String adminToken = registerAndGetToken("admin@test.local", "dev-admin-test");
        User adminUser = userRepository.findByEmail("admin@test.local").orElseThrow();
        adminUser.setRole(UserRole.ADMIN);
        userRepository.save(adminUser);
        adminToken = loginAndGetToken("admin@test.local", "password123");

        long requesterId = myProfile(requesterToken).get("id").asLong();

        mockMvc.perform(put("/api/admin/users/{id}/active", requesterId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("active", false))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "requester3@test.local", "password", "password123"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/devices/{id}/active", targetId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("active", false))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/devices/{id}", targetId)
                        .header("Authorization", bearer(requesterToken)))
                .andExpect(status().isForbidden());
    }

    private JsonNode myProfile(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return json(result);
    }

    private JsonNode userProfile(String token, long userId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/{id}", userId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return json(result);
    }

    private JsonNode createDevice(String token, String title, String category) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/devices")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(devicePayload(title, category))))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result);
    }

    private JsonNode getDevice(String token, long deviceId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/devices/{id}", deviceId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return json(result);
    }

    private Map<String, Object> devicePayload(String title, String category) {
        return Map.of(
                "title", title,
                "category", category,
                "condition", "GOOD",
                "status", "AVAILABLE",
                "brand", "Brand",
                "model", "Model",
                "city", "Kyiv",
                "desiredExchange", "Any",
                "description", "test",
                "imageUrl", "https://example.com/img.png"
        );
    }

    private JsonNode createExchangeRequest(String token, long targetDeviceId, Long offeredDeviceId, String message) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/exchange-requests")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "targetDeviceId", targetDeviceId,
                                "offeredDeviceId", offeredDeviceId,
                                "message", message
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result);
    }

    private JsonNode listExchangeRequests(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/exchange-requests")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return json(result);
    }

    private JsonNode updateExchangeStatus(String token, long requestId, String statusValue) throws Exception {
        MvcResult result = mockMvc.perform(put("/api/exchange-requests/{id}/status", requestId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", statusValue))))
                .andExpect(status().isOk())
                .andReturn();
        return json(result);
    }

    private JsonNode createReview(String token, long requestId, int rating, String comment) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reviews")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "exchangeRequestId", requestId,
                                "rating", rating,
                                "comment", comment
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result);
    }

    private void createMessage(String token, long requestId, String text) throws Exception {
        mockMvc.perform(post("/api/messages/request/{requestId}", requestId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("text", text))))
                .andExpect(status().isCreated());
    }

    private JsonNode listNotifications(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return json(result);
    }

    private String registerAndGetToken(String email, String fullName) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", "password123",
                                "fullName", fullName
                        ))))
                .andExpect(status().isCreated());

        return loginAndGetToken(email, "password123");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        return json(result).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private JsonNode findById(JsonNode array, long id) {
        for (JsonNode node : array) {
            if (node.get("id").asLong() == id) {
                return node;
            }
        }
        throw new IllegalStateException("Element not found by id=" + id);
    }

    private String json(Object payload) throws Exception {
        return objectMapper.writeValueAsString(payload);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
