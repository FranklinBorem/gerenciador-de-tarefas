package com.taskapi.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        register("duplicado@email.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("duplicado@email.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        register("login@email.com");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@email.com\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsListsAndDeletesTasks() throws Exception {
        String token = register("fluxo@email.com");

        String created = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson("Estudar Spring")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/tasks").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(patch("/tasks/" + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(patch("/tasks/" + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDING\"}"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(delete("/tasks/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotSeeTaskFromAnotherUser() throws Exception {
        String tokenAna = register("ana.dona@email.com");
        String tokenBruno = register("bruno.intruso@email.com");

        String created = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson("Tarefa da Ana")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/tasks/" + id).header("Authorization", "Bearer " + tokenBruno))
                .andExpect(status().isNotFound());
    }

    @Test
    void blankTitleReturnsValidationError() throws Exception {
        String token = register("validacao@email.com");

        mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }


    // ---------- ajudantes ----------

    private String register(String email) throws Exception {
        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String userJson(String email) {
        return "{\"name\":\"Teste\",\"email\":\"" + email + "\",\"password\":\"123456\"}";
    }

    private String taskJson(String title) {
        return "{\"title\":\"" + title + "\",\"description\":\"Descrição\",\"dueDate\":\""
                + LocalDate.now().plusDays(3) + "\"}";
    }
}
