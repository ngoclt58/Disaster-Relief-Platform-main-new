package com.relief.service;

import com.relief.dto.CreateNeedRequest;
import com.relief.dto.ResidentProfileRequest;
import com.relief.entity.Household;
import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.entity.Ward;
import com.relief.repository.HouseholdRepository;
import com.relief.repository.NeedsRequestRepository;
import com.relief.realtime.RealtimeBroadcaster;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ResidentService {

    private final HouseholdRepository householdRepository;
    private final NeedsRequestRepository needsRequestRepository;
    private final RealtimeBroadcaster broadcaster;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public Household upsertResidentProfile(User resident, ResidentProfileRequest request, Ward ward) {
        Household household = householdRepository.findByResidentUser_Id(resident.getId()).orElseGet(() -> {
            Household h = new Household();
            h.setResidentUser(resident);
            return h;
        });

        household.setAddress(request.getAddress());
        household.setWard(ward);
        household.setHouseholdSize(request.getHouseholdSize());
        if (request.getLat() != null && request.getLng() != null) {
            Point p = geometryFactory.createPoint(new Coordinate(request.getLng(), request.getLat()));
            household.setGeomPoint(p);
        }
        return householdRepository.save(household);
    }

    public NeedsRequest createNeed(User createdBy, Household household, CreateNeedRequest request) {
        NeedsRequest need = new NeedsRequest();
        need.setHousehold(household);
        need.setType(request.getType());
        need.setSeverity(request.getSeverity());
        need.setNotes(request.getNotes());
        need.setCreatedBy(createdBy);
        if (request.getLat() != null && request.getLng() != null) {
            Point p = geometryFactory.createPoint(new Coordinate(request.getLng(), request.getLat()));
            need.setGeomPoint(p);
        } else if (household.getGeomPoint() != null) {
            need.setGeomPoint(household.getGeomPoint());
        }
        NeedsRequest saved = needsRequestRepository.save(need);
        broadcaster.broadcast("needs.created", saved.getId());
        return saved;
    }

    public List<NeedsRequest> getMyNeeds(UUID userId) {
        return needsRequestRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
    }
}


