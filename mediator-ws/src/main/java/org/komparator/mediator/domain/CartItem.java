package org.komparator.mediator.domain;

import org.komparator.mediator.domain.*;
import org.komparator.mediator.ws.InvalidQuantity_Exception;

public class CartItem {
    /** Item has productId and supplierId */
    private Item item;
    /** Synchronized quanity */
    private volatile int quantity;

    public CartItem() {}

    public CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public ItemId getItemId() {
        return item.getItemId();
    }

    public String getProductId() {
        return item.getItemId().getProductId();
    }

    public String getSupplierId() {
        return item.getItemId().getSupplierId();
    }

    public int getPrice() {
        return item.getPrice();
    }

    public synchronized int getQuantity() {
        return quantity;
    }

    /** Gets quantity in synchronized fashion and multiplies by price */
    public int getCartItemPrice() {
        int result = quantity * item.getPrice();
        return result;
    }

    public void setItem(Item item) {
      this.item = item;
    }

    /** Synchronized locks object before setting new quantity */
    public synchronized void setQuantity(int quantity) {
        if (quantity < 0)
            return;
        this.quantity = quantity;
    }

    public boolean productMatch(String productId, String supplierId) {
      if (productId.equals(getProductId()) && supplierId.equals(getSupplierId())) {
        return true;
      }
      return false;
    }

	/** Synchronized locks object before increasing quantity */
    public synchronized void increaseQuantity(int quantity) {
        if (quantity <= 0)
            return;
        this.quantity += quantity;
    }

	/** Synchronized locks object before decreasing quantity */
    public synchronized void decreaseQuantity(int quantity) {
        if (this.quantity - quantity < 0)
            return;
        this.quantity -= quantity;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CartItem [productId=").append(getProductId());
        builder.append(", supplierId=").append(getSupplierId());
        builder.append(", itemPrice=").append(getPrice());
        builder.append(", quantity=").append(quantity);
        builder.append(", cartItemPrice=").append(getCartItemPrice());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        CartItem other = (CartItem) obj;

        if (this.quantity != other.quantity)
            return false;

        if (this.item == null) {
            if (other.item != null)
                return false;
        } else if (!this.item.equals(other.item))
            return false;

        return true;
    }
}
