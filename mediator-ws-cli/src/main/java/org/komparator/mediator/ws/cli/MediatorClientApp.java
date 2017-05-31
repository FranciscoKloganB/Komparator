package org.komparator.mediator.ws.cli;

import java.util.*;
import java.lang.*;
import java.io.*;
import org.komparator.mediator.ws.*;

public class MediatorClientApp {
  private static MediatorClient client = null;
  private static Scanner keyboardScanner;
  private static int operation, quantity;
  private static String description, productid, supplierid, cartid, creditcard;

  private static void clear() {
    client.clear();
  }

  private static void ping() {
    System.out.print(client.ping("client"));
  }

  private static void searchItems(String descText) throws Exception  {
    StringBuilder builder = new StringBuilder();
    List<ItemView> list = client.searchItems(descText);
    builder.append("Items:");
    builder.append(System.getProperty("line.separator"));
    for (ItemView item : list) {
      builder.append("Product [ProductId=").append(item.getItemId().getProductId());
      builder.append(", supplierId=").append(item.getItemId().getSupplierId());
      builder.append(", description=").append(item.getDesc());
      builder.append(", price=").append(item.getPrice());
      builder.append("]");
      builder.append(System.getProperty("line.separator"));
    }
    builder.append(System.getProperty("line.separator"));
    System.out.println(builder.toString());
  }

  private static void listCarts() {
    StringBuilder builder = new StringBuilder();
    List<CartView> list = client.listCarts();
    builder.append("Carts:");
    builder.append(System.getProperty("line.separator"));
    for (CartView cart : list) {
      builder.append("Cart [CartId=").append(cart.getCartId());
      builder.append(", items=");
      List<CartItemView> cartItems = cart.getItems();
      for (CartItemView item : cartItems) {
          builder.append("Product [ProductId=").append(item.getItem().getItemId().getProductId());
          builder.append(", supplierId=").append(item.getItem().getItemId().getSupplierId());
          builder.append(", description=").append(item.getItem().getDesc());
          builder.append(", price=").append(item.getItem().getPrice());
          builder.append("]");
          builder.append(", quantity=").append(item.getQuantity());
          builder.append("]");
      }
      builder.append(System.getProperty("line.separator"));
    }
    builder.append(System.getProperty("line.separator"));
    System.out.println(builder.toString());
  }

  private static void getItems(String productId) throws Exception  {
    StringBuilder builder = new StringBuilder();
    List<ItemView> list = client.getItems(productId);
    builder.append("Items:");
    builder.append(System.getProperty("line.separator"));
    for (ItemView item : list) {
      builder.append("Product [ProductId=").append(item.getItemId().getProductId());
      builder.append(", supplierId=").append(item.getItemId().getSupplierId());
      builder.append(", description=").append(item.getDesc());
      builder.append(", price=").append(item.getPrice());
      builder.append("]");
      builder.append(System.getProperty("line.separator"));
    }
    builder.append(System.getProperty("line.separator"));
    System.out.println(builder.toString());
  }

  private static void buyCart(String cartId, String creditCardNr) throws Exception  {
    StringBuilder builder = new StringBuilder();
    ShoppingResultView result = client.buyCart(cartId, creditCardNr);
    List<CartItemView> purchasedItems = result.getPurchasedItems();
    List<CartItemView> droppedItems = result.getDroppedItems();
    builder.append("ShoppingResult [id=").append(result.getId());
    builder.append(", result=").append(result.getResult().value());
    builder.append(", purchasedItems=[");
    for (CartItemView item : purchasedItems) {
      builder.append("Product [ProductId=").append(item.getItem().getItemId().getProductId());
      builder.append(", supplierId=").append(item.getItem().getItemId().getSupplierId());
      builder.append(", description=").append(item.getItem().getDesc());
      builder.append(", price=").append(item.getItem().getPrice());
      builder.append("]");
      builder.append(", quantity=").append(item.getQuantity());
      builder.append("]");
      builder.append(System.getProperty("line.separator"));
    }
    builder.append("], droppedItems=[");
    for (CartItemView item : droppedItems) {
      builder.append("Product [ProductId=").append(item.getItem().getItemId().getProductId());
      builder.append(", supplierId=").append(item.getItem().getItemId().getSupplierId());
      builder.append(", description=").append(item.getItem().getDesc());
      builder.append(", price=").append(item.getItem().getPrice());
      builder.append("]");
      builder.append(", quantity=").append(item.getQuantity());
      builder.append("]");
      builder.append(System.getProperty("line.separator"));
    }
    builder.append(System.getProperty("line.separator"));
    System.out.println(builder.toString());
  }

  private static void addToCart(String cartId, ItemIdView itemId, int itemQty) throws Exception  {
    client.addToCart(cartId, itemId, itemQty);
  }

