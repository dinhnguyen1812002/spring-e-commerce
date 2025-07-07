package com.app.e_commerce.services;


import com.app.e_commerce.DTO.RateMessageDTO;
import com.app.e_commerce.entity.Product;
import com.app.e_commerce.entity.Rate;
import com.app.e_commerce.repository.ProductRepo;
import com.app.e_commerce.repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RateService {

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<Rate> getRatesByProduct(Long productId) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return rateRepository.findByProduct(product);
    }

    @Transactional
    public Rate saveRate(Rate rate) {
        Rate savedRate = rateRepository.save(rate);
        // Broadcast the new rating to the WebSocket topic
        RateMessageDTO message = new RateMessageDTO(
                savedRate.getId(),
                savedRate.getProduct().getId(),
                savedRate.getUser().getUsername(),
                savedRate.getComment(),
                savedRate.getStar(),
               diffForHuman(savedRate.getCreatedAt())
        );
        messagingTemplate.convertAndSend("/topic/ratings/" + savedRate.getProduct().getId(), message);
        return savedRate;
    }

    public double getAverageRating(Long productId) {
        List<Rate> rates = getRatesByProduct(productId);
        if (rates.isEmpty()) {
            return 0;
        }
        double totalRating = rates.stream()
                .mapToDouble(Rate::getStar)
                .sum();
        
        return Math.round((totalRating / rates.size()) * 10.0) / 10.0; // Làm tròn đến 1 chữ số thập phân
    }

     public long getRatingCount(Long productId) {
        return getRatesByProduct(productId).size();
    }

    /**
     * Lấy phân bố rating (số lượng rating cho mỗi sao)
     */
    public int[] getRatingDistribution(Long productId) {
        List<Rate> rates = getRatesByProduct(productId);
        int[] distribution = new int[5]; // 1-5 sao
        
        for (Rate rate : rates) {
            int star = rate.getStar();
            if (star >= 1 && star <= 5) {
                distribution[star - 1]++;
            }
        }
        
        return distribution;
    }
    private String diffForHuman(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + "m";
        } else if (minutes < 1440) {
            return (minutes / 60) + "h";
        } else {
            return (minutes / 1440) + "d";
        }
    }
}