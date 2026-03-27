package com.traceviewcore.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.traceviewcore.bootstrap.TraceViewCoreApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TraceViewCoreApplication.class)
@AutoConfigureMockMvc
class AnalysisApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    @Test
    void runsAnalysisWithAStoreLegacyAdapter() throws Exception {
        Path sampleRoot = tempDir.resolve("astore-sample");
        Files.createDirectories(sampleRoot);
        Files.writeString(sampleRoot.resolve("ManagementController.java"), """
                @Controller
                @RequestMapping("/content/transfer")
                class ManagementController extends BaseController {

                    @Autowired
                    private ContentService contentService;

                    @RequestMapping(value = "/getManagementList.as", method = RequestMethod.GET)
                    public Map<String, Object> getManagementList() {
                        return contentService.getManagementList();
                    }
                }
                """);
        Files.writeString(sampleRoot.resolve("ContentServiceImpl.java"), """
                class ContentServiceImpl implements ContentService {

                    private ContentTransferDAO contentTransferDAO;

                    public Map<String, Object> getManagementList() {
                        contentTransferDAO.findAll();
                        return new HashMap<>();
                    }
                }
                """);
        Files.writeString(sampleRoot.resolve("ContentTransferDAOImpl.java"), """
                class ContentTransferDAOImpl extends SqlMapClientDaoSupport implements ContentTransferDAO {
                    public List<String> findAll() {
                        return new ArrayList<>();
                    }
                }
                """);

        mockMvc.perform(post("/api/analysis/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rootPath":"%s","adapterId":"astore-legacy"}
                                """.formatted(sampleRoot.toString().replace("\\", "\\\\"))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.snapshotId", notNullValue()))
                .andExpect(jsonPath("$.adapterId", is("astore-legacy")))
                .andExpect(jsonPath("$.nodeCount", greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.relationCount", greaterThanOrEqualTo(3)));
    }

    @Test
    void returnsBadRequestForUnknownAdapter() throws Exception {
        mockMvc.perform(post("/api/analysis/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rootPath":"%s","adapterId":"missing-adapter"}
                                """.formatted(tempDir.toString().replace("\\", "\\\\"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Analysis adapter not found")));
    }
}
