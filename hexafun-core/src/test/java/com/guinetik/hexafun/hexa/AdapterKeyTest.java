package com.guinetik.hexafun.hexa;

import com.guinetik.hexafun.HexaApp;
import com.guinetik.hexafun.HexaFun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AdapterKey and adapter functionality.
 */
@DisplayName("AdapterKey")
public class AdapterKeyTest {

    // Test records for adapter scenarios
    record OrderRequest(String itemId, int quantity, double total) {}
    record InventoryCheck(String itemId, int quantity) {}
    record PaymentRequest(double amount) {}

    // Test adapter keys
    static final AdapterKey<OrderRequest, InventoryCheck> TO_INVENTORY =
        AdapterKey.of("orderToInventory");
    static final AdapterKey<OrderRequest, PaymentRequest> TO_PAYMENT =
        AdapterKey.of("orderToPayment");
    static final AdapterKey<String, Integer> STRING_TO_LENGTH =
        AdapterKey.of("stringToLength");

    @Nested
    @DisplayName("AdapterKey basics")
    class AdapterKeyBasicsTests {

        @Test
        @DisplayName("should have correct name")
        void shouldHaveCorrectName() {
            AdapterKey<String, Integer> key = AdapterKey.of("myAdapter");
            assertEquals("myAdapter", key.name());
        }

        @Test
        @DisplayName("should be equal when names match")
        void shouldBeEqualWhenNamesMatch() {
            AdapterKey<String, Integer> key1 = AdapterKey.of("test");
            AdapterKey<Double, Boolean> key2 = AdapterKey.of("test");

            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when names differ")
        void shouldNotBeEqualWhenNamesDiffer() {
            AdapterKey<String, Integer> key1 = AdapterKey.of("test1");
            AdapterKey<String, Integer> key2 = AdapterKey.of("test2");

            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("should throw on null name")
        void shouldThrowOnNullName() {
            assertThrows(NullPointerException.class, () -> AdapterKey.of(null));
        }

        @Test
        @DisplayName("toString should include name")
        void toStringShouldIncludeName() {
            AdapterKey<String, Integer> key = AdapterKey.of("myAdapter");
            assertTrue(key.toString().contains("myAdapter"));
        }
    }

    @Nested
    @DisplayName("Direct registration")
    class DirectRegistrationTests {

        @Test
        @DisplayName("should register adapter directly on HexaApp")
        void shouldRegisterAdapterDirectly() {
            HexaApp app = HexaApp.create();
            app.withAdapter(STRING_TO_LENGTH, String::length);

            assertTrue(app.hasAdapter(STRING_TO_LENGTH));
            assertEquals(5, (int) app.adapt(STRING_TO_LENGTH, "hello"));
        }

        @Test
        @DisplayName("should register multiple adapters directly")
        void shouldRegisterMultipleAdaptersDirectly() {
            HexaApp app = HexaApp.create();
            app.withAdapter(TO_INVENTORY,
                req -> new InventoryCheck(req.itemId(), req.quantity()));
            app.withAdapter(TO_PAYMENT,
                req -> new PaymentRequest(req.total()));

            assertTrue(app.hasAdapter(TO_INVENTORY));
            assertTrue(app.hasAdapter(TO_PAYMENT));
            assertEquals(2, app.registeredAdapters().size());
        }

        @Test
        @DisplayName("should chain withAdapter calls")
        void shouldChainWithAdapterCalls() {
            HexaApp app = HexaApp.create()
                .withAdapter(STRING_TO_LENGTH, String::length)
                .withAdapter(TO_INVENTORY,
                    req -> new InventoryCheck(req.itemId(), req.quantity()));

            assertEquals(2, app.registeredAdapters().size());
        }
    }

    @Nested
    @DisplayName("DSL registration")
    class DslRegistrationTests {

        @Test
        @DisplayName("should register adapter via DSL")
        void shouldRegisterAdapterViaDsl() {
            HexaApp app = HexaFun.dsl()
                .withAdapter(STRING_TO_LENGTH, String::length)
                .build();

            assertTrue(app.hasAdapter(STRING_TO_LENGTH));
        }

        @Test
        @DisplayName("should register multiple adapters")
        void shouldRegisterMultipleAdapters() {
            HexaApp app = HexaFun.dsl()
                .withAdapter(TO_INVENTORY,
                    req -> new InventoryCheck(req.itemId(), req.quantity()))
                .withAdapter(TO_PAYMENT,
                    req -> new PaymentRequest(req.total()))
                .build();

            assertTrue(app.hasAdapter(TO_INVENTORY));
            assertTrue(app.hasAdapter(TO_PAYMENT));
            assertEquals(2, app.registeredAdapters().size());
        }

        @Test
        @DisplayName("should list registered adapter names")
        void shouldListRegisteredAdapterNames() {
            HexaApp app = HexaFun.dsl()
                .withAdapter(TO_INVENTORY,
                    req -> new InventoryCheck(req.itemId(), req.quantity()))
                .withAdapter(STRING_TO_LENGTH, String::length)
                .build();

            var adapters = app.registeredAdapters();
            assertEquals(2, adapters.size());
            assertTrue(adapters.contains("orderToInventory"));
            assertTrue(adapters.contains("stringToLength"));
        }
    }

    @Nested
    @DisplayName("adapt() invocation")
    class AdaptInvocationTests {

        @Test
        @DisplayName("should adapt value using registered adapter")
        void shouldAdaptValue() {
            HexaApp app = HexaFun.dsl()
                .withAdapter(STRING_TO_LENGTH, String::length)
                .build();

            Integer result = app.adapt(STRING_TO_LENGTH, "hello");
            assertEquals(5, result);
        }

        @Test
        @DisplayName("should adapt complex types")
        void shouldAdaptComplexTypes() {
            HexaApp app = HexaFun.dsl()
                .withAdapter(TO_INVENTORY,
                    req -> new InventoryCheck(req.itemId(), req.quantity()))
                .build();

            OrderRequest order = new OrderRequest("SKU-123", 5, 99.99);
            InventoryCheck check = app.adapt(TO_INVENTORY, order);

            assertEquals("SKU-123", check.itemId());
            assertEquals(5, check.quantity());
        }

        @Test
        @DisplayName("should throw when adapter not found")
        void shouldThrowWhenAdapterNotFound() {
            AdapterKey<String, Integer> MISSING = AdapterKey.of("missing");
            HexaApp app = HexaFun.dsl().build();

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> app.adapt(MISSING, "test")
            );
            assertTrue(ex.getMessage().contains("missing"));
        }

        @Test
        @DisplayName("should handle null input if adapter allows")
        void shouldHandleNullInput() {
            AdapterKey<String, String> NULL_SAFE = AdapterKey.of("nullSafe");

            HexaApp app = HexaFun.dsl()
                .withAdapter(NULL_SAFE, s -> s == null ? "NULL" : s.toUpperCase())
                .build();

            assertEquals("NULL", app.adapt(NULL_SAFE, null));
            assertEquals("HELLO", app.adapt(NULL_SAFE, "hello"));
        }
    }

    @Nested
    @DisplayName("integration with use cases")
    class IntegrationTests {

        @Test
        @DisplayName("should use adapter in use case composition")
        void shouldUseAdapterInUseCaseComposition() {
            UseCaseKey<OrderRequest, String> PROCESS_ORDER = UseCaseKey.of("processOrder");
            UseCaseKey<InventoryCheck, Boolean> CHECK_STOCK = UseCaseKey.of("checkStock");

            HexaApp app = HexaFun.dsl()
                .withAdapter(TO_INVENTORY,
                    req -> new InventoryCheck(req.itemId(), req.quantity()))
                .useCase(CHECK_STOCK)
                    .handle(check -> check.quantity() <= 10) // In stock if <= 10
                .useCase(PROCESS_ORDER)
                    .handle(order -> "Processed: " + order.itemId())
                .build();

            // Use adapter to transform, then invoke another use case
            OrderRequest order = new OrderRequest("SKU-456", 3, 29.99);
            InventoryCheck check = app.adapt(TO_INVENTORY, order);
            Boolean inStock = app.invoke(CHECK_STOCK, check);

            assertTrue(inStock);
            assertEquals("SKU-456", check.itemId());
            assertEquals(3, check.quantity());
        }

        @Test
        @DisplayName("should chain adapters manually")
        void shouldChainAdaptersManually() {
            AdapterKey<String, Integer> TO_INT = AdapterKey.of("toInt");
            AdapterKey<Integer, String> TO_BINARY = AdapterKey.of("toBinary");

            HexaApp app = HexaFun.dsl()
                .withAdapter(TO_INT, Integer::parseInt)
                .withAdapter(TO_BINARY, Integer::toBinaryString)
                .build();

            // Chain: "42" -> 42 -> "101010"
            Integer num = app.adapt(TO_INT, "42");
            String binary = app.adapt(TO_BINARY, num);

            assertEquals(42, num);
            assertEquals("101010", binary);
        }
    }

    @Nested
    @DisplayName("hasAdapter()")
    class HasAdapterTests {

        @Test
        @DisplayName("should return true for registered adapter")
        void shouldReturnTrueForRegistered() {
            HexaApp app = HexaFun.dsl()
                .withAdapter(STRING_TO_LENGTH, String::length)
                .build();

            assertTrue(app.hasAdapter(STRING_TO_LENGTH));
        }

        @Test
        @DisplayName("should return false for unregistered adapter")
        void shouldReturnFalseForUnregistered() {
            AdapterKey<String, Integer> MISSING = AdapterKey.of("missing");
            HexaApp app = HexaFun.dsl().build();

            assertFalse(app.hasAdapter(MISSING));
        }
    }
}
