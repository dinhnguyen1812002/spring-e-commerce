package com.app.e_commerce.controller;

import com.app.e_commerce.entity.Traffic;
import com.app.e_commerce.services.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/traffic")
public class TrafficController {

    @Autowired
    private TrafficService trafficService;
    @Autowired
    private SimpMessagingTemplate template;

    // Show all visit statistics
    @GetMapping("/stats")
    public String showVisitStats(Model model) {
        List<Traffic> visits = trafficService.getAllTraffic();
        model.addAttribute("visits", visits);
        return "traffic/traffic"; // Refers to the Thymeleaf template
    }

    // Show visit statistics for a specific date
    @GetMapping("/stats/date")
    public String showVisitsByDate(@RequestParam("date") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date,
            Model model) {
        trafficService.getVisitByDay(date).ifPresent(visit -> model.addAttribute("visit", visit));
        return "traffic/traffic";
    }

    // public void sendTrafficUpdate() {
    // LocalDate today = LocalDate.now();
    // Long visitCount = trafficService.getVisitsByDate(today)
    // .map(DailyVisit::getVisitCount)
    // .orElse(0L);
    // template.convertAndSend("/topic/traffic", visitCount); // Send to all
    // subscribers
    // }

}
