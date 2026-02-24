package com.app.e_commerce.controller;

import com.app.e_commerce.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam(defaultValue = "month") String period) {
        
        Map<String, Object> response = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        
        switch (period.toLowerCase()) {
            case "day":
                List<Object[]> dailyRevenue = dashboardService.findDailyRevenueForLast30Days();
                dailyRevenue.forEach(item -> {
                    labels.add(String.valueOf(item[0]));
                    data.add(new BigDecimal(item[1].toString()));
                });
                break;
                
            case "month":
                // Get current year
                int currentYear = LocalDate.now().getYear();
                
                // Get monthly data for current year
                List<Object[]> monthlyRevenue = dashboardService.findMonthlyRevenueForCurrentYear();
                Map<Integer, BigDecimal> monthToRevenue = monthlyRevenue.stream()
                        .collect(Collectors.toMap(
                                item -> ((Number) item[0]).intValue(),
                                item -> new BigDecimal(item[1].toString())
                        ));
                
                // Fill in all months, even those with no data
                for (int month = 1; month <= 12; month++) {
                    String monthName = YearMonth.of(currentYear, month).getMonth().toString();
                    labels.add(monthName.substring(0, 1) + monthName.substring(1).toLowerCase());
                    data.add(monthToRevenue.getOrDefault(month, BigDecimal.ZERO));
                }
                break;
                
            case "quarter":
                List<Object[]> quarterlyRevenue = dashboardService.findQuarterlyRevenueForCurrentYear();
                Map<Integer, BigDecimal> quarterToRevenue = quarterlyRevenue.stream()
                        .collect(Collectors.toMap(
                                item -> ((Number) item[0]).intValue(),
                                item -> new BigDecimal(item[1].toString())
                        ));
                
                // Fill in all quarters
                for (int q = 1; q <= 4; q++) {
                    labels.add("Q" + q);
                    data.add(quarterToRevenue.getOrDefault(q, BigDecimal.ZERO));
                }
                break;
                
            case "year":
                List<Object[]> yearlyRevenue = dashboardService.findYearlyRevenueForLast5Years();
                yearlyRevenue.forEach(item -> {
                    labels.add(String.valueOf(item[0]));
                    data.add(new BigDecimal(item[1].toString()));
                });
                break;
                
            default:
                throw new IllegalArgumentException("Invalid period. Must be one of: day, month, quarter, year");
        }
        
        response.put("labels", labels);
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesStatistics(
            @RequestParam(defaultValue = "month") String period) {
        
        Map<String, Object> response = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        
        switch (period.toLowerCase()) {
            case "day":
                List<Object[]> dailySales = dashboardService.findDailySalesForLast30Days();
                dailySales.forEach(item -> {
                    labels.add(String.valueOf(item[0]));
                    data.add(((Number) item[1]).longValue());
                });
                break;
                
            case "month":
                // Get current year
                int currentYear = LocalDate.now().getYear();
                
                // Get monthly data for current year
                List<Object[]> monthlySales = dashboardService.findMonthlySalesForCurrentYear();
                Map<Integer, Long> monthToSales = monthlySales.stream()
                        .collect(Collectors.toMap(
                                item -> ((Number) item[0]).intValue(),
                                item -> ((Number) item[1]).longValue()
                        ));
                
                // Fill in all months, even those with no data
                for (int month = 1; month <= 12; month++) {
                    String monthName = YearMonth.of(currentYear, month).getMonth().toString();
                    labels.add(monthName.substring(0, 1) + monthName.substring(1).toLowerCase());
                    data.add(monthToSales.getOrDefault(month, 0L));
                }
                break;
                
            case "quarter":
                List<Object[]> quarterlySales = dashboardService.findQuarterlySalesForCurrentYear();
                Map<Integer, Long> quarterToSales = quarterlySales.stream()
                        .collect(Collectors.toMap(
                                item -> ((Number) item[0]).intValue(),
                                item -> ((Number) item[1]).longValue()
                        ));
                
                // Fill in all quarters
                for (int q = 1; q <= 4; q++) {
                    labels.add("Q" + q);
                    data.add(quarterToSales.getOrDefault(q, 0L));
                }
                break;
                
            case "year":
                List<Object[]> yearlySales = dashboardService.findYearlySalesForLast5Years();
                yearlySales.forEach(item -> {
                    labels.add(String.valueOf(item[0]));
                    data.add(((Number) item[1]).longValue());
                });
                break;
                
            default:
                throw new IllegalArgumentException("Invalid period. Must be one of: day, month, quarter, year");
        }
        
        response.put("labels", labels);
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Get total revenue
        BigDecimal totalRevenue = dashboardService.getTotalRevenue();
        
        // Get current month revenue
        LocalDate now = LocalDate.now();
        BigDecimal currentMonthRevenue = dashboardService.getMonthlyRevenue(now.getYear(), now.getMonthValue());
        
        // Calculate growth from last month
        LocalDate lastMonth = now.minusMonths(1);
        BigDecimal lastMonthRevenue = dashboardService.getMonthlyRevenue(lastMonth.getYear(), lastMonth.getMonthValue());
        
        double growthPercentage = 0.0;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = currentMonthRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else if (currentMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = 100.0;
        }
        
        summary.put("totalRevenue", totalRevenue);
        summary.put("currentMonthRevenue", currentMonthRevenue);
        summary.put("revenueGrowth", growthPercentage);
        
        return ResponseEntity.ok(summary);
    }
}
