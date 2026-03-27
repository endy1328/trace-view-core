package com.traceviewcore.api;

import com.traceviewcore.application.GraphResponse;
import com.traceviewcore.application.NodeDetailResponse;
import com.traceviewcore.application.QueryApplicationService;
import com.traceviewcore.application.ServiceChainResponse;
import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.GraphNode;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final QueryApplicationService queryApplicationService;

    public QueryController(QueryApplicationService queryApplicationService) {
        this.queryApplicationService = queryApplicationService;
    }

    @GetMapping("/search")
    public List<GraphNode> search(@RequestParam("q") String query) {
        return queryApplicationService.search(query);
    }

    @GetMapping("/endpoints")
    public List<GraphNode> endpoints() {
        return queryApplicationService.endpoints();
    }

    @GetMapping("/nodes/{id}")
    public NodeDetailResponse node(@PathVariable String id) {
        return queryApplicationService.node(id);
    }

    @GetMapping("/endpoints/{id}/service-chain")
    public ServiceChainResponse serviceChain(@PathVariable String id) {
        return queryApplicationService.serviceChain(id);
    }

    @GetMapping("/latest")
    public ResponseEntity<AnalysisSnapshot> latest() {
        return queryApplicationService.latestOptional()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/graph")
    public ResponseEntity<GraphResponse> graph(
            @RequestParam(value = "nodeId", required = false) String nodeId,
            @RequestParam(value = "type", required = false) String type
    ) {
        if (queryApplicationService.latestOptional().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(queryApplicationService.graph(nodeId, type));
    }
}
