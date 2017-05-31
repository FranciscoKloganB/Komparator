package org.komparator.mediator.ws;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import java.lang.StringBuffer;
import java.lang.Thread;
import java.lang.InterruptedException;

import org.komparator.supplier.ws.*;
import org.komparator.supplier.ws.cli.*;

import org.komparator.mediator.ws.*;
import org.komparator.mediator.domain.*;
import org.komparator.mediator.ws.cli.*;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import org.komparator.security.domain.SecurityManager;
import org.komparator.security.handler.RequestIdMediatorHandler;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceContext;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;

@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType",
		wsdlLocation = "mediator.wsdl",
		name = "MediatorWebService",
		portName = "MediatorPort",
		targetNamespace = "http://ws.mediator.komparator.org/",
		serviceName = "MediatorService"
)
@HandlerChain(file = "/mediator-ws_handler-chain.xml")
public class MediatorPortImpl implements MediatorPortType {
	private MediatorEndpointManager endpointManager;
	private Mediator mediator;
	private String wsName;
	private Boolean trueBool = Boolean.valueOf(true);
	private Boolean falseBool = Boolean.valueOf(false);
	private final String uddiURL = "http://T64:dnBqZpmC@uddi.sd.rnl.tecnico.ulisboa.pt:9090/";

	@Resource
	private WebServiceContext webServiceContext;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		/** sets up endpoint */
		this.endpointManager = endpointManager;
		/** gets singleton instance of Mediator class object */
		this.mediator = Mediator.getInstance();
		/**
		* This wsName is the one used for WebServices search
		* Remember this is not this mediator's port wsName
		*/
		this.wsName = "T64_Supplier%";
	}

	// Main operations -------------------------------------------------------

	/**
	* Returns list of Items, sorted by Price, of a productId from all
	* suppliers who have such product
	*/
	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		/** Initializing all needed objects for this method execution */
		SupplierClient supplier = null;
		String organizationURL = null;
		String organizationName = null;
		ProductView productView = null;
		ItemId itemId = null;
		Item item = null;
		ItemView itemView = null;

		ArrayList<ItemView> itemsList = new ArrayList<ItemView>();

		if (productId == null) {
			throwInvalidItemId_Exception("Product identifaction can't be null!");
			return itemsList;
		} else if (productId.trim().isEmpty()) {
			throwInvalidItemId_Exception("Product identifaction can't be empty or whitespace!");
			return itemsList;
		}

		UDDINaming uddiNamingObj = endpointManager.getUddiNaming();
		Vector<UDDIRecord> organizationList = (Vector)mediator.getWsUDDIRecords(uddiNamingObj, wsName);

		/**
		* Runs through the list of organizations returned by getWsUDDIRecords
		* in order to create SupplierClient instances that allow us to get
		* ProductView items that are then used to create ItemViews which belong
		* to the list that is going to be returned.
		*/
		for (UDDIRecord organization : organizationList) {

			organizationURL = organization.getUrl();
			organizationName = organization.getOrgName();

			if (organizationName == null) {
				throwInvalidItemId_Exception("Organization name was null!");
				continue;
			}

			try {
				supplier = mediator.newSupplierClient(organizationName, uddiURL);
				productView = supplier.getProduct(productId);
				itemId = new ItemId(productId, organizationName);
				item = new Item(itemId, productView.getDesc(), productView.getPrice());
				itemView = newItemView(item);
				itemsList.add(itemView);
			} catch (BadProductId_Exception bPid) {
				throwInvalidItemId_Exception("Exception caused by BadProductId_Exception with message: " + bPid.getMessage());
				break;
			} catch (NullPointerException nPE) {
				continue;
			} catch (SupplierClientException sCE) {
				continue;
			}

		}

		/** Using annonymous inner class for sorting by price on ItemView list */
		Collections.sort(itemsList, new Comparator<ItemView>() {
      public int compare(ItemView a, ItemView b) {
        return Integer.compare(a.getPrice(), b.getPrice());
      }
    });

		return itemsList;
	}

	/**
	* Runs through the list of organizations returned by getWsUDDIRecords
	* in order to create SupplierClient instances that allow us to get
	* Lists of ProductView items that are then iterated to create ItemViews
	* which belong to the list that is going to be returned.
	*/
	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		/** Initializing all needed objects for this method execution */
		SupplierClient supplier = null;
		ItemId itemId = null;
		Item item = null;
		ItemView itemView = null;
		List<ProductView> productViewList = null;

		ArrayList<ItemView> foundItems = new ArrayList<ItemView>();

		if (descText == null) {
			throwInvalidText_Exception("Description text can't be null!");
			return foundItems;
		} else if (descText.trim().isEmpty()) {
			throwInvalidText_Exception("Description text can't be empty or whitespace!");
			return foundItems;
		}

		UDDINaming uddiNamingObj = endpointManager.getUddiNaming();
		Vector<UDDIRecord> organizationList = (Vector)mediator.getWsUDDIRecords(uddiNamingObj, wsName);

		for (UDDIRecord organization : organizationList) {
			try {
				supplier = mediator.newSupplierClient(organization.getOrgName(), uddiURL);
				productViewList = supplier.searchProducts(descText);
				// productViewList is never null after searchProducts. Worse case is empty.
				for (ProductView productView : productViewList) {
					itemId = new ItemId(productView.getId(), organization.getOrgName());
					item = new Item(itemId, productView.getDesc(), productView.getPrice());
					itemView = newItemView(item);
					foundItems.add(itemView);
				}
			} catch (BadText_Exception bTE) {
				throwInvalidText_Exception(bTE.getMessage());
				break;
			} catch (SupplierClientException sCE) {
				continue;
			}
		}

		/** Using annonymous inner class for sorting using two flags */
		Collections.sort(foundItems, new Comparator<ItemView>() {
			public int compare(ItemView a, ItemView b) {
				int productIdResult;
				int priceResult;
				// Get the productId of each ItemView passed as argument.
				String aS = a.getItemId().getProductId();
				String bS = b.getItemId().getProductId();
				// If the productId of both itemViews is different sort by name.
				if ((productIdResult = aS.compareTo(bS)) != 0) {
					return productIdResult;
				// Otherwise sort using their prices.
				} else {
					return Integer.compare(a.getPrice(), b.getPrice());
				}
			}
		});

		return foundItems;
	}

	@Override
  public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		String requestId = getRequestId();

		if (cartId == null) {
			throwInvalidCartId_Exception("Cart identification can't be null!");
			return;
		} else if (cartId.trim().isEmpty()) {
			throwInvalidCartId_Exception("Cart identification can't be empty or whitespace!");
			return;
		}

		if (itemId == null) {
			throwInvalidItemId_Exception("Item identifaction can't be null!");
			return;
		}

		String productId = itemId.getProductId();
		String supplierId = itemId.getSupplierId();

		if (productId == null || supplierId == null) {
			throwInvalidItemId_Exception("Item identification fields (productId or supplierId) can't be null!");
			return;
		} else if (productId.trim().isEmpty() || supplierId.trim().isEmpty()) {
			throwInvalidItemId_Exception("Item identification fields (productId or supplierId) can't be whitespace or empty!");
			return;
		}

		if (itemQty <= 0) {
			throwInvalidQuantity_Exception("The quantity of item you want to add to cart must be a positive integer!");
			return;
		}

		int price = 0;

		String descText = null;
		String wsURL = null;
		SupplierClient supplier = null;
		ProductView productView = null;
		Boolean previousAnswer;

		UDDINaming uddiNamingObj = endpointManager.getUddiNaming();

		try {
			 wsURL = uddiNamingObj.lookup(supplierId);
			 supplier = mediator.newSupplierClient(supplierId, uddiURL);
			 productView = supplier.getProduct(productId);

			 if (productView == null) {
					throwInvalidItemId_Exception("Given supplier does not have such item!");
					return;
			 }

			 int stock = productView.getQuantity();
			 int quantityInCart = mediator.getItemQuantityInCart(cartId, productId, supplierId);

			 if (stock < itemQty || stock < quantityInCart + itemQty) {
				 throwNotEnoughItems_Exception("Quantity of items to buy was higher then available stock!");
			 }

			 descText = productView.getDesc();
			 price = productView.getPrice();

			 previousAnswer = mediator.getAddToCartAnswer(requestId);
			 if (previousAnswer != null) {
				 System.out.println("Add to cart request canceled. An operation with same ID as already been executed..." );
	 			 System.out.println("Repeated add to cart requestId: " + requestId);
				 while (!previousAnswer.booleanValue()) {
					 previousAnswer = mediator.getAddToCartAnswer(requestId);
				 }
				 return;
			 } else {
				 System.out.println("Add to cart request execution started. Setting operation as answered and incomplete...");
				 mediator.setAddToCartAnswer(requestId, falseBool);
				 mediator.addCart(cartId, itemId, itemQty, descText, price);
				 mediator.setAddToCartAnswer(requestId, trueBool);
				 System.out.println("Add to cart request execution finished. Setting operation as answered and complete...");
			 }

		} catch (BadProductId_Exception bPId) {
			throwInvalidItemId_Exception(bPId.getMessage());
			return;
		} catch (UDDINamingException uNE) {;
			return;
		} catch (SupplierClientException sCE) {
			return;
		}

		if (endpointManager.getMediatorStatus()) {
			MediatorClient smClient = endpointManager.getSecondaryMediatorClient();
			if (smClient != null) {
				smClient.replicateAddToCart(cartId, itemId, itemQty, descText, price, requestId);
			}
		}

	}

	@Override
  public ShoppingResultView buyCart(String cartId, String creditCardNr) throws InvalidCartId_Exception, EmptyCart_Exception, InvalidCreditCard_Exception {
		String requestId = getRequestId();
		ShoppingResult shoppingResult = null;
		ShoppingResultView shoppingResultView = new ShoppingResultView();
		ShoppingResultView previousAnswer;

		if (cartId == null) {
			throwInvalidCartId_Exception("Cart identification can't be null!");
			return shoppingResultView;
		} else if (cartId.trim().isEmpty()) {
				throwInvalidCartId_Exception("Cart identification can't be empty or whitespace!");
			return shoppingResultView;
		} else if (!mediator.cartExists(cartId)) {
				throwInvalidCartId_Exception("Cart you tryed to buy does not exist!");
				return shoppingResultView;
		} else if (!mediator.cartIsEmpty(cartId)) {
				throwEmptyCart_Exception("The cart you tryed to buy is empty! Please add items to cart and try again!");
				return shoppingResultView;
		}

		if (creditCardNr == null) {
			throwInvalidCartId_Exception("Cart identification can't be null!");
			return shoppingResultView;
		} else if (cartId.trim().isEmpty()) {
			throwInvalidCartId_Exception("Cart identification can't be empty or whitespace!");
			return shoppingResultView;
		}

		previousAnswer = mediator.getBuyCartAnswer(requestId);
		if (previousAnswer != null) {
			System.out.println("Buy cart request canceled. An operation with same ID as already been executed..." );
			System.out.println("Repeated buy cart requestId: " + requestId);
			System.out.println("Dispatching previous answer...");
			return previousAnswer;
		} else {
			System.out.println("Buy cart request execution started. Setting operation as answered. Not yet incomplete...");
			mediator.setBuyCartAnswer(requestId, shoppingResultView);
			try {
				shoppingResult = mediator.buyCart(cartId, creditCardNr);
			} catch(CreditCardClientException ccCE) {
				throwInvalidCreditCard_Exception(ccCE.getMessage());
				return shoppingResultView;
			} catch (SupplierClientException sC) {
				return shoppingResultView;
			}
			shoppingResultView = newShoppingResultView(shoppingResult);
			System.out.println("Buy cart request execution finished.");
			mediator.setBuyCartAnswer(requestId, shoppingResultView);

			if (endpointManager.getMediatorStatus()) {
				MediatorClient smClient = endpointManager.getSecondaryMediatorClient();
				if (smClient != null) {
					smClient.replicateBuyCart(shoppingResultView, requestId);
				}
			}
		}

		return shoppingResultView;
	}

	@Override
    public List<CartView> listCarts() {
		ArrayList<Cart> cartsList = (ArrayList<Cart>)mediator.getAllCarts();
		List<CartView> cartViewList = new ArrayList<CartView>();
		for (Cart cart : cartsList)
			cartViewList.add(newCartView(cart));
		return cartViewList;
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		ArrayList<ShoppingResultView> shopHistoryView = new ArrayList<ShoppingResultView>();
		Vector<ShoppingResult> shopHistory = (Vector)mediator.getShopHistory();
		for (ShoppingResult shoppingResult : shopHistory) {
			shopHistoryView.add(newShoppingResultView(shoppingResult));
		}
		Collections.reverse(shopHistoryView);
		return shopHistoryView;
	}

	// Auxiliary operations --------------------------------------------------

	/**
	* Attempts to reset the state of each SupplierService of each organization
	* found with getWsUDDIRecords;
	*/
	@Override
	public void clear() {
		String requestId = getRequestId();
		Boolean previousAnswer = mediator.getClearAnswer(requestId);
		if (previousAnswer != null) {
			System.out.println("Clear request canceled. An operation with same ID as already been executed..." );
			System.out.println("Repeated clear requestId: " + requestId);
			while (!previousAnswer.booleanValue()) {
				previousAnswer = mediator.getClearAnswer(requestId);
			}
			return;
		} else {
				System.out.println("Clear request execution started. Setting operation as answered and incomplete...");
				mediator.setClearAnswer(requestId, Boolean.valueOf(false));
				UDDINaming uddiNamingObj = endpointManager.getUddiNaming();
				Vector<UDDIRecord> organizationList = (Vector)mediator.getWsUDDIRecords(uddiNamingObj, wsName);

				if (organizationList.isEmpty()) {
					return;
				}

				for (UDDIRecord organization : organizationList){
					try {
						String organizationURL = organization.getUrl();
						SupplierClient supplierClient = mediator.newSupplierClient(organization.getOrgName(), uddiURL);
						supplierClient.clear();
					} catch (Exception e) {
						return;
					}
				}

				mediator.reset();
				System.out.println("Clear request execution finished. Setting operation as answered and complete...");
				mediator.setClearAnswer(requestId, Boolean.valueOf(true));

				if (endpointManager.getMediatorStatus()) {
					MediatorClient smClient = endpointManager.getSecondaryMediatorClient();
					// Extra layer of protection, usefull for example when secondary mediator becomes primary.
					if (smClient != null) {
						smClient.replicateClear(requestId);
					}
				}
			}
	}

	/**
	* Check if Mediator is live and what Suppliers are running, if any is found.
	*/
	@Override
	public String ping(String name) {
		StringBuffer buffer = new StringBuffer();
		UDDINaming uddiNamingObj = endpointManager.getUddiNaming();
		Vector<UDDIRecord> organizationList = (Vector)mediator.getWsUDDIRecords(uddiNamingObj, wsName);

		if (organizationList.isEmpty()) {
			return "No suppliers were found";
		}

		else {
			String organizationName = null;
			for (UDDIRecord organization : organizationList) {
				try {
					organizationName = organization.getOrgName();
					String organizationURL = organization.getUrl();
					SupplierClient supplierClient = mediator.newSupplierClient(organizationName, uddiURL);
					buffer.append(supplierClient.ping(endpointManager.getWsName()));
					buffer.append(System.lineSeparator());
				} catch (Exception e) {
					buffer.append(organizationName + " is not avaialable or did not answer.");
					buffer.append(System.lineSeparator());
				}
			}
		}
		return buffer.toString();
	}

	/**
	* If the mediator invoking imAlive method is primary nothing happens.
	* Otherwise, if the Mediator whos running this method is not primary, it will
	* store the time instant of when the method was invoked. That instant represents
	* the last time the primary mediator sent a proof that it is still up and running.
	* That instant is stored in the endpoint manager using the respective set method.
	*/
	@Override
	public void imAlive() {
		if (endpointManager.getMediatorStatus()) {
			return;
		} else {
			SecurityManager secM = SecurityManager.getInstance();
			endpointManager.setlastLifeProof(secM.generateTimeStamp());
		}
	}

	/**
	* Replicates a clear invocation on secondary mediator. Notice that this is an
	* update only, therefore it is not requered to invoke clear upon suppliers,
	* because if anything goes wrong during supplier resetting, the caught exception
	* and the remaking of the request, will ensure that the suppliers are resetted
	* anyway.
	*/
	@Override
	public void replicateClear(String requestId) {
		System.out.println("Recieved clear replication request. Initiating...");
		mediator.replicateClear(requestId);
	}

	/**
	* replicateBuyCart imitates the behaviour of buyCart WS method, except it
	* does not check for errors. It merelly merelly makes the secondary mediator
	* similar in state to the primary mediator.
	*/
	@Override
	public void replicateAddToCart(String cartId, ItemIdView itemId, int itemQty, String descText, int price, String requestId) {
		System.out.println("Recieved add to cart replication request. Initiating...");
		mediator.replicateAddToCart(cartId, itemId, itemQty, descText, price, requestId);
	}

	/**
	* replicateBuyCart imitates the behaviour of buyCart WS method, except it
	* does not alter critical states on suppliers, does not generate any objects,
	* does not check for errors. It merelly merelly makes the secondary mediator
	* similar in state to the primary mediator.
	*/
	@Override
	public void replicateBuyCart(ShoppingResultView shopResult, String requestId) {
		System.out.println("Recieved buy cart replication request. Initiating...");
		mediator.replicateBuyCart(shopResult, requestId);
	}

	// Other helpers ---------------------------------------------------------

	private String getRequestId() {
		MessageContext messageContext = webServiceContext.getMessageContext();
		return (String)messageContext.get(RequestIdMediatorHandler.REQUEST_ID);
	}

	// View helpers ----------------------------------------------------------
	private ShoppingResultView newShoppingResultView(ShoppingResult shoppingResult) {
		ShoppingResultView shoppingResultView = new ShoppingResultView();
		List<CartItem> purchasedItems = (ArrayList)shoppingResult.getPurchasedItems();
		List<CartItem> droppedItems = (ArrayList)shoppingResult.getDroppedItems();

		shoppingResultView.setId(shoppingResult.getId());
		shoppingResultView.setResult(shoppingResult.getResult());

		for (CartItem cartItem : purchasedItems) {
			shoppingResultView.getPurchasedItems().add(newCartItemView(cartItem));
		}

		for (CartItem cartItem : droppedItems) {
			shoppingResultView.getDroppedItems().add(newCartItemView(cartItem));
		}

		shoppingResultView.setTotalPrice(shoppingResult.getTotalPrice());

		return shoppingResultView;
	}

	private ItemView newItemView(Item item) {
		ItemIdView itemIdView = newItemIdView(item.getItemId());
		ItemView itemView = new ItemView();
		itemView.setItemId(itemIdView);
		itemView.setDesc(item.getDescription());
		itemView.setPrice(item.getPrice());
		return itemView;
	}

	private ItemIdView newItemIdView(ItemId itemId) {
		ItemIdView itemIdView = new ItemIdView();
		itemIdView.setProductId(itemId.getProductId());
		itemIdView.setSupplierId(itemId.getSupplierId());
		return itemIdView;
	}

	private CartView newCartView(Cart cart) {
		Vector<CartItem> listCartItems = (Vector)cart.getCartItems();
		CartView cartView = new CartView();

		cartView.setCartId(cart.getCartId());
		for (CartItem cartItem : listCartItems) {
			cartView.getItems().add(newCartItemView(cartItem));
		}
		return cartView;
	}

	private CartItemView newCartItemView(CartItem cartItem) {
		ItemView itemView = newItemView(cartItem.getItem());
		CartItemView cartItemView = new CartItemView();
		cartItemView.setItem(itemView);
		cartItemView.setQuantity(cartItem.getQuantity());
		return cartItemView;
	}

	// Exception helpers -----------------------------------------------------

	/** Helper method to throw new InvalidCartId exception */
	private void throwInvalidCartId_Exception (final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.message = message;
		throw new InvalidCartId_Exception(message, faultInfo);
	}

	/** Helper method to throw new BadProductId exception */
	private void throwInvalidQuantity_Exception(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.message = message;
		throw new InvalidQuantity_Exception(message, faultInfo);
	}

	/** Helper method to throw new InvalidText exception */
	private void throwInvalidText_Exception(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.message = message;
		throw new InvalidText_Exception(message, faultInfo);
	}

	/** Helper method to throw new InvalidItemId exception */
	private void  throwInvalidItemId_Exception(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.message = message;
		throw new InvalidItemId_Exception(message, faultInfo);
	}

	/** Helper method to throw new NotEnoughItems exception */
	private void throwNotEnoughItems_Exception(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.message = message;
		throw new NotEnoughItems_Exception(message, faultInfo);
	}

	/** Helper method to throw new InvalidCreditCard exception */
	private void throwInvalidCreditCard_Exception(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.message = message;
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}

	/** Helper method to throw new EmptyCart exception */
	private void throwEmptyCart_Exception(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.message = message;
		throw new EmptyCart_Exception(message, faultInfo);
	}

}
