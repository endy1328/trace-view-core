package com.traceviewcore.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpringSourceParserTest {

    private final SpringSourceParser parser = new SpringSourceParser();

    @Test
    void parsesControllerAndMappingMethod() {
        String content = """
                @RestController
                @RequestMapping("/orders")
                class OrderController {

                    private final OrderService orderService;

                    @GetMapping("/{id}")
                    public OrderResponse findOrder() {
                        return orderService.findOrder();
                    }
                }
                """;

        SpringSourceParser.ParsedClass parsedClass = parser.parse(new SourceDocument("OrderController.java", content));

        assertEquals("OrderController", parsedClass.typeName());
        assertTrue(parsedClass.hasAnnotation("RestController"));
        assertEquals(1, parsedClass.methods().size());
        assertEquals("findOrder", parsedClass.methods().get(0).methodName());
    }
}
