package org.komparator.mediator.domain;

import org.komparator.mediator.domain.ItemId;

public class Item {
	private ItemId itemId;
	private String description;
	private int price;

	public Item() {}
		
	/** Create a new Item - Item is also known as a modified product */
	public Item(ItemId itemId, String description, int price) {
		this.itemId = itemId;
		this.description = description;
		this.price = price;
	}

	public ItemId getItemId() {
		return itemId;
	}

	public String getProductId() {
		return itemId.getProductId();
	}

	public String getSupplierId() {
		return itemId.getSupplierId();
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return price;
	}

	public void setItemId (ItemId itemId) {
		this.itemId = itemId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Product [ProductId=").append(getProductId());
		builder.append(", supplierId=").append(getSupplierId());
		builder.append(", description=").append(description);
		builder.append(", price=").append(price);
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

		Item other = (Item) obj;

		if (this.price != other.price)
			return false;

		if (this.description == null) {
			if (other.description != null)
				return false;
		} else if (!this.description.equals(other.description)) {
			return false;
		}

		if (this.itemId == null) {
			if (other.itemId != null)
				return false;
		} else if (!this.itemId.equals(other.itemId)) {
			return false;
		}

		return true;
	}

}
