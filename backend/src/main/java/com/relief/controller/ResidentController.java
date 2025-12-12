package com.relief.controller;

import com.relief.dto.CreateNeedRequest;
import com.relief.dto.ResidentProfileRequest;
import com.relief.entity.Household;
import com.relief.entity.NeedsRequest;
import com.relief.entity.User;
import com.relief.entity.Ward;
import com.relief.repository.UserRepository;
import com.relief.repository.WardRepository;
import com.relief.security.RequiresPermission;
import com.relief.security.Permission;
import com.relief.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resident")
@RequiredArgsConstructor
@Tag(name = "Resident", description = "Resident profile and needs endpoints")
public class ResidentController {

    private final ResidentService residentService;
    private final UserRepository userRepository;
    private final WardRepository wardRepository;

    @PostMapping("/profile")
    @Operation(summary = "Create or update resident profile")
    @RequiresPermission(Permission.USER_WRITE)
    public ResponseEntity<Household> upsertProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ResidentProfileRequest request
    ) {
        User user = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        Ward ward = null;
        if (request.getWardId() != null && !request.getWardId().isBlank()) {
            try {
                ward = wardRepository.findById(java.util.UUID.fromString(request.getWardId())).orElse(null);
            } catch (IllegalArgumentException ignore) {
                ward = null;
            }
        }
        Household household = residentService.upsertResidentProfile(user, request, ward);
        return ResponseEntity.ok(household);
    }

    @GetMapping("/needs")
    @Operation(summary = "Get my needs requests")
    @RequiresPermission(Permission.NEEDS_READ)
    public ResponseEntity<List<NeedsRequest>> getMyNeeds(
            @AuthenticationPrincipal UserDetails principal
    ) {
        User user = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        List<NeedsRequest> needs = residentService.getMyNeeds(user.getId());
        return ResponseEntity.ok(needs);
    }

    @PostMapping("/needs")
    @Operation(summary = "Create a new needs request")
    @RequiresPermission(Permission.NEEDS_WRITE)
    public ResponseEntity<NeedsRequest> createNeed(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateNeedRequest request
    ) {
        User user = userRepository.findByEmail(principal.getUsername()).orElseGet(() ->
                userRepository.findByPhone(principal.getUsername()).orElseThrow());
        // Get or create household for the user
        Household household = residentService.upsertResidentProfile(user, new com.relief.dto.ResidentProfileRequest(), null);
        NeedsRequest need = residentService.createNeed(user, household, request);
        return ResponseEntity.ok(need);
    }
}


