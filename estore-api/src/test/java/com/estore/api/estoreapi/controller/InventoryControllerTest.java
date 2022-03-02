package com.estore.api.estoreapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.estore.api.estoreapi.model.Product;
import com.estore.api.estoreapi.persistence.InventoryDAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag("Controller")
public class InventoryControllerTest {
    private InventoryController inventoryController;
    private InventoryDAO mockInventoryDAO;

    @BeforeEach
    public void setUp() {
        mockInventoryDAO = mock(InventoryDAO.class);
        inventoryController = new InventoryController(mockInventoryDAO);
    }

    @Test
    public void testCreateProduct() throws IOException {
        Product product = new Product("test", "testdes", 1.0, 1);

        when(mockInventoryDAO.createProduct(product)).thenReturn(product);

        ResponseEntity<Product> response = inventoryController.createProduct(product);

        assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        assertEquals(response.getBody(), product);
    }

    @Test
    public void testUpdateProduct() throws IOException {
        Product product = new Product("test", "testdes", 1.0, 1);

        when(mockInventoryDAO.updateProduct(product)).thenReturn(product);

        ResponseEntity<Product> response = inventoryController.updateProduct(product);
        when(mockInventoryDAO.updateProduct(product)).thenThrow(new IllegalArgumentException());
        ResponseEntity<Product> response2 = inventoryController.updateProduct(product);
        assertEquals(response2.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), product);
    }

    @Test
    public void testGetInventory() throws IOException {
        Product[] products = new Product[3];
        products[0] = new Product("test", "testdes", 1.0, 1);
        products[1] = new Product("test1", "test2des", 1.0, 1);
        products[2] = new Product("test2", "test3des", 1.0, 1);

        when(mockInventoryDAO.getInventory()).thenReturn(products);

        ResponseEntity<Product[]> response = inventoryController.getInventory();

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), products);
    }

    @Test
    public void testDuplicates() throws IOException {
        Product product = new Product("test", "testdes", 1.0, 1);

        when(mockInventoryDAO.createProduct(product)).thenThrow(new IllegalArgumentException());

        ResponseEntity<Product> response = inventoryController.createProduct(product);

        assertEquals(response.getStatusCode(), HttpStatus.CONFLICT);
    }

    @Test
    public void testError() throws IOException {
        Product product = new Product("test", "testdes", 1.0, 1);

        when(mockInventoryDAO.createProduct(product)).thenThrow(new IOException());
        when(mockInventoryDAO.updateProduct(product)).thenThrow(new IOException());
        when(mockInventoryDAO.getInventory()).thenThrow(new IOException());

        ResponseEntity<Product> response = inventoryController.createProduct(product);
        ResponseEntity<Product> response2 = inventoryController.updateProduct(product);
        ResponseEntity<Product[]> response3 = inventoryController.getInventory();

        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response2.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response3.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
