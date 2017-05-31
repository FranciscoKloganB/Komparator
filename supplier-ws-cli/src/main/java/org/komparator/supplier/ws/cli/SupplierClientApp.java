package org.komparator.supplier.ws.cli;
import java.io.*;
import java.lang.*;
import java.util.*;
import org.komparator.supplier.ws.*;

/** Main class that starts the Supplier Web Service client. */
public class SupplierClientApp {
	private static SupplierClient client = null;
	private static Scanner keyboardScanner;
	private static int operation, quantity, price;
	private static String productid, description;
	private static  void clear() {
		client.clear();
	}

	private static  void ping() {
		client.ping("client");
	}

	private static  void listProducts() {
		List<ProductView> list = client.listProducts();
		if (list.isEmpty()) {
			System.out.println("No products available.");
		} else {
			StringBuilder builder = new StringBuilder();
			for (ProductView product : list) {
				builder.append("Product [productId=").append(product.getId());
				builder.append(", description=").append(product.getDesc());
				builder.append(", quantity=").append(product.getQuantity());
				builder.append(", price=").append(product.getPrice());
				builder.append("]").append(System.getProperty("line.separator"));
			}
			System.out.println(builder.toString());
		}
	}

	private static void listPurchases() {
		List<PurchaseView> list =client.listPurchases();
		if (list.isEmpty()) {
			System.out.println("No products were purchased.");
		} else {
			StringBuilder builder = new StringBuilder();
			for (PurchaseView purchase : list) {
				builder.append("Purchase [purchaseId=").append(purchase.getId());
				builder.append(", productId=").append(purchase.getProductId());
				builder.append(", quantity=").append(purchase.getQuantity());
				builder.append(", unitPrice=").append(purchase.getUnitPrice());
				builder.append("]").append(System.getProperty("line.separator"));
			}
			System.out.println(builder.toString());
		}
	}

	private static void getProduct(String productId) throws BadProductId_Exception {
		ProductView product = client.getProduct(productId);
		StringBuilder builder = new StringBuilder();
		builder.append("Product [productId=").append(product.getId());
		builder.append(", description=").append(product.getDesc());
		builder.append(", quantity=").append(product.getQuantity());
		builder.append(", price=").append(product.getPrice());
		builder.append("]").append(System.getProperty("line.separator"));
		System.out.println(builder.toString());
	}

	private static  void searchProducts(String descText) throws BadText_Exception {
		List<ProductView> list = client.searchProducts(descText);
		if (list.isEmpty()) {
			System.out.println("No products were found with given description.");
		} else {
			StringBuilder builder = new StringBuilder();
			for (ProductView product : list) {
				builder.append("Product [productId=").append(product.getId());
				builder.append(", description=").append(product.getDesc());
				builder.append(", quantity=").append(product.getQuantity());
				builder.append(", price=").append(product.getPrice());
				builder.append("]").append(System.getProperty("line.separator"));
			}
			System.out.println(builder.toString());
		}
	}

	private static void buyProduct(String productId, int quantity) throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		System.out.println(client.buyProduct(productId, quantity));
	}

	private static void createProduct(ProductView productToCreate) throws BadProductId_Exception, BadProduct_Exception {
		client.createProduct(productToCreate);
	}

	private static void listOperations() {
		StringBuilder builder = new StringBuilder();
		builder.append("Listing available operations: ").append(System.getProperty("line.separator"));
		builder.append("0 : exitApplication()").append(System.getProperty("line.separator"));
		builder.append("1 : clear()").append(System.getProperty("line.separator"));
		builder.append("2 : ping()").append(System.getProperty("line.separator"));
		builder.append("3 : listProducts()").append(System.getProperty("line.separator"));
		builder.append("4 : listPurchases()").append(System.getProperty("line.separator"));
		builder.append("5 : getProduct(String productId)").append(System.getProperty("line.separator"));
		builder.append("6 : searchProducts(String descText)").append(System.getProperty("line.separator"));
		builder.append("7 : buyProduct(String productId, int quantity)").append(System.getProperty("line.separator"));
		builder.append("8 : createProduct(String productId, String descText, int quantity, int price)").append(System.getProperty("line.separator"));
    builder.append("9 : relistOperations()").append(System.getProperty("line.separator"));
		builder.append(System.getProperty("line.separator"));
		System.out.println(builder.toString());
	}

	private static void loadStoreFromFile(String filename) throws BadProduct_Exception, BadProductId_Exception {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
		if (is == null) {
			System.out.println("Error: Could not load InputStream resource...");
			return;
		}
		Scanner fileScanner = new Scanner(is);
		while(fileScanner.hasNextLine()) {
			StringTokenizer tokenizer = new StringTokenizer(fileScanner.nextLine());
			ProductView productToCreate = new ProductView();
			productid = tokenizer.nextToken();
			description = tokenizer.nextToken();
			quantity = Integer.parseInt(tokenizer.nextToken());
			price = Integer.parseInt(tokenizer.nextToken());
			productToCreate.setId(productid);
			productToCreate.setDesc(description);
			productToCreate.setQuantity(quantity);
			productToCreate.setPrice(price);
			createProduct(productToCreate);
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		keyboardScanner = new Scanner(System.in);
		// Check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierClientApp.class.getName() + " wsURL");
			return;
		}

		String wsURL = args[0];
		String wsName = args[1];
		String uddiURL = args[2];

		// Create client
		// System.out.printf("Creating client for server at %s%n", wsURL);
		// SupplierClient client = new SupplierClient(wsURL);
		client = new SupplierClient(wsName, uddiURL);
		System.out.println("");
		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit
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
				listProducts();
      } else if (operation == 4) {
        listPurchases();
      } else if (operation == 5) {
        System.out.println("Enter product id: ");
        productid = keyboardScanner.next();
        getProduct(productid);
      } else if (operation == 6) {
        System.out.println("Enter product description.");
				description = keyboardScanner.next();
        searchProducts(description);
      } else if (operation == 7) {
        System.out.println("Enter product id: ");
        productid = keyboardScanner.next();
        System.out.println("Enter quantity: ");
        quantity = keyboardScanner.nextInt();
        buyProduct(productid, quantity);
      } else if (operation == 8) {
				ProductView productToCreate = new ProductView();
				System.out.println("Enter product id: ");
				productid = keyboardScanner.next();
				System.out.println("Enter product description.");
				description = keyboardScanner.next();
				System.out.println("Enter quantity: ");
				quantity = keyboardScanner.nextInt();
				System.out.println("Enter price: ");
				price = keyboardScanner.nextInt();
				productToCreate.setId(productid);
				productToCreate.setDesc(description);
				productToCreate.setQuantity(quantity);
				productToCreate.setPrice(price);
        createProduct(productToCreate);
      } else if (operation == 9) {
        listOperations();
			} else if (operation == 10) {
				System.out.println("Enter the name of the file to load: ");
				String filename = keyboardScanner.next();
				loadStoreFromFile(filename);
			} else {
        System.out.println("Unrecognizable operation, try again.");
        continue;
      }
    }
	}

}
