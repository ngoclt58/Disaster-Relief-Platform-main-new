package com.relief.config;

import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.repository.NeedsRequestRepository;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Data seeder to populate database with sample needs requests for testing
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final NeedsRequestRepository needsRequestRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (needsRequestRepository.count() > 0) {
            log.info("Database already contains needs requests. Skipping seed data.");
            return;
        }

        log.info("Starting to seed needs requests data...");

        // Get or create a default user for seeding
        Optional<User> defaultUser = userRepository.findByEmail("admin@relief.local");
        if (defaultUser.isEmpty()) {
            log.warn("Default admin user not found. Cannot seed needs requests.");
            return;
        }

        User user = defaultUser.get();

        // Sample needs requests data for the last 7 days
        // type: food, water, medical, evacuation, sos, other
        // status: new, assigned, in_progress, completed, cancelled
        List<NeedRequestData> sampleData = Arrays.asList(
            new NeedRequestData("food", "Emergency food assistance needed for family of 4", 3, 0),
            new NeedRequestData("medical", "Need first aid kit and basic medications", 4, 1),
            new NeedRequestData("water", "Urgent need for clean drinking water", 5, 2),
            new NeedRequestData("other", "Temporary shelter needed due to flooding", 4, 3),
            new NeedRequestData("other", "Clothes for children and adults", 2, 4),
            new NeedRequestData("other", "Warm blankets for cold weather", 3, 5),
            new NeedRequestData("food", "Additional food assistance", 3, 6),
            new NeedRequestData("medical", "Need medical checkup", 4, 0),
            new NeedRequestData("water", "Clean water for drinking and cooking", 3, 1),
            new NeedRequestData("other", "Safe place to stay", 5, 2),
            new NeedRequestData("food", "Food for elderly person", 2, 3),
            new NeedRequestData("medical", "Prescription medications", 4, 4),
            new NeedRequestData("water", "Bottled water needed", 3, 5),
            new NeedRequestData("other", "Winter clothing for family", 3, 6),
            new NeedRequestData("food", "Baby food and formula", 2, 0),
            new NeedRequestData("other", "Emergency shelter for displaced family", 5, 1),
            new NeedRequestData("medical", "Emergency medical care", 5, 2),
            new NeedRequestData("water", "Water purification tablets", 2, 3),
            new NeedRequestData("food", "Non-perishable food items", 3, 4),
            new NeedRequestData("other", "Shoes and socks", 2, 5),
            new NeedRequestData("other", "Temporary housing assistance", 4, 6),
            new NeedRequestData("food", "Meals for community center", 3, 0),
            new NeedRequestData("medical", "Bandages and antiseptics", 3, 1),
            new NeedRequestData("water", "Large water containers", 2, 2),
            new NeedRequestData("food", "Canned goods and rice", 3, 3),
            new NeedRequestData("other", "Tent for temporary shelter", 4, 4),
            new NeedRequestData("medical", "Doctor consultation needed", 4, 5),
            new NeedRequestData("other", "Warm jackets and pants", 3, 6),
            new NeedRequestData("food", "Breakfast items", 2, 0),
            new NeedRequestData("water", "Emergency water supply", 4, 1),
            new NeedRequestData("other", "Safe accommodation", 5, 2),
            new NeedRequestData("food", "Lunch and dinner supplies", 3, 3),
            new NeedRequestData("medical", "Pain relief medications", 3, 4),
            new NeedRequestData("other", "Underwear and basic clothing", 2, 5),
            new NeedRequestData("water", "Drinking water for 50 people", 4, 6),
            new NeedRequestData("food", "Snacks and beverages", 2, 0),
            new NeedRequestData("other", "Emergency evacuation center", 5, 1),
            new NeedRequestData("medical", "Ambulance service needed", 5, 2),
            new NeedRequestData("food", "Vegetables and fruits", 3, 3),
            new NeedRequestData("water", "Water for cooking and cleaning", 3, 4),
            new NeedRequestData("other", "Children's clothing", 2, 5),
            new NeedRequestData("food", "Protein sources", 3, 6),
            new NeedRequestData("other", "Long-term housing solution", 4, 0),
            new NeedRequestData("medical", "Vitamins and supplements", 2, 1),
            new NeedRequestData("water", "Portable water filters", 3, 2),
            new NeedRequestData("food", "Ready-to-eat meals", 3, 3),
            new NeedRequestData("other", "Adult clothing sizes", 3, 4),
            new NeedRequestData("other", "Temporary housing", 4, 5),
            new NeedRequestData("medical", "Mental health support", 4, 6)
        );

        int created = 0;
        LocalDateTime now = LocalDateTime.now();

        // Create needs requests distributed over the last 7 days
        for (int i = 0; i < sampleData.size(); i++) {
            NeedRequestData data = sampleData.get(i);
            
            // Distribute requests over the last 7 days
            int daysAgo = data.daysAgo;
            LocalDateTime createdAt = now.minusDays(daysAgo)
                .minusHours(random.nextInt(12)) // Random hour within the day
                .minusMinutes(random.nextInt(60)); // Random minute

            // Random coordinates (Vietnam area: 8-23 N, 102-110 E)
            double latitude = 10.5 + (random.nextDouble() * 12); // 10.5 to 22.5
            double longitude = 102.0 + (random.nextDouble() * 8); // 102 to 110
            Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

            // Random status distribution: new, assigned, in_progress, completed, cancelled
            String status;
            int statusRand = random.nextInt(100);
            if (statusRand < 30) {
                status = "new";
            } else if (statusRand < 50) {
                status = "assigned";
            } else if (statusRand < 75) {
                status = "in_progress";
            } else if (statusRand < 95) {
                status = "completed";
            } else {
                status = "cancelled";
            }

            // Generate address
            String address = String.format("Address %d, District %d, City", 
                random.nextInt(100) + 1, random.nextInt(20) + 1);

            try {
                NeedsRequest needsRequest = NeedsRequest.builder()
                    .type(data.type)
                    .notes(data.notes)
                    .severity(data.severity)
                    .status(status)
                    .source("app")
                    .geomPoint(location)
                    .address(address)
                    .createdBy(user)
                    .createdAt(createdAt)
                    .updatedAt(createdAt)
                    .build();

                needsRequestRepository.save(needsRequest);
                created++;
            } catch (Exception e) {
                log.error("Error creating needs request: {} - {}", data.type, data.notes, e);
            }
        }

        log.info("Successfully seeded {} needs requests", created);
    }

    private static class NeedRequestData {
        String type;
        String notes;
        int severity;
        int daysAgo;

        NeedRequestData(String type, String notes, int severity, int daysAgo) {
            this.type = type;
            this.notes = notes;
            this.severity = severity;
            this.daysAgo = daysAgo;
        }
    }
}

