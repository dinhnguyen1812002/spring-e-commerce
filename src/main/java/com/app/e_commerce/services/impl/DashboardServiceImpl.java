package com.app.e_commerce.services.impl;

import com.app.e_commerce.repository.OrderRepository;

import com.app.e_commerce.repository.ProductRepo;
import com.app.e_commerce.repository.UserRepo;
import com.app.e_commerce.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepo productRepository;
    private final UserRepo userRepository;

    public DashboardServiceImpl(OrderRepository orderRepository,
            ProductRepo productRepository,
            UserRepo userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Object[]> findDailyRevenueForLast30Days() {
        return orderRepository.findDailyRevenueForLast30Days();
    }

    @Override
    public List<Object[]> findMonthlyRevenueForCurrentYear() {
        return orderRepository.findMonthlyRevenueForCurrentYear();
    }

    @Override
    public List<Object[]> findQuarterlyRevenueForCurrentYear() {
        return orderRepository.findQuarterlyRevenueForCurrentYear();
    }

    @Override
    public List<Object[]> findYearlyRevenueForLast5Years() {
        return orderRepository.findYearlyRevenueForLast5Years();
    }

    @Override
    public List<Object[]> findDailySalesForLast30Days() {
        return orderRepository.findDailySalesForLast30Days();
    }

    @Override
    public List<Object[]> findMonthlySalesForCurrentYear() {
        return orderRepository.findMonthlySalesForCurrentYear();
    }

    @Override
    public List<Object[]> findQuarterlySalesForCurrentYear() {
        return orderRepository.findQuarterlySalesForCurrentYear();
    }

    @Override
    public List<Object[]> findYearlySalesForLast5Years() {
        return orderRepository.findYearlySalesForLast5Years();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderRepository.getTotalRevenue() != null ? orderRepository.getTotalRevenue() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getMonthlyRevenue(int year, int month) {
        BigDecimal revenue = orderRepository.getMonthlyRevenue(year, month);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public Map<String, Object> getRevenueStatistics(String period) {
        // This method is now implemented in the controller
        throw new UnsupportedOperationException("This method is now implemented in the controller");
    }

    @Override
    public Map<String, Object> getSalesStatistics(String period) {
        // This method is now implemented in the controller
        throw new UnsupportedOperationException("This method is now implemented in the controller");
    }

    @Override
    public Map<String, Object> getDashboardSummary() {
        return Map.of();
    }

}
