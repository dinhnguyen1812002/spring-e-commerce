package com.app.e_commerce.services;

import com.app.e_commerce.entity.Traffic;
import com.app.e_commerce.repository.TrafficRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrafficService {

    @Autowired
    private TrafficRepo trafficRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void trackVisit() {
        LocalDate today = LocalDate.now();

        // update count, nếu có record
        int updated = trafficRepo.incrementCount(today);

        if (updated == 0) {
            // chưa có record => insert mới
            Traffic traffic = new Traffic(today, 1L);
            trafficRepo.save(traffic);
        }

        // gửi realtime update
        messagingTemplate.convertAndSend("/topic/trafficUpdates", getAllTraffic());
    }

    public List<Traffic> getAllTraffic() {
        return trafficRepo.findAll();
    }

    public Optional<Traffic> getVisitByDay(LocalDate date) {
        return trafficRepo.findByDate(date);
    }
}
