package com.smartparking.eventprocessor.service.impl;

import com.smartparking.eventprocessor.model.view.Event;
import com.smartparking.eventprocessor.model.view.Spot;
import com.smartparking.eventprocessor.model.view.UnverifiedEvent;
import com.smartparking.eventprocessor.service.DatabaseService;
import com.smartparking.eventprocessor.service.EntityViewService;
import com.smartparking.eventprocessor.service.EventBatchService;
import com.smartparking.eventprocessor.service.ServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventBatchServiceImpl implements EventBatchService {

    @Autowired
    private EntityViewService entityViewService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private DatabaseService databaseService;

    private Queue<Event> events = new ConcurrentLinkedQueue<>();

    @Override
    public void push(Event event) {
        events.add(event);
    }

    @Override
    public Event poll() {
        return events.poll();
    }

    @Override
    public synchronized void send(Collection<? extends Event> events) {
        if (!entityViewService.isInitialized() || !serverService.isServerAvailable()) {
            try {
                entityViewService.update();
                log.info("EntityViewService updated.");
            } catch (IOException ex) {
                this.events.addAll(events);
                log.error("EntityViewService updating failed: " + ex.toString());
            }
        }
        List<Event> verifiedEvents = events.stream()
                .filter(this::verify)
                .collect(Collectors.toList());
        log.info("Unverified events prepared: passed={}, ignored={}.",
                verifiedEvents.size(), events.size() - verifiedEvents.size());
        try {
            databaseService.saveEvents(verifiedEvents);
            log.info("Events sent: count=" + events.size() + ".");
        } catch (DataAccessException ex) {
            this.events.addAll(events);
            log.error("Events does not sent: " + ex.toString());
        } catch (SQLException e) {
            this.events.addAll(events);
            log.error("Events does not sent: " + e.toString());
        }
    }

    private boolean verify(Event event) {
        if (event instanceof UnverifiedEvent) {
            Spot spot = entityViewService.getSpotByNumberAndParkingToken(((UnverifiedEvent) event).getSpotNumber(),
                    ((UnverifiedEvent) event).getParkingToken());
            if (spot != null) {
                event.setSpotId(spot.getId());
                return spot.getParking().getToken().equals(((UnverifiedEvent) event).getParkingToken())
                        && spot.getSpotNumber().equals(((UnverifiedEvent) event).getSpotNumber());
            }
            return false;
        } else {
            return true;
        }
    }
}
