package com.distributed.inventory.infrastructure.persistence.adapter;

import com.distributed.inventory.domain.model.Product;
import com.distributed.inventory.domain.repository.ProductRepositoryPort;
import com.distributed.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import com.distributed.inventory.infrastructure.persistence.repository.SpringDataProductRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository productRepository;

    public ProductPersistenceAdapter(SpringDataProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity(product.getId(), product.getStock());
        ProductJpaEntity saved = productRepository.save(entity);
        return new Product(saved.getId(), saved.getStock());
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id)
                .map(entity -> new Product(entity.getId(), entity.getStock()));
    }

    @Override
    public boolean existsById(UUID id) {
        return productRepository.existsById(id);
    }

    @Override
    public int deductStockAtomically(UUID productId, int quantity) {
        return productRepository.deductStockAtomically(productId, quantity);
    }

    @Override
    public int restoreStockAtomically(UUID productId, int quantity) {
        return productRepository.restoreStockAtomically(productId, quantity);
    }
}
