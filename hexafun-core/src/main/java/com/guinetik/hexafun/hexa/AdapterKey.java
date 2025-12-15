package com.guinetik.hexafun.hexa;

import java.util.Objects;

/**
 * Type-safe key for adapter registration and invocation.
 * Provides compile-time type checking for transformations between types.
 *
 * <p>Adapters transform data from one type to another, useful for:
 * <ul>
 *   <li>Converting between use case input types</li>
 *   <li>Mapping domain objects to DTOs</li>
 *   <li>Transforming external data to internal representations</li>
 * </ul>
 *
 * <p>Usage:
 * <pre class="language-java">{@code
 * // Define keys as constants
 * public interface OrderAdapters {
 *     AdapterKey<OrderRequest, InventoryCheck> TO_INVENTORY =
 *         AdapterKey.of("orderToInventory");
 *     AdapterKey<OrderRequest, PaymentRequest> TO_PAYMENT =
 *         AdapterKey.of("orderToPayment");
 * }
 *
 * // Register in DSL
 * HexaFun.dsl()
 *     .withAdapter(TO_INVENTORY, req -> new InventoryCheck(req.itemId()))
 *     .withAdapter(TO_PAYMENT, req -> new PaymentRequest(req.total()))
 *     .useCase(...)
 *     .build();
 *
 * // Type-safe adaptation
 * InventoryCheck check = app.adapt(TO_INVENTORY, orderRequest);
 * }</pre>
 *
 * @param <From> The source type to adapt from
 * @param <To> The target type to adapt to
 */
public final class AdapterKey<From, To> {

    private final String name;

    private AdapterKey(String name) {
        this.name = Objects.requireNonNull(
            name,
            "Adapter name cannot be null"
        );
    }

    /**
     * Create a new type-safe adapter key.
     *
     * @param name The unique name for this adapter
     * @param <From> The source type
     * @param <To> The target type
     * @return A new AdapterKey instance
     */
    public static <From, To> AdapterKey<From, To> of(String name) {
        return new AdapterKey<>(name);
    }

    /**
     * Get the string name of this adapter key.
     * @return The adapter name
     */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterKey<?, ?> that = (AdapterKey<?, ?>) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "AdapterKey(" + name + ")";
    }
}
