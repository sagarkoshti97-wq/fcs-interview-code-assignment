package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseAndStoreEndpointTest {

	@Test
	@Order(1)
	public void testStoreEndpointsAndLegacyGatewaySync() {
		// List stores
		given().when().get("/store").then().statusCode(200).body(containsString("TONSTAD"), containsString("KALLAX"));

		// Get single store
		given().when().get("/store/1").then().statusCode(200).body("name", is("TONSTAD"));

		// Get non-existent store -> 404
		given().when().get("/store/999").then().statusCode(404);

		// Create store -> 201 (or 202) + triggers post-commit event
		String newStoreJson = "{\"name\": \"HEMNES\", \"quantityProductsInStock\": 12}";
		Integer createdId = given().contentType(ContentType.JSON).body(newStoreJson).when().post("/store").then()
				.statusCode(anyOf(is(201), is(202))).extract().path("id");

		// Create store with ID -> 422
		given().contentType(ContentType.JSON).body("{\"id\": 50, \"name\": \"INVALID\"}").when().post("/store").then()
				.statusCode(422);

		// Update store -> 200
		given().contentType(ContentType.JSON).body("{\"name\": \"HEMNES_UPDATED\", \"quantityProductsInStock\": 20}")
				.when().put("/store/" + createdId).then().statusCode(200).body("name", is("HEMNES_UPDATED"));

		// Update store without name -> 422
		given().contentType(ContentType.JSON).body("{\"quantityProductsInStock\": 20}").when()
				.put("/store/" + createdId).then().statusCode(422);

		// Update non-existent store -> 404
		given().contentType(ContentType.JSON).body("{\"name\": \"NONE\", \"quantityProductsInStock\": 20}").when()
				.put("/store/999").then().statusCode(404);

		// Patch store -> 200
		given().contentType(ContentType.JSON).body("{\"name\": \"HEMNES_PATCHED\", \"quantityProductsInStock\": 25}")
				.when().patch("/store/" + createdId).then().statusCode(200).body("name", is("HEMNES_PATCHED"));

		// Patch store without name -> 422
		given().contentType(ContentType.JSON).body("{\"quantityProductsInStock\": 25}").when()
				.patch("/store/" + createdId).then().statusCode(422);

		// Patch non-existent store -> 404
		given().contentType(ContentType.JSON).body("{\"name\": \"NONE\", \"quantityProductsInStock\": 25}").when()
				.patch("/store/999").then().statusCode(404);

		// Delete store -> 204
		given().when().delete("/store/" + createdId).then().statusCode(204);
		given().when().delete("/store/999").then().statusCode(404);
	}

	@Test
	@Order(2)
	public void testWarehouseCrudAndReplaceEndpoints() {
		// List warehouses
		given().when().get("/warehouse").then().statusCode(200).body(containsString("MWH.001"),
				containsString("MWH.012"), containsString("MWH.023"));

		// Get by ID and by Business Unit Code
		given().when().get("/warehouse/MWH.012").then().statusCode(200).body("location", is("AMSTERDAM-001"));

		given().when().get("/warehouse/999").then().statusCode(404);

		// Create a new warehouse at AMSTERDAM-002
		String createPayload = "{\"businessUnitCode\": \"MWH.099\", \"location\": \"AMSTERDAM-002\", \"capacity\": 40, \"stock\": 15}";
		given().contentType(ContentType.JSON).body(createPayload).when().post("/warehouse").then().statusCode(200)
				.body("businessUnitCode", is("MWH.099"));

		// Replace warehouse MWH.099
		String replacePayload = "{\"businessUnitCode\": \"MWH.099\", \"location\": \"AMSTERDAM-002\", \"capacity\": 50, \"stock\": 15}";
		given().contentType(ContentType.JSON).body(replacePayload).when().post("/warehouse/MWH.099/replacement").then()
				.statusCode(200).body("capacity", is(50));

		// Archive warehouse MWH.099
		given().when().delete("/warehouse/MWH.099").then().statusCode(204);
		given().when().get("/warehouse/MWH.099").then().statusCode(404);
		given().when().delete("/warehouse/MWH.099").then().statusCode(404);
	}

	@Test
	@Order(3)
	public void testBonusFulfilmentConstraints() {
		// Create fresh products so we don't depend on IDs deleted by
		// ProductEndpointTest
		Integer prod1 = given().contentType(ContentType.JSON).body("{\"name\": \"PROD_BONUS_1\", \"stock\": 10}").when()
				.post("/product").then().statusCode(201).extract().path("id");

		Integer prod2 = given().contentType(ContentType.JSON).body("{\"name\": \"PROD_BONUS_2\", \"stock\": 10}").when()
				.post("/product").then().statusCode(201).extract().path("id");

		Integer prod3 = given().contentType(ContentType.JSON).body("{\"name\": \"PROD_BONUS_3\", \"stock\": 10}").when()
				.post("/product").then().statusCode(201).extract().path("id");

		Integer prod4 = given().contentType(ContentType.JSON).body("{\"name\": \"PROD_BONUS_4\", \"stock\": 10}").when()
				.post("/product").then().statusCode(201).extract().path("id");

		Integer prod5 = given().contentType(ContentType.JSON).body("{\"name\": \"PROD_BONUS_5\", \"stock\": 10}").when()
				.post("/product").then().statusCode(201).extract().path("id");

		Integer prod6 = given().contentType(ContentType.JSON).body("{\"name\": \"PROD_BONUS_6\", \"stock\": 10}").when()
				.post("/product").then().statusCode(201).extract().path("id");

		// Create additional warehouses to test constraints
		given().contentType(ContentType.JSON).body(
				"{\"businessUnitCode\": \"MWH.101\", \"location\": \"AMSTERDAM-001\", \"capacity\": 20, \"stock\": 5}")
				.when().post("/warehouse").then().statusCode(200);

		given().contentType(ContentType.JSON).body(
				"{\"businessUnitCode\": \"MWH.102\", \"location\": \"EINDHOVEN-001\", \"capacity\": 30, \"stock\": 5}")
				.when().post("/warehouse").then().statusCode(200);

		// Assign MWH.001 & MWH.012 to Store 1, prod1 (Max 2 warehouses per product per
		// store)
		Integer assocId = given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.001\", \"storeId\": 1, \"productId\": " + prod1 + "}")
				.when().post("/fulfilment").then().statusCode(201).extract().path("id");

		// Idempotent duplicate assignment returns 201
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.001\", \"storeId\": 1, \"productId\": " + prod1 + "}")
				.when().post("/fulfilment").then().statusCode(201);

		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.012\", \"storeId\": 1, \"productId\": " + prod1 + "}")
				.when().post("/fulfilment").then().statusCode(201);

		// 3rd warehouse for same prod1 and Store 1 -> Must fail with 400 (Constraint 1)
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.023\", \"storeId\": 1, \"productId\": " + prod1 + "}")
				.when().post("/fulfilment").then().statusCode(400);

		// Assign MWH.023 to Store 1, prod2 (3rd warehouse for Store 1 - allowed)
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.023\", \"storeId\": 1, \"productId\": " + prod2 + "}")
				.when().post("/fulfilment").then().statusCode(201);

		// 4th warehouse (MWH.101) for Store 1 -> Must fail with 400 (Constraint 2: Max
		// 3 warehouses per Store)
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.101\", \"storeId\": 1, \"productId\": " + prod3 + "}")
				.when().post("/fulfilment").then().statusCode(400);

		// Test Constraint 3: Max 5 product types per Warehouse (using MWH.102 and Store
		// 2)
		for (Integer pid : new Integer[] { prod1, prod2, prod3, prod4, prod5 }) {
			given().contentType(ContentType.JSON)
					.body("{\"warehouseBusinessUnitCode\": \"MWH.102\", \"storeId\": 2, \"productId\": " + pid + "}")
					.when().post("/fulfilment").then().statusCode(201);
		}

		// 6th product in MWH.102 -> Must fail with 400 (Constraint 3)
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.102\", \"storeId\": 2, \"productId\": " + prod6 + "}")
				.when().post("/fulfilment").then().statusCode(400);

		// Validation & 404 checks on /fulfilment
		given().contentType(ContentType.JSON).body("{}").when().post("/fulfilment").then().statusCode(400);
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"NONE\", \"storeId\": 1, \"productId\": " + prod1 + "}").when()
				.post("/fulfilment").then().statusCode(404);
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.001\", \"storeId\": 999, \"productId\": " + prod1 + "}")
				.when().post("/fulfilment").then().statusCode(404);
		given().contentType(ContentType.JSON)
				.body("{\"warehouseBusinessUnitCode\": \"MWH.001\", \"storeId\": 1, \"productId\": 999}").when()
				.post("/fulfilment").then().statusCode(404);

		// List and delete association
		given().when().get("/fulfilment").then().statusCode(200);
		given().when().delete("/fulfilment/" + assocId).then().statusCode(204);
		given().when().delete("/fulfilment/99999").then().statusCode(404);
	}
}