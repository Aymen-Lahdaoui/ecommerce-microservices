package com.ecomerce.product_service.service;

import com.ecomerce.product_service.entity.Product;
import com.ecomerce.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID : " + id));
    }

    public Product updateProduct(String id, Product productDetails) {
        Product existingProduct = getProductById(id);

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStock(productDetails.getStock());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(String id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public void reduceStock(String productId, int quantityToReduce) {
        Product product = getProductById(productId);

        if (product.getStock() < quantityToReduce) {
            throw new RuntimeException("Stock insuffisant pour le produit : " + product.getName()
                    + " (Stock disponible : " + product.getStock() + ", Demandé : " + quantityToReduce + ")");
        }

        product.setStock(product.getStock() - quantityToReduce);
        productRepository.save(product);
    }
}