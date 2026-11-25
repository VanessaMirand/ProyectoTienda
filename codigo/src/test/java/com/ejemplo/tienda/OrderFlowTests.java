package com.ejemplo.tienda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end flow through HTTP only. It lives outside both modules, so it can
 * only use what any client can use: the public endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowTests {

    @Autowired
    MockMvc mvc;

    @Test
    void anOrderTakesItsUnitsOutOfTheCatalogStock() throws Exception {
        long productId = createProduct("Teclado mecánico", "50000", 10);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(productId, 3)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(150000))
                .andExpect(jsonPath("$.lines[0].productName").value("Teclado mecánico"));

        mvc.perform(get("/api/products/" + productId))
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void aRejectedOrderIsSavedWithItsIdAndDoesNotTakeStock() throws Exception {
        long scarce = createProduct("Casi agotado", "10000", 1);

        // Pide 5 y solo hay 1: RN-3 -> RECHAZADO, total 0, pero se guarda (201 con id)
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(scarce, 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("RECHAZADO"))
                .andExpect(jsonPath("$.total").value(0));

        // El rechazado no consumió stock: la unidad sigue ahí
        mvc.perform(get("/api/products/" + scarce))
                .andExpect(jsonPath("$.stock").value(1));

        // Y esa unidad todavía se puede comprar
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(scarce, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void anOrderForAnUnknownProductReturns404() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(999_999L, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Producto no encontrado"));
    }

    @Test
    void anInvalidOrderReturns400() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(1L, 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anExistingOrderCanBeReadBackWithItsLines() throws Exception {
        long productId = createProduct("Monitor", "200000", 4);
        String created = mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(productId, 2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long orderId = ((Number) JsonPath.read(created, "$.id")).longValue();

        mvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.lines[0].productName").value("Monitor"))
                .andExpect(jsonPath("$.total").value(400000));
    }

    @Test
    void aProductWithOrdersCannotBeDeleted() throws Exception {
        long productId = createProduct("Webcam", "60000", 5);
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(order(productId, 1)))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Producto en uso"));

        mvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk());
    }

    @Test
    void aProductWithoutOrdersCanBeDeleted() throws Exception {
        long productId = createProduct("Cable HDMI", "9900", 5);

        mvc.perform(delete("/api/products/" + productId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/products/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void anUnknownOrderReturns404() throws Exception {
        mvc.perform(get("/api/orders/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Pedido no encontrado"));
    }

    private static String order(long productId, int quantity) {
        return "{\"clienteId\":\"C-001\",\"productId\":" + productId
                + ",\"quantity\":" + quantity + "}";
    }

    private long createProduct(String name, String price, int stock) throws Exception {
        String body = mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"d\","
                                + "\"price\":" + price + ",\"stock\":" + stock
                                + ",\"category\":\"electronica\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}