package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {}

	@AfterClass
	public static void oneTimeTearDown() {}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
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
			product.setDesc("Basketball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
		client.clear();
	}

	// tests
	// assertEquals(expected, actual);

	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	// bad input tests

	@Test(expected = BadText_Exception.class)
	public void searchProductsNullTest() throws BadText_Exception {
		client.searchProducts(null);
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsEmptyTest() throws BadText_Exception {
		client.searchProducts("");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsWhiteSpaceTest() throws BadText_Exception {
		client.searchProducts(" ");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsTabTest() throws BadText_Exception {
		client.searchProducts("\t");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsNewLineTest() throws BadText_Exception {
		client.searchProducts("\n");
	}

	// main tests

	@Test
	public void searchProductTestLenght() throws BadText_Exception {
		// List size should be two, containing product X1 and Y2, but not Z3.
		List<ProductView> foundProducts = client.searchProducts("Basketball");
		assertEquals(2, foundProducts.size());
	}
	@Test
	public void searchProductTestDescriptionOne() throws BadText_Exception {
		// List should only contain X1 and Y2 items, that means only items with description Basketball
		List<ProductView> foundProducts = client.searchProducts("Basketball");
		for (ProductView pv : foundProducts) {
			assertEquals("Basketball", pv.getDesc());
		}
	}

	@Test
	public void searchProductTestDescriptionTwo() throws BadText_Exception {
		// List should be of size 0, because no items have item description basketBall
		List<ProductView> foundProducts = client.searchProducts("basketBall");
		assertEquals(true, foundProducts.isEmpty());
	}

	@Test
	public void searchProductTestDescriptionThree() throws BadText_Exception {
		// List should be of size 0, because no items have item description something
		List<ProductView> foundProducts = client.searchProducts("something");
		assertEquals(true, foundProducts.isEmpty());
	}

	@Test
	public void searchProductTestFive() throws BadText_Exception {
		// List should be of size 1, because 'Soccer Ball' contains 'Soccer '
		List<ProductView> foundProducts = client.searchProducts("Soccer ");
		assertEquals(1, foundProducts.size());
	}

	@Test
	public void searchProductTestSix() throws BadText_Exception {
		// List should be of size 2, because 'Basketball' contains 'Basket'
		List<ProductView> foundProducts = client.searchProducts("Basket");
		assertEquals(2, foundProducts.size());
	}

	@Test
	public void searchProductTestSeven() throws BadText_Exception {
		// List should be of size 2, because 'Basketball' contains 'Basket'
		List<ProductView> foundProducts = client.searchProducts("ket");
		assertEquals(2, foundProducts.size());
	}

	@Test
	public void searchProductTestProductId() throws BadText_Exception {
		// List should only contain X1 and Y2 items, that means only items with description Basketball
		String productId1;
		String productId2;
		ArrayList<ProductView> foundProducts = (ArrayList<ProductView>)client.searchProducts("Basketball");
		assertEquals(2, foundProducts.size());
		productId1 = foundProducts.get(0).getId();
		productId2 = foundProducts.get(1).getId();

		if (productId1.equals(productId2)) {
			Assert.fail("ProductId1 and ProductId2 are equal and shouldnt be");
		}

		if (!(productId1.equals("X1") || productId1.equals("Y2"))) {
			Assert.fail("ProductId1 is not X1 nor Y2. Was: " + productId1);
		}

		if (!(productId2.equals("X1") || productId2.equals("Y2"))) {
			Assert.fail("ProductId2 is not X1 nor Y2. Was:" + productId2);
		}
	}

	@Test
	public void searchProductTestChangeId() throws BadText_Exception, BadProductId_Exception, BadProduct_Exception {
		ArrayList<ProductView> foundProducts = (ArrayList<ProductView>)client.searchProducts("Soccer ball");
		assertEquals(1, foundProducts.size());
		assertEquals("Z3", foundProducts.get(0).getId());
		assertEquals(30, foundProducts.get(0).getPrice());
		assertEquals(30, foundProducts.get(0).getQuantity());

		// Replaces Z3 with a new, different, Soccer ball and creates yet another Soccer ball.
		// Z3 should have updated fields and two products should be found on search.
		ProductView newproduct1 = new ProductView();
		newproduct1.setId("Z3");
		newproduct1.setDesc("Soccer ball");
		newproduct1.setPrice(10);
		newproduct1.setQuantity(10);
		client.createProduct(newproduct1);
		ProductView newproduct2 = new ProductView();
		newproduct2.setId("Z4");
		newproduct2.setDesc("Soccer ball");
		newproduct2.setPrice(20);
		newproduct2.setQuantity(20);
		client.createProduct(newproduct2);
		foundProducts = (ArrayList<ProductView>)client.searchProducts("Soccer ball");
		assertEquals(2, foundProducts.size());
		for (ProductView pv : foundProducts) {
			if (pv.getId().equals("Z3")) {
				assertEquals(10, pv.getPrice());
				assertEquals(10, pv.getQuantity());
			}
		}
	}

	@Test
	public void searchProductEmptyList() throws BadText_Exception {
		client.clear();
		List<ProductView> foundProducts = client.searchProducts("Basketball");
		if (!foundProducts.isEmpty()) {
			Assert.fail();
		}
	}
}
