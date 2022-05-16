package siit.model;

import java.math.BigDecimal;

public class OrderProduct {

    private long orderId;
    private Product product;
    private int quantity;
    private BigDecimal value;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getValue() {
       return this.product.getPrice().multiply(BigDecimal.valueOf(this.quantity));
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "OrderProduct{" +
                "orderId=" + orderId +
                ", product=" + product +
                ", quantity=" + quantity +
                ", value=" + value +
                '}';
    }
}
