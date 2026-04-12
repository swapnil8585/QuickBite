package com.quickbite.restaurantservice.service;

import com.quickbite.restaurantservice.dto.*;
import com.quickbite.restaurantservice.entity.*;
import com.quickbite.restaurantservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public Restaurant createRestaurant(Long ownerId, RestaurantRequest req) {
        Restaurant r = Restaurant.builder()
                .ownerId(ownerId).name(req.getName())
                .description(req.getDescription()).address(req.getAddress())
                .phone(req.getPhone()).cuisineType(req.getCuisineType())
                .build();
        return restaurantRepository.save(r);
    }

    @Cacheable(value = "restaurants", key = "'all-open'")
    public List<Restaurant> getOpenRestaurants() {
        return restaurantRepository.findByOpenTrue();
    }

    @Cacheable(value = "restaurants", key = "#id")
    public Restaurant getById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
    }

    @Transactional
    @CacheEvict(value = "restaurants", key = "#id")
    public Restaurant toggleOpen(Long id, boolean open) {
        Restaurant r = getById(id);
        r.setOpen(open);
        return restaurantRepository.save(r);
    }

    // --- Menu Items ---

    @Transactional
    @CacheEvict(value = "menus", key = "#restaurantId")
    public MenuItem addMenuItem(Long restaurantId, MenuItemRequest req) {
        Restaurant r = getById(restaurantId);
        MenuItem item = MenuItem.builder()
                .restaurant(r).name(req.getName())
                .description(req.getDescription()).price(req.getPrice())
                .category(req.getCategory()).available(req.isAvailable())
                .build();
        return menuItemRepository.save(item);
    }

    @Cacheable(value = "menus", key = "#restaurantId")
    public List<MenuItem> getMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    @Transactional
    @CacheEvict(value = "menus", key = "#restaurantId")
    public void deleteMenuItem(Long restaurantId, Long itemId) {
        menuItemRepository.deleteById(itemId);
    }
}