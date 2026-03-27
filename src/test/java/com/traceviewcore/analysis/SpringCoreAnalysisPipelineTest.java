package com.traceviewcore.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class SpringCoreAnalysisPipelineTest {

    private final SpringSourceParser parser = new SpringSourceParser();
    private final EvidenceFactory evidenceFactory = new EvidenceFactory();

    @Test
    void buildsStandardSpringServiceChainGraph() {
        AnalysisContext context = new AnalysisContext(
                Path.of("/sample"),
                List.of(
                        new SourceDocument("OrderController.java", """
                                @RestController
                                @RequestMapping("/orders")
                                class OrderController {

                                    private final OrderService orderService;

                                    @GetMapping("/{id}")
                                    public OrderResponse findOrder() {
                                        return orderService.findOrder();
                                    }
                                }
                                """),
                        new SourceDocument("OrderService.java", """
                                @Service
                                class OrderService {

                                    private final PaymentService paymentService;
                                    private final OrderRepository orderRepository;
                                    private final BillingClient billingClient;

                                    public OrderResponse findOrder() {
                                        paymentService.loadPayment();
                                        orderRepository.findById(1L);
                                        billingClient.requestBilling();
                                        return new OrderResponse();
                                    }
                                }
                                """),
                        new SourceDocument("PaymentService.java", """
                                @Service
                                class PaymentService {

                                    public Payment loadPayment() {
                                        return new Payment();
                                    }
                                }
                                """),
                        new SourceDocument("OrderRepository.java", """
                                interface OrderRepository extends JpaRepository<Order, Long> {
                                }
                                """),
                        new SourceDocument("BillingClient.java", """
                                @FeignClient(name = "billing")
                                interface BillingClient {

                                    @GetMapping("/billing")
                                    BillingResponse requestBilling();
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
        ));

        AnalysisGraph graph = pipeline.analyze(context);

        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.API_ENDPOINT && node.name().equals("GET /orders/{id}")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.BACKEND_ENTRY_POINT && node.name().equals("OrderController#findOrder")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.SERVICE_METHOD && node.name().equals("OrderService#findOrder")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.SERVICE_METHOD && node.name().equals("PaymentService#loadPayment")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.REPOSITORY_CALL && node.name().equals("OrderRepository")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.type() == NodeType.EXTERNAL_CALL && node.name().equals("BillingClient")));

        assertEquals(1, countRelations(graph, RelationType.HANDLED_BY, "GET /orders/{id}", "OrderController#findOrder"));
        assertEquals(1, countRelations(graph, RelationType.INVOKES, "OrderController#findOrder", "OrderService#findOrder"));
        assertEquals(1, countRelations(graph, RelationType.INVOKES, "OrderService#findOrder", "PaymentService#loadPayment"));
        assertEquals(1, countRelations(graph, RelationType.ACCESSES, "OrderService#findOrder", "OrderRepository"));
        assertEquals(1, countRelations(graph, RelationType.CALLS, "OrderService#findOrder", "BillingClient"));
        assertTrue(graph.evidences().size() >= 5);
    }

    private long countRelations(AnalysisGraph graph, RelationType relationType, String fromName, String toName) {
        return graph.relations().stream()
                .filter(relation -> relation.relationType() == relationType)
                .filter(relation -> graph.nodes().stream().anyMatch(node -> node.id().equals(relation.fromId()) && node.name().equals(fromName)))
                .filter(relation -> graph.nodes().stream().anyMatch(node -> node.id().equals(relation.toId()) && node.name().equals(toName)))
                .count();
    }
}
