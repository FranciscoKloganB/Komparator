package org.komparator.mediator.domain;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;
import org.komparator.supplier.ws.*;
import org.komparator.mediator.ws.cli.MediatorClient;

import org.komparator.mediator.ws.*;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;

import java.lang.Boolean;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Domain Root. */
public class Mediator {
  // Members ---------------------------------------------------------------
  private AtomicInteger cartPurchaseIdCounter = new AtomicInteger(0);
  private Map<String, Cart> cartsMap = new ConcurrentHashMap<>();
  private Map<String, ShoppingResultView> buyCartAnswers = new ConcurrentHashMap<>();
  private Map<String, Boolean> addCartAnswers = new ConcurrentHashMap<>();
  private Map<String, Boolean> clearAnswers = new ConcurrentHashMap<>();
  private List<ShoppingResult> shopHistory = new Vector<ShoppingResult>();
  private List<UDDIRecord> wsUDDIRecords = new Vector<UDDIRecord>();
  private List<String> wsURLs = new Vector<String>();
  private Boolean trueBool = Boolean.valueOf(true);
  private Boolean falseBool = Boolean.valueOf(false);

  // Singleton -------------------------------------------------------------
  /* Private constructor prevents instantiation from other classes */
  private Mediator() {}

    /** SingletonHolder for Mediator Class */
    private static class SingletonMediatorHolder {
      private static final Mediator INSTANCE = new Mediator();
    }

    /** Defines access point to client classes that need Mediator methods */
    public static synchronized Mediator getInstance() {
      return SingletonMediatorHolder.INSTANCE;
    }


    // Cart Related methods --------------------------------------------------
    /** Returns all values of carts map. That means all Cart instances in the map */
    public List<Cart> getAllCarts() {
      List<Cart> carts = new ArrayList<Cart>(cartsMap.values());
      return carts;
    }

    /** Verify if the given cart exists */
    public boolean cartExists(String cartId) {
      if (!cartsMap.containsKey(cartId)) {
        return false;
      }
      return true;
    }
    /** Verify is cart exists, then checks if the card list of CartItems is empty */
    public boolean cartIsEmpty(String cartId) {
      if (!cartsMap.containsKey(cartId)) {
        return false;
      } else if (cartsMap.get(cartId).isEmpty()) {
        return false;
      } else {
        return true;
      }
    }

    /** Returns the amount of the given ItemId within the given cart */
    public int getItemQuantityInCart(String cartId, String productId, String supplierId) {
      Cart cart = cartsMap.get(cartId);
      if (cart == null) {
        return 0;
      } else {
        return cart.getItemQuantityInCart(productId, supplierId);
      }
    }

    /**
    * Method updates users Carts.
    * If Cart instance is null then cart doesnt exist yet on this mediators Map,
    * therefore we need to create a new one, place it on vector and prooced as normal.
    * Proceeding as normal means, updating CartItem list in Cart instance according
    * to its own method definition.
    */
    public void addCart(String cartId, ItemIdView itemId, int itemQty, String descText, int price) {
      Cart cart = cartsMap.get(cartId);
      if (cart == null) {
        cart = new Cart(cartId);
        cart.addCart(itemId, itemQty, descText, price);
        cartsMap.put(cartId, cart);
      } else {
        cart.addCart(itemId, itemQty, descText, price);
      }
    }


    // ShoppingResult related methods ----------------------------------------
    /**
    * buyCart method tries to make a purchase of a cart.
    * It first contacts the external server to check credit card credentials.
    * If they are valid it contacts every supplier sequently to make proper purchases.
    * It updates a list of items that were purchased and dropped by the client.
    * It finally returns a result and total price.
    */
    public ShoppingResult buyCart(String cartId, String creditCardNr) throws CreditCardClientException, SupplierClientException {
      Cart cart = null;
      CreditCardClient ccClient = null;
      String cartPurchaseId = null;
      String supplierId = null;
      String productId = null;
      Result result = null;
      ProductView product = null;
      List<CartItem> uncheckedItems;
      List<CartItem> purchasedItems = new ArrayList<CartItem>();
      List<CartItem> droppedItems = new ArrayList<CartItem>();
      ShoppingResult shoppingResult = new ShoppingResult();
      HashMap<String, SupplierClient> suppliersMap = new HashMap<String, SupplierClient>();
      int quantity = 0;
      int totalPrice = 0;
      String ccHostName = "CreditCard";
      String ccHost = "http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc";
      String uddiURL = "http://T64:dnBqZpmC@uddi.sd.rnl.tecnico.ulisboa.pt:9090/";

      boolean validation = false;

      // Try to obtain a remote handler for service invocation.
      try {
        ccClient = new CreditCardClient(ccHost);
      } catch (CreditCardClientException ccCE) {
        ccClient = null;
      }

      if (ccClient == null) {
        try {
          ccClient = new CreditCardClient(uddiURL, ccHostName);
        } catch (CreditCardClientException ccCE2) {
          throw new CreditCardClientException("Validation of credit card wasn't processed. Service is down.");
        }
      }

      // check if CreditCardNr is valid
      validation = ccClient.validateNumber(creditCardNr);

      if (!validation) {
        throw new CreditCardClientException("Invalid credit card information was given!");
      }

      cart = cartsMap.get(cartId);
      uncheckedItems = (Vector)cart.getCartItems();

      for (CartItem cartItem : uncheckedItems) {
        SupplierClient sCAux;
        supplierId = cartItem.getSupplierId();
        productId = cartItem.getProductId();
        quantity = cartItem.getQuantity();

        if ((sCAux = suppliersMap.get(supplierId)) == null) {
          try {
            sCAux = new SupplierClient(supplierId, uddiURL);
            suppliersMap.put(supplierId, sCAux);
          } catch(SupplierClientException sC) {
            throw sC;
          }
        }

        try {
          sCAux.buyProduct(productId, quantity);
        } catch (BadProductId_Exception bPI) {
          droppedItems.add(cartItem);
          continue;
        } catch(BadQuantity_Exception bQ) {
          droppedItems.add(cartItem);
          continue;
        } catch(InsufficientQuantity_Exception iQ) {
          droppedItems.add(cartItem);
          continue;
        }

        purchasedItems.add(cartItem);
        totalPrice += cartItem.getCartItemPrice();
      }

      cartPurchaseId = generateCartPurchaseId();
      if (droppedItems.isEmpty()) {
        result = Result.valueOf("COMPLETE");
      } else if (purchasedItems.isEmpty()) {
        result = Result.valueOf("EMPTY");
      } else {
        result = Result.valueOf("PARTIAL");
      }

      shoppingResult = new ShoppingResult(cartPurchaseId, result, purchasedItems, droppedItems, totalPrice);

      shopHistory.add(shoppingResult);
      cartsMap.remove(cartPurchaseId);

      return shoppingResult;
    }

    public List<ShoppingResult> getShopHistory() {
      return shopHistory;
    }

    // UDDI Related methods --------------------------------------------------
    /**
    * getWsUDDIRecords never returns a null object because of lazy initialization.
    * That allows the exceptions to be handled on mediator server side.
    */
    public List<UDDIRecord> getWsUDDIRecords(UDDINaming uddiNamingObj, String wsName) {
      try {
        this.wsUDDIRecords = new Vector<UDDIRecord>(uddiNamingObj.listRecords(wsName));
      } catch (UDDINamingException uNE) {
        System.out.println(uNE.getMessage());
      }
      return wsUDDIRecords;
    }

    /**
    * getWsURLs never returns a null object because of lazy initialization.
    * That allows the exceptions to be handled on mediator server side.
    */
    public List<String> getWsURLs(UDDINaming uddiNamingObj, String wsName) {
      try {
        this.wsURLs = new Vector<String>(uddiNamingObj.list(wsName));
      } catch (UDDINamingException uNE) {
        System.out.println(uNE.getMessage());
      }
      return wsURLs;
    }

    // SupplierClient related methods ----------------------------------------
    /** Creates a new SupplierClient and handles exceptions with given organization URL */
    public SupplierClient newSupplierClient(String wsURL) throws NullPointerException, SupplierClientException {
      SupplierClient supplierClient = null;
      try {
        supplierClient = new SupplierClient(wsURL);
      } catch (NullPointerException nPE) {
        throw nPE;
      } catch (SupplierClientException sCE) {
        throw sCE;
      }

      return supplierClient;
    }
    /** Creates a new SupplierClient and handles exceptions with given organization name and organization URL */
    public SupplierClient newSupplierClient(String orgName, String uddiURL) throws NullPointerException, SupplierClientException {
      SupplierClient supplierClient = null;
      try {
        supplierClient = new SupplierClient(orgName, uddiURL);
      } catch (NullPointerException nPE) {
        throw nPE;
      } catch (SupplierClientException sCE) {
        throw sCE;
      }

      return supplierClient;
    }

    // Replication methods ------------------------------------------------------
    public void replicateClear(String requestId) {
      if (getClearAnswer(requestId) != null) {
        System.out.println("Clear replication canceled. An operation with same ID as already been executed...");
        System.out.println("Repeated clear requestId: " + requestId);
        return;
      } else {
        System.out.println("Clear replication started. Setting operation as answered and incomplete...");
        setClearAnswer(requestId, falseBool);
        reset();
        setClearAnswer(requestId, trueBool);
        System.out.println("Clear replication finished. Setting operation as answered and complete...");
      }
    }
    /**
    * Does exactly the same as the original addCart method.
    * If the method is already answered (if map contains the key), it does
    * nothing. Otherwise it sets the request as answered. There is a very small
    * chance that two concurrent threads, one resulting from client making a new
    * invocation due to primary mediator crash and the other being the update
    * invocation seeing the operation as not answered, in which case a double
    * execution will occur.
    */
    public void replicateAddToCart(String cartId, ItemIdView itemId, int itemQty, String descText, int price, String requestId) {
      if (getAddToCartAnswer(requestId) != null) {
        System.out.println("Add to cart replication canceled. An operation with same ID as already been executed...");
        System.out.println("Repeated add to cart requestId: " + requestId);
        return;
      } else {
        System.out.println("Add to cart replication started. Setting operation as answered and incomplete...");
        setAddToCartAnswer(requestId, falseBool);
        addCart(cartId, itemId, itemQty, descText, price);
        setAddToCartAnswer(requestId, trueBool);
        System.out.println("Add to cart replication finished. Setting operation as answered and complete...");
      }
    }

