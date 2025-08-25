package com.app.e_commerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    // Revenue statistics
    List<Object[]> findDailyRevenueForLast30Days();
    List<Object[]> findMonthlyRevenueForCurrentYear();
    List<Object[]> findQuarterlyRevenueForCurrentYear();
    List<Object[]> findYearlyRevenueForLast5Years();
    
    // Sales statistics
    List<Object[]> findDailySalesForLast30Days();
    List<Object[]> findMonthlySalesForCurrentYear();
    List<Object[]> findQuarterlySalesForCurrentYear();
    List<Object[]> findYearlySalesForLast5Years();
    
    // Summary methods
    BigDecimal getTotalRevenue();
    BigDecimal getMonthlyRevenue(int year, int month);
    
    // Legacy methods (can be removed if not used elsewhere)
    Map<String, Object> getRevenueStatistics(String period);
    Map<String, Object> getSalesStatistics(String period);
    Map<String, Object> getDashboardSummary();
}
