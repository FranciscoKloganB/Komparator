package org.komparator.mediator.domain;

import org.komparator.mediator.domain.*;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;

import java.util.List;
import java.util.Vector;

public class Cart {
	private String cartId;
	private List<CartItem> items = new Vector<CartItem>();

    /** Create a new Item - Item is also known as a modified product */
    public Cart(String cartId) {
        this.cartId = cartId;
    }

	public String getCartId() {
		return cartId;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

  public List<CartItem> getCartItems() {
      return items;
  }

	public int getItemQuantityInCart(String productId, String supplierId) {
		int quantity = 0;
		for (CartItem cartItem : items) {
			if (cartItem.productMatch(productId, supplierId)) {
				quantity = cartItem.getQuantity();
				return quantity;
			}
		}
		return quantity;
	}
  /**
  * Iterate though the vector until we find the object in the list of CartItems that
  * is equals to cAux, incrementing the CartItem quantity and returning.
  * If no equality is found we append cAux to the vector just as it was created.
  */
  public void addCart(ItemIdView itemIdView, int quantity, String descText, int price) {
      String productId = itemIdView.getProductId();
      String supplierId = itemIdView.getSupplierId();

      ItemId itemId = new ItemId(productId, supplierId);
      Item item = new Item(itemId, descText, price);

      CartItem cAux = new CartItem(item, quantity);

      for (CartItem cartItem : items) {
          if (cartItem.equals(cAux)) {
              cartItem.increaseQuantity(quantity);
              return;
          }
      }

      items.add(cAux);
  }

  @Override
  public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Cart [CartId=").append(cartId);
      builder.append(", items=");
      for (CartItem item : items) {
          builder.append(item.toString());
      }
      return builder.toString();
  }
}
