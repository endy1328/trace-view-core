package com.traceviewcore.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.traceviewcore.bootstrap.TraceViewCoreApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TraceViewCoreApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StaticUiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesUiShellAtRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trace View Core")))
                .andExpect(content().string(containsString("/app/main.js")));
    }

    @Test
    void servesStaticUiAssets() throws Exception {
        mockMvc.perform(get("/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("--accent")));

        mockMvc.perform(get("/app/main.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("registerRoute")));
    }

    @Test
    void returnsNoContentWhenNoSnapshotExists() throws Exception {
        mockMvc.perform(get("/api/query/latest"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/query/graph"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnsNotFoundForUnknownReviewTargetsAndAnnotations() throws Exception {
        mockMvc.perform(get("/api/query/nodes/missing_node"))
                .andExpect(status().isConflict());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/reviews/annotations")
                        .contentType("application/json")
                        .content("""
                                {"targetId":"missing_node","content":"invalid","author":"tester"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/reviews/annotations/annotation_missing/approve")
                        .contentType("application/json")
                        .content("""
                                {"approver":"lead"}
                                """))
                .andExpect(status().isNotFound());
    }
}
