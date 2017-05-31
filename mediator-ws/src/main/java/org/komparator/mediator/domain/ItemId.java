package org.komparator.mediator.domain;

import org.komparator.mediator.domain.*;

public class ItemId {
	private String productId;
	private String supplierId;

	public ItemId() {}
		
	/** Create a new Item - Item is also known as a modified product */
	public ItemId(String productId, String supplierId) {
		this.productId = productId;
		this.supplierId = supplierId;
	}

	public String getProductId() {
		return productId;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ItemId [ProductId=").append(productId);
		builder.append(", supplierId=").append(supplierId);
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

		ItemId other = (ItemId) obj;

		if (this.productId == null) {
			if (other.productId != null)
				return false;
		} else if (!this.productId.equals(other.productId)) {
			return false;
		}

		if (this.supplierId == null) {
			if (other.supplierId != null)
				return false;
		} else if (!this.supplierId.equals(other.supplierId)) {
			return false;
		}

		return true;
	}
}
