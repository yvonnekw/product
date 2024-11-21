package com.auction.product.service;

import com.auction.product.dto.CategoryRequest;
import com.auction.product.dto.CategoryResponse;
import com.auction.product.dto.ProductRequest;
import com.auction.product.model.Category;
import com.auction.product.model.Product;
import com.auction.product.repostory.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;



    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }


    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Category updateCategory(Long categoryId, Category updatedCategory) {
        return categoryRepository.findById(categoryId).map(category -> {
            category.setName(updatedCategory.getName());
            category.setDescription(updatedCategory.getDescription());
            return categoryRepository.save(category);
        }).orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }




}