    /**
    * Updates shopHistory and removes purchased cart from carts map.
    * If the method is already answered (if map contains the key), it does
    * nothing. Otherwise it sets the request as answered with recieved shopResultView
    * this view will always be truthfull in almost all situations. There is a
    * very, small chance that two concurrent threads, ne resulting from client
    * making a new invocation due to primary mediator crash and the other being
    * the update invocation seeing the operation as not answered, in which case
    * a double execution will occur.
    */
    public void replicateBuyCart(ShoppingResultView shoppingResultView, String requestId) {
      if (getBuyCartAnswer(requestId) != null) {
        System.out.println("Buy cart replication canceled. An operation with same ID as already been executed...");
        System.out.println("Repeated buy cart requestId: " + requestId);
        return;
      } else {
        System.out.println("Add to cart replication started. Setting operation as answered and complete...");
        setBuyCartAnswer(requestId, shoppingResultView);
        shopHistory.add(newShoppingResult(shoppingResultView));
        cartsMap.remove(shoppingResultView.getId());
      }
    }

    // Helper methods ------------------------------------------------------

    /** Cleans up all datastructures associated with carts and UDDI Registry. */
    public void reset() {
      cartsMap.clear();
      shopHistory.clear();
      addCartAnswers.clear();
      buyCartAnswers.clear();
      clearAnswers.clear();
      wsUDDIRecords.clear();
      wsURLs.clear();
    }

    /** generates a unique purchaseId to be used on buyCart */
    private String generateCartPurchaseId() {
      // relying on AtomicInteger to make sure assigned number is unique
      int purchaseId = cartPurchaseIdCounter.incrementAndGet();
      return Integer.toString(purchaseId);
    }

    public synchronized Boolean getClearAnswer(String requestId) {
      return clearAnswers.get(requestId);
    }

    public synchronized Boolean getAddToCartAnswer(String requestId) {
      return addCartAnswers.get(requestId);
    }

    public synchronized ShoppingResultView getBuyCartAnswer(String requestId) {
      return buyCartAnswers.get(requestId);
    }

    public synchronized void setClearAnswer(String requestId, Boolean bool) {
      clearAnswers.put(requestId, Boolean.valueOf(true));
    }

    public synchronized void setAddToCartAnswer(String requestId, Boolean bool) {
      addCartAnswers.put(requestId, bool);
    }

    public synchronized void setBuyCartAnswer(String requestId, ShoppingResultView shoppingResultView) {
      buyCartAnswers.put(requestId, shoppingResultView);
    }


    /** Reverts a ShoppingResultView back to Shopping Result */
    private ShoppingResult newShoppingResult(ShoppingResultView shoppingResultView) {
  		ShoppingResult shoppingResult = new ShoppingResult();

      List<CartItemView> purchasedItems = (ArrayList)shoppingResultView.getPurchasedItems();
      List<CartItem> purchasedAux = new ArrayList<CartItem>();

      List<CartItemView> droppedItems = (ArrayList)shoppingResultView.getDroppedItems();
      List<CartItem> droppedAux = new ArrayList<CartItem>();

  		shoppingResult.setId(shoppingResultView.getId());
  		shoppingResult.setResult(shoppingResultView.getResult());

  		for (CartItemView cartItemView : purchasedItems) {
  			purchasedAux.add(newCartItem(cartItemView));
  		}
  		for (CartItemView cartItemView : droppedItems) {
  			droppedAux.add(newCartItem(cartItemView));
  		}

      shoppingResult.setPurchasedItems(purchasedAux);
      shoppingResult.setDroppedItems(droppedAux);

  		shoppingResult.setTotalPrice(shoppingResultView.getTotalPrice());
  		return shoppingResult;
  	}

    /** Reverts a CartItemView back to CartItem */
    private CartItem newCartItem(CartItemView cartItemView) {
      Item item = newItem(cartItemView.getItem());
      CartItem cartItem = new CartItem();
      cartItem.setItem(item);
      cartItem.setQuantity(cartItemView.getQuantity());
      return cartItem;
    }

    /** Reverts a ItemView back to Item */
    private Item newItem(ItemView itemView) {
      ItemId itemId = newItemId(itemView.getItemId());
      Item item = new Item();
      item.setItemId(itemId);
      item.setDescription(itemView.getDesc());
      item.setPrice(itemView.getPrice());
      return item;
    }

    /** Reverts a ItemIdView back to ItemId */
    private ItemId newItemId(ItemIdView itemIdView) {
      ItemId itemId = new ItemId();
      itemId.setProductId(itemIdView.getProductId());
      itemId.setSupplierId(itemIdView.getSupplierId());
      return itemId;
    }
}
