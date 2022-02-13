package com.estore.api.estoreapi.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.estore.api.estoreapi.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryFileDAO implements InventoryDAO {

    /**
     * The current inventory.
     */
    private Map<String, Product> inventory;

    /**
     * The file name of the inventory file.
     */
    private String filename;

    /**
     * The object mapper.
     */

    private ObjectMapper objectMapper;

    /**
     * The next id to assign to a product.
     */
    private static int nextId;

    public InventoryFileDAO(@Value("${inventory.filename}") String filename, ObjectMapper objectMapper)
            throws IOException {
        this.filename = filename;
        this.objectMapper = objectMapper;
        loadInventory();
    }

    private synchronized static int nextId() {
        int id = nextId;
        nextId++;
        return id;
    }

    private ArrayList<Product> getInventoryArray() {
        return new ArrayList<>(inventory.values());
    }

    @Override
    public Product createProduct(Product product) throws IOException,
            IllegalArgumentException {
        synchronized (inventory) {
            Product newProduct = new Product(nextId(), product.getName(),
                    product.getDescription(), product.getPrice(),
                    product.getQuantity());

            if (inventory.values().stream().anyMatch(p -> p.getName().equals(newProduct.getName()))) {
                throw new IllegalArgumentException("Product with name " +
                        newProduct.getName() + " already exists");
            }
            inventory.put(newProduct.getName(), newProduct);
            saveInventory();
            return newProduct;
        }
    }

    @Override
    public Product[] searchProducts(String searchTerms) {
        if (searchTerms.length() == 0)
            return new Product[0];

        ArrayList<Product> products = new ArrayList<>();
        for (Product product : inventory.values()) {
            if (product.getName().toLowerCase().contains(searchTerms.toLowerCase())) {
                products.add(product);
            }
        }

        return products.toArray(new Product[0]);
    }

    @Override
    public Product getProduct(String name) {
        synchronized (inventory) {
            Product tempProduct = inventory.get(name);
            return tempProduct;
        }
    }

    private void saveInventory() throws IOException {
        objectMapper.writeValue(new File(filename), getInventoryArray());
    }

    private void loadInventory() throws IOException {
        inventory = new TreeMap<>();
        nextId = 0;
        Product[] inventoryArray = objectMapper.readValue(new File(filename), Product[].class);
        for (Product product : inventoryArray) {
            inventory.put(product.getName(), product);
            if (product.getId() > nextId) {
                nextId = product.getId();
            }
        }
        nextId++;
    }

}
