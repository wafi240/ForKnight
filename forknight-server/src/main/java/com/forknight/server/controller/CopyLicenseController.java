package com.forknight.server.controller;

import com.forknight.common.dto.licensing.CopyLicenseCreateRequest;
import com.forknight.common.dto.licensing.CopyLicenseDecisionRequest;
import com.forknight.common.dto.licensing.CopyLicenseRequestDTO;
import com.forknight.server.security.UserPrincipal;
import com.forknight.server.service.CopyLicenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/licensing")
public class CopyLicenseController {

    private final CopyLicenseService copyLicenseService;

    public CopyLicenseController(CopyLicenseService copyLicenseService) {
        this.copyLicenseService = copyLicenseService;
    }

    @PostMapping("/requests")
    public ResponseEntity<CopyLicenseRequestDTO> requestCopy(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CopyLicenseCreateRequest request
    ) {
        CopyLicenseRequestDTO dto = copyLicenseService.createRequest(userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/requests/{id}/decision")
    public ResponseEntity<CopyLicenseRequestDTO> decideRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody CopyLicenseDecisionRequest decision
    ) {
        CopyLicenseRequestDTO dto = copyLicenseService.decideRequest(userPrincipal.id(), id, decision);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<CopyLicenseRequestDTO>> getIncomingRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<CopyLicenseRequestDTO> requests = copyLicenseService.getIncomingRequestsForOwner(userPrincipal.id());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<CopyLicenseRequestDTO>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<CopyLicenseRequestDTO> requests = copyLicenseService.getMySubmittedRequests(userPrincipal.id());
        return ResponseEntity.ok(requests);
    }
}
