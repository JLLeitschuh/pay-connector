package uk.gov.pay.connector.model;

import java.util.Objects;

public class GatewayStatusOnly<T> implements StatusMapFromStatus<T> {

    private final T status;

    public static <T> GatewayStatusOnly<T> of(T status) {
        return new GatewayStatusOnly<>(status);
    }

    private GatewayStatusOnly(T status) {
        this.status = Objects.requireNonNull(status);
    }

    @Override
    public T getGatewayStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof StatusMapFromStatus<?>)) {
            return false;
        }

        StatusMapFromStatus<?> that = (StatusMapFromStatus<?>) other;
        return this.getGatewayStatus().equals(that.getGatewayStatus());
    }

    @Override
    public int hashCode() {
        return 31 * status.hashCode();
    }

    @Override
    public String toString() {
        return "GatewayStatusOnly{status=" + status + '}';
    }

}
