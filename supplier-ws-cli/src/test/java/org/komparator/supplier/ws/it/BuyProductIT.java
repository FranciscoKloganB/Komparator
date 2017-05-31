package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {
	// clear remote service state before all tests
	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws  BadProductId_Exception, BadProduct_Exception {
		client.clear();
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Futebol");
			product.setPrice(8);
			product.setQuantity(20);
			client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
		client.clear();
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null, 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhitespaceTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(" ", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNewlineTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\n", 1);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductNegativeInt() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", -1);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductNullInt() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 0);
	}

	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductPositiveAboveStock() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 11);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullIdAndInvalidInt() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null, -1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyIdAndInvalidInt() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("", -1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhiteSpaceIdAndInvalidInt() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(" ", -1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabIdAndInvalidInt() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t", -1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNewLineIdAndInvalidInt() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\n", -1);
	}

	// main tests
	@Test
	public void success() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception{
		String purchase = client.buyProduct("X1",5);

		int quantity = client.getProduct("X1").getQuantity();
		List<PurchaseView> purchases = client.listPurchases();

		assertEquals(5, quantity);
		assertEquals(1, purchases.size());

	}

	@Test
	public void sameQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception  {
		client.buyProduct("X1",10);
		int quantity = client.getProduct("X1").getQuantity();
		assertEquals(0, quantity);
	}

	@Test(expected = BadProductId_Exception.class)
	public void LowerCaseProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception  {
		client.buyProduct("x1",10);
	}

	@Test(expected = BadProductId_Exception.class)
	public void noProductTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception  {
		client.buyProduct("X2",10);
	}
}
