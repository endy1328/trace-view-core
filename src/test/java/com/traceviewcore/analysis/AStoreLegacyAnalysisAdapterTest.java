package com.traceviewcore.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.traceviewcore.analysis.adapter.AStoreLegacyAnalysisAdapter;
import com.traceviewcore.analysis.evidence.EvidenceFactory;
import com.traceviewcore.analysis.external.SpringExternalCallAnalyzer;
import com.traceviewcore.analysis.repository.SpringRepositoryAnalyzer;
import com.traceviewcore.analysis.service.SpringInvocationAnalyzer;
import com.traceviewcore.analysis.service.SpringServiceAnalyzer;
import com.traceviewcore.analysis.springmvc.SpringEndpointAnalyzer;
import com.traceviewcore.domain.AnalysisGraph;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.RelationType;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class AStoreLegacyAnalysisAdapterTest {

    private final SpringSourceParser parser = new SpringSourceParser();
    private final EvidenceFactory evidenceFactory = new EvidenceFactory();

    @Test
    void augmentsLegacySpringProjectUsingAStoreConventions() {
        AnalysisContext context = new AnalysisContext(
                Path.of("/astore"),
                List.of(
                        new SourceDocument("ManagementController.java", """
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
                                """),
                        new SourceDocument("ContentServiceImpl.java", """
                                class ContentServiceImpl implements ContentService {

                                    private ContentTransferDAO contentTransferDAO;
                                    private MemberBiz memberBiz;

                                    public Map<String, Object> getManagementList() {
                                        memberBiz.loadMember();
                                        contentTransferDAO.findAll();
                                        return new HashMap<>();
                                    }
                                }
                                """),
                        new SourceDocument("ContentTransferDAOImpl.java", """
                                class ContentTransferDAOImpl extends SqlMapClientDaoSupport implements ContentTransferDAO {
                                    public List<String> findAll() {
                                        return new ArrayList<>();
                                    }
                                }
                                """),
                        new SourceDocument("MemberBizImpl.java", """
                                class MemberBizImpl implements MemberBiz {
                                    public Member loadMember() {
                                        return new Member();
                                    }
                                }
                                """)
                )
        );

        SpringCoreAnalysisPipeline pipeline = new SpringCoreAnalysisPipeline(List.of(
                new SpringEndpointAnalyzer(parser, evidenceFactory),
                new SpringServiceAnalyzer(parser, evidenceFactory),
                new SpringRepositoryAnalyzer(parser, evidenceFactory),
                new SpringExternalCallAnalyzer(parser, evidenceFactory),
                new SpringInvocationAnalyzer(parser, evidenceFactory)
        ), List.of(
                new AStoreLegacyAnalysisAdapter(parser, evidenceFactory)
        ));

        AnalysisGraph graph = pipeline.analyze(context, "astore-legacy");

        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.API_ENDPOINT && node.name().equals("GET /content/transfer/getManagementList.as")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.SERVICE_METHOD && node.name().equals("ContentService#getManagementList")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.SERVICE_METHOD && node.name().equals("MemberBiz#loadMember")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.REPOSITORY_CALL && node.name().equals("ContentTransferDAO")));
        assertTrue(graph.nodes().stream().noneMatch(node -> node.type() == NodeType.SERVICE_METHOD && node.name().equals("ContentServiceImpl#getManagementList")));
        assertTrue(graph.nodes().stream().noneMatch(node -> node.type() == NodeType.SERVICE_METHOD && node.name().equals("MemberBizImpl#loadMember")));
        assertTrue(graph.nodes().stream().noneMatch(node -> node.type() == NodeType.REPOSITORY_CALL && node.name().equals("ContentTransferDAOImpl")));

        assertEquals(1, countRelations(graph, RelationType.INVOKES, "ManagementController#getManagementList", "ContentService#getManagementList"));
        assertEquals(1, countRelations(graph, RelationType.INVOKES, "ContentService#getManagementList", "MemberBiz#loadMember"));
        assertEquals(1, countRelations(graph, RelationType.ACCESSES, "ContentService#getManagementList", "ContentTransferDAO"));
    }

    private long countRelations(AnalysisGraph graph, RelationType relationType, String fromName, String toName) {
        return graph.relations().stream()
                .filter(relation -> relation.relationType() == relationType)
                .filter(relation -> graph.nodes().stream().anyMatch(node -> node.id().equals(relation.fromId()) && node.name().equals(fromName)))
                .filter(relation -> graph.nodes().stream().anyMatch(node -> node.id().equals(relation.toId()) && node.name().equals(toName)))
                .count();
    }
}
