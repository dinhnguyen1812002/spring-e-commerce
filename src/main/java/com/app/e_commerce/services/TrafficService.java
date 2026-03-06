package com.app.e_commerce.services;

import com.app.e_commerce.entity.Traffic;
import com.app.e_commerce.repository.TrafficRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrafficService {

    @Autowired
    private TrafficRepo trafficRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Store session start times for calculating session duration
    private final Map<String, LocalDateTime> sessionStartTimes = new ConcurrentHashMap<>();

    // Store page counts per session for bounce rate calculation
    private final Map<String, Integer> sessionPageCounts = new ConcurrentHashMap<>();

    // Store current page for each session
    private final Map<String, String> currentPages = new ConcurrentHashMap<>();

    // Store real-time visitor counts per page
    private final Map<String, Integer> realTimeVisitors = new ConcurrentHashMap<>();

    @Transactional
    public void trackVisit() {
        LocalDate today = LocalDate.now();
        trafficRepo.incrementCountUpsert(today);

        // gửi realtime update
        messagingTemplate.convertAndSend("/topic/trafficUpdates", getAllTraffic());
    }

    /**
     * Track a page view
     * @param request the HTTP request
     * @param session the HTTP session
     */
    @Transactional
    public void trackPageView(HttpServletRequest request, HttpSession session) {
        LocalDate today = LocalDate.now();
        String sessionId = session.getId();
        String page = request.getRequestURI();

        // Get or create traffic record for today
        Traffic traffic = getOrCreateTrafficForToday(today);

        // Increment page views
        traffic.setPageViews(traffic.getPageViews() + 1);

        // Track session start time if this is a new session
        if (!sessionStartTimes.containsKey(sessionId)) {
            sessionStartTimes.put(sessionId, LocalDateTime.now());
            traffic.setSessionCount(traffic.getSessionCount() + 1);
        }

        // Update page count for this session
        sessionPageCounts.put(sessionId, sessionPageCounts.getOrDefault(sessionId, 0) + 1);

        // Update current page for this session
        String previousPage = currentPages.put(sessionId, page);

        // Update real-time visitors
        if (previousPage != null) {
            realTimeVisitors.put(previousPage, realTimeVisitors.getOrDefault(previousPage, 1) - 1);
        }
        realTimeVisitors.put(page, realTimeVisitors.getOrDefault(page, 0) + 1);

        // Track traffic source if this is the first page view in the session
        if (sessionPageCounts.getOrDefault(sessionId, 0) == 1) {
            trackTrafficSource(request, traffic);
        }

        // Save traffic record
        trafficRepo.save(traffic);

        // Send real-time update
        sendRealTimeUpdate(traffic);
    }

    /**
     * Track session end (e.g., on session timeout or logout)
     * @param sessionId the session ID
     */
    @Transactional
    public void trackSessionEnd(String sessionId) {
        LocalDate today = LocalDate.now();

        // Get traffic record for today
        Optional<Traffic> trafficOpt = trafficRepo.findByDate(today);
        if (trafficOpt.isEmpty()) {
            return;
        }

        Traffic traffic = trafficOpt.get();

        // Calculate session duration if session start time exists
        LocalDateTime startTime = sessionStartTimes.remove(sessionId);
        if (startTime != null) {
            long durationSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
            traffic.setTotalSessionDuration(traffic.getTotalSessionDuration() + durationSeconds);
        }

        // Check if this was a bounce (single page view)
        Integer pageCount = sessionPageCounts.remove(sessionId);
        if (pageCount != null && pageCount == 1) {
            traffic.setBounceCount(traffic.getBounceCount() + 1);
        }

        // Remove current page
        String currentPage = currentPages.remove(sessionId);
        if (currentPage != null) {
            realTimeVisitors.put(currentPage, realTimeVisitors.getOrDefault(currentPage, 1) - 1);
        }

        // Save traffic record
        trafficRepo.save(traffic);

        // Send real-time update
        sendRealTimeUpdate(traffic);
    }

    /**
     * Track traffic source from request
     * @param request the HTTP request
     * @param traffic the traffic record
     */
    private void trackTrafficSource(HttpServletRequest request, Traffic traffic) {
        String referer = request.getHeader("Referer");
        String source = "direct";

        if (referer != null) {
            if (referer.contains("google") || referer.contains("bing") || referer.contains("yahoo")) {
                source = "search";
            } else if (referer.contains("facebook") || referer.contains("twitter") || referer.contains("instagram")) {
                source = "social";
            } else if (referer.contains("mail") || referer.contains("outlook") || referer.contains("gmail")) {
                source = "email";
            } else {
                source = "referral";
            }
        }

        // Update traffic sources
        Map<String, Integer> sources = parseTrafficSources(traffic.getTrafficSources());
        sources.put(source, sources.getOrDefault(source, 0) + 1);

        try {
            traffic.setTrafficSources(objectMapper.writeValueAsString(sources));
        } catch (JsonProcessingException e) {
            // Log error and continue
            System.err.println("Error serializing traffic sources: " + e.getMessage());
        }
    }

    /**
     * Parse traffic sources from JSON string
     * @param json the JSON string
     * @return map of traffic sources and counts
     */
    private Map<String, Integer> parseTrafficSources(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            // Log error and return empty map
            System.err.println("Error parsing traffic sources: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get or create traffic record for today
     * @param date the date
     * @return the traffic record
     */
    private Traffic getOrCreateTrafficForToday(LocalDate date) {
        trafficRepo.insertIfAbsent(date);
        return trafficRepo.findByDate(date)
                .orElseThrow(() -> new IllegalStateException("Failed to load traffic row for date: " + date));
    }

    /**
     * Send real-time update via WebSocket
     * @param traffic the traffic record
     */
    private void sendRealTimeUpdate(Traffic traffic) {
        // Create a data object with all the metrics
        Map<String, Object> data = new HashMap<>();
        data.put("traffic", traffic);
        data.put("realTimeVisitors", realTimeVisitors);
        data.put("trafficSources", parseTrafficSources(traffic.getTrafficSources()));

        // Send update
        messagingTemplate.convertAndSend("/topic/trafficUpdates", (Object) data);
    }

    /**
     * Get all traffic records
     * @return list of traffic records
     */
    public List<Traffic> getAllTraffic() {
        return trafficRepo.findAll();
    }

    /**
     * Get traffic record for a specific date
     * @param date the date
     * @return optional traffic record
     */
    public Optional<Traffic> getVisitByDay(LocalDate date) {
        return trafficRepo.findByDate(date);
    }

    /**
     * Get real-time visitor count
     * @return total number of real-time visitors
     */
    public int getRealTimeVisitorCount() {
        return realTimeVisitors.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Get real-time visitors per page
     * @return map of pages and visitor counts
     */
    public Map<String, Integer> getRealTimeVisitorsPerPage() {
        return new HashMap<>(realTimeVisitors);
    }

    /**
     * Get traffic sources for a specific date
     * @param date the date
     * @return map of traffic sources and counts
     */
    public Map<String, Integer> getTrafficSourcesForDate(LocalDate date) {
        Optional<Traffic> trafficOpt = trafficRepo.findByDate(date);
        if (trafficOpt.isEmpty()) {
            return new HashMap<>();
        }

        return parseTrafficSources(trafficOpt.get().getTrafficSources());
    }

    /**
     * Get traffic data for admin dashboard
     * @return map of traffic data
     */
    public Map<String, Object> getTrafficDataForDashboard() {
        Map<String, Object> data = new HashMap<>();

        // Get today's traffic
        LocalDate today = LocalDate.now();
        Traffic todayTraffic = getOrCreateTrafficForToday(today);

        // Get yesterday's traffic for comparison
        LocalDate yesterday = today.minusDays(1);
        Optional<Traffic> yesterdayTrafficOpt = trafficRepo.findByDate(yesterday);
        Traffic yesterdayTraffic = yesterdayTrafficOpt.orElse(new Traffic(yesterday, 0L));

        // Calculate growth percentages
        double visitorGrowth = calculateGrowthPercentage(todayTraffic.getCount(), yesterdayTraffic.getCount());
        double pageViewGrowth = calculateGrowthPercentage(todayTraffic.getPageViews(), yesterdayTraffic.getPageViews());
        double bounceRateChange = todayTraffic.getBounceRate() - yesterdayTraffic.getBounceRate();
        double sessionDurationChange = calculateGrowthPercentage(
                todayTraffic.getAvgSessionDuration(), yesterdayTraffic.getAvgSessionDuration());

        // Add metrics to data
        data.put("todayVisitors", todayTraffic.getCount());
        data.put("visitorGrowth", visitorGrowth);
        data.put("todayPageViews", todayTraffic.getPageViews());
        data.put("pageViewGrowth", pageViewGrowth);
        data.put("bounceRate", todayTraffic.getBounceRate());
        data.put("bounceRateChange", bounceRateChange);
        data.put("avgSessionDuration", formatSessionDuration(todayTraffic.getAvgSessionDuration()));
        data.put("sessionDurationChange", sessionDurationChange);
        data.put("realTimeVisitors", getRealTimeVisitorCount());
        data.put("realTimeVisitorsPerPage", getRealTimeVisitorsPerPage());
        data.put("trafficSources", parseTrafficSources(todayTraffic.getTrafficSources()));

        // Get last 7 days of traffic for charts
        List<Traffic> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Traffic traffic = trafficRepo.findByDate(date).orElse(new Traffic(date, 0L));
            last7Days.add(traffic);
        }
        data.put("last7Days", last7Days);

        return data;
    }

    /**
     * Calculate growth percentage
     * @param current current value
     * @param previous previous value
     * @return growth percentage
     */
    private double calculateGrowthPercentage(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    /**
     * Format session duration in seconds to "Xm Ys" format
     * @param seconds duration in seconds
     * @return formatted duration
     */
    private String formatSessionDuration(double seconds) {
        int minutes = (int) (seconds / 60);
        int remainingSeconds = (int) (seconds % 60);
        return minutes + "m " + remainingSeconds + "s";
    }
}
