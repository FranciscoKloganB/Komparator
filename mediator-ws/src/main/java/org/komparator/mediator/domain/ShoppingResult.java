package org.komparator.mediator.domain;

import org.komparator.mediator.domain.*;
import org.komparator.mediator.ws.*;

import java.util.List;
import java.util.Vector;

public class ShoppingResult {
	private String id;
	private Result result;
  private List<CartItem> purchasedItems;
  private List<CartItem> droppedItems;
	private int totalPrice;

    public ShoppingResult() {
      // empty constructor;
    }

	public ShoppingResult(String id, Result result, List<CartItem> purchasedItems, List<CartItem> droppedItems, int totalPrice) {
		this.id = id;
		this.result = result;
		this.purchasedItems = purchasedItems;
    this.droppedItems = droppedItems;
    this.totalPrice = totalPrice;
	}

	public String getId() {
		return id;
	}

	public Result getResult() {
		return result;
	}

  public List<CartItem> getPurchasedItems() {
    return purchasedItems;
  }

  public List<CartItem> getDroppedItems() {
    return droppedItems;
  }

  public int getTotalPrice() {
    return totalPrice;
  }

	public void setId(String id) {
		this.id = id;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public void setPurchasedItems(List<CartItem> purchasedItems) {
		this.purchasedItems = purchasedItems;
	}

	public void setDroppedItems(List<CartItem> droppedItems) {
		this.droppedItems = droppedItems;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShoppingResult [id=").append(id);
		builder.append(", result=").append(result.value());
        builder.append(", purchasedItems=[");
        for (CartItem cartItem : purchasedItems) {
            builder.append(" " + cartItem.toString() + " ");
        }
		builder.append("], droppedItems=[");
        for (CartItem cartItem : droppedItems) {
            builder.append(" " + cartItem.toString() + " ");
        }
		builder.append("]");
		return builder.toString();
	}
}
