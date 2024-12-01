package com.app.e_commerce.services;

import com.app.e_commerce.data.RevenueDataPoint;
import com.app.e_commerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RevenueService {
    @Autowired
    private OrderRepository orderRepository;

    public BigDecimal calculateRevenueByDay(LocalDate date) {
        return orderRepository
                .sumTotalAmountByOrderDate(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
    public BigDecimal calculateRevenueByMonth(int month, int year){
        LocalDate startMonth = LocalDate.of(year, month, 1);
        LocalDate endMonth = startMonth.plusMonths(1);

        return orderRepository
                .sumTotalAmountByOrderDate(startMonth.atStartOfDay(), endMonth.atStartOfDay());
    }
    public BigDecimal calculateRevenueByYear(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = startDate.plusYears(1);
        return orderRepository.sumTotalAmountByOrderDate(startDate.atStartOfDay(), endDate.atStartOfDay());
    }

    public List<RevenueDataPoint> getDailyRevenueForMonth(int month, int year) {
        List<RevenueDataPoint> revenueDataPoints = new ArrayList<>();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        // Loop through each day in the month and get the revenue for that day
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            BigDecimal revenue = orderRepository.sumTotalAmountByOrderDate(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            revenueDataPoints.add(new RevenueDataPoint(date.toString(), revenue != null ? revenue : BigDecimal.ZERO));
        }

        return revenueDataPoints;
    }

    // Get monthly revenue for each month in the specified year
    public List<RevenueDataPoint> getMonthlyRevenueForYear(int year) {
        List<RevenueDataPoint> revenueDataPoints = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1);
            BigDecimal revenue = orderRepository.sumTotalAmountByOrderDate(startDate.atStartOfDay(), endDate.atStartOfDay());
            String monthName = startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            revenueDataPoints.add(new RevenueDataPoint(monthName, revenue != null ? revenue : BigDecimal.ZERO));
        }

        return revenueDataPoints;
    }
    public List<RevenueDataPoint> getDailyRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<RevenueDataPoint> revenueDataPoints = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            BigDecimal revenue = orderRepository.sumTotalAmountByOrderDate(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            revenueDataPoints.add(new RevenueDataPoint(date.toString(), revenue != null ? revenue : BigDecimal.ZERO));
        }
        return revenueDataPoints;
    }

    public List<RevenueDataPoint> getMonthlyRevenueBetweenYears(int startYear, int endYear) {
        List<RevenueDataPoint> revenueDataPoints = new ArrayList<>();
        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 12; month++) {
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.plusMonths(1);
                BigDecimal revenue = orderRepository.sumTotalAmountByOrderDate(startDate.atStartOfDay(), endDate.atStartOfDay());
                String monthYear = startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;

                revenueDataPoints.add(new RevenueDataPoint(monthYear, revenue != null ? revenue : BigDecimal.ZERO));
            }
        }
        return revenueDataPoints;
    }
}