  private static void shopHistory() {
    StringBuilder builder = new StringBuilder();
    List<ShoppingResultView> list = client.shopHistory();
    for (ShoppingResultView result : list) {
      List<CartItemView> purchasedItems = result.getPurchasedItems();
      List<CartItemView> droppedItems = result.getDroppedItems();
      builder.append("ShoppingResult [id=").append(result.getId());
      builder.append(", result=").append(result.getResult().value());
      builder.append(", purchasedItems=[");
      for (CartItemView item : purchasedItems) {
        builder.append("Product [ProductId=").append(item.getItem().getItemId().getProductId());
        builder.append(", supplierId=").append(item.getItem().getItemId().getSupplierId());
        builder.append(", description=").append(item.getItem().getDesc());
        builder.append(", price=").append(item.getItem().getPrice());
        builder.append("]");
        builder.append(", quantity=").append(item.getQuantity());
        builder.append("]");
        builder.append(System.getProperty("line.separator"));
      }
      builder.append("], droppedItems=[");
      for (CartItemView item : droppedItems) {
        builder.append("Product [ProductId=").append(item.getItem().getItemId().getProductId());
        builder.append(", supplierId=").append(item.getItem().getItemId().getSupplierId());
        builder.append(", description=").append(item.getItem().getDesc());
        builder.append(", price=").append(item.getItem().getPrice());
        builder.append("]");
        builder.append(", quantity=").append(item.getQuantity());
        builder.append("]");
        builder.append(System.getProperty("line.separator"));
      }
      builder.append(System.getProperty("line.separator"));
    }
    builder.append(System.getProperty("line.separator"));
    System.out.println(builder.toString());
  }

  private static void listOperations() {
    StringBuilder builder = new StringBuilder();
    builder.append("Listing available operations: ").append(System.getProperty("line.separator"));
    builder.append("0 : exitApplication()").append(System.getProperty("line.separator"));
    builder.append("1 : clear()").append(System.getProperty("line.separator"));
    builder.append("2 : ping()").append(System.getProperty("line.separator"));
    builder.append("3 : searchItems(String descText)").append(System.getProperty("line.separator"));
    builder.append("4 : listCarts()").append(System.getProperty("line.separator"));
    builder.append("5 : getItems(String productId)").append(System.getProperty("line.separator"));
    builder.append("6 : buyCart(String cartId, String creditCardNr)").append(System.getProperty("line.separator"));
    builder.append("7 : addToCart(String cartId, String productId, String supplierId, int itemQty)").append(System.getProperty("line.separator"));
    builder.append("9 : relistOperations()").append(System.getProperty("line.separator"));
    builder.append(System.getProperty("line.separator"));
    System.out.println(builder.toString());
  }

  public static void main(String[] args) throws Exception {
    keyboardScanner = new Scanner(System.in);

    if (args.length == 0) {
      System.err.println("Argument(s) missing!");
      System.err.println("Usage: java " + MediatorClientApp.class.getName() + " wsURL OR uddiURL wsName");
      return;
    }

    String uddiURL = null;
    String wsName = null;
    String wsURL = null;

    if (args.length == 1) {
      wsURL = args[0];
    } else if (args.length >= 2) {
      uddiURL = args[0];
      wsName = args[1];
    }

    if (wsURL != null) {
      System.out.printf("Creating client for server at %s%n", wsURL);
      client = new MediatorClient(wsURL);
    } else if (uddiURL != null) {
      System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
      client = new MediatorClient(uddiURL, wsName);
    }

    listOperations();

    while(true) {
      operation = keyboardScanner.nextInt();
      if (operation == 0) {
        client = null;
        keyboardScanner.reset();
        break;
      } else if (operation == 1) {
        clear();
      } else if (operation == 2) {
        ping();
      } else if (operation == 3) {
        description = keyboardScanner.next();
        System.out.println("Enter item description: ");
        searchItems(description);
      } else if (operation == 4) {
        listCarts();
      } else if (operation == 5) {
        System.out.println("Enter product id: ");
        productid = keyboardScanner.next();
        getItems(productid);
      } else if (operation == 6) {
        System.out.println("Enter card id.");
        cartid = keyboardScanner.next();
        System.out.println("Enter credit card number.");
        creditcard = keyboardScanner.next();
        buyCart(cartid, creditcard);
      } else if (operation == 7) {
        System.out.println("Enter card id: ");
        cartid = keyboardScanner.next();
        System.out.println("Enter product id: ");
        productid = keyboardScanner.next();
        System.out.println("Enter supplier id: ");
        supplierid = keyboardScanner.next();
        System.out.println("Enter quantity: ");
        quantity = keyboardScanner.nextInt();
        ItemIdView itemid = new ItemIdView();
        itemid.setProductId(productid);
        itemid.setSupplierId(supplierid);
        addToCart(cartid, itemid, quantity);
      } else if (operation == 8) {
        shopHistory();
      } else if (operation == 9) {
        listOperations();
      } else {
        System.out.println("Unrecognizable operation, try again.");
        continue;
      }
    }

    System.out.println("Exiting application.");
    System.out.println("");
  }
}
