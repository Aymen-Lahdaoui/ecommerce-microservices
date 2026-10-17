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

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID : " + id));
    }

    // NOUVEAU : Mise à jour d'un produit
    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = getProductById(id); // Vérifie s'il existe

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());

        return productRepository.save(existingProduct);
    }

    // NOUVEAU : Suppression d'un produit
    public void deleteProduct(Long id) {
        Product product = getProductById(id); // Vérifie s'il existe avant de supprimer
        productRepository.delete(product);
    }
}