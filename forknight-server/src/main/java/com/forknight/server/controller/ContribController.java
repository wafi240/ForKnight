package com.forknight.server.controller;

import com.forknight.common.dto.contrib.CollaboratorDTO;
import com.forknight.common.dto.contrib.ContribCreateRequest;
import com.forknight.common.dto.contrib.ContribDecisionRequest;
import com.forknight.common.dto.contrib.ContribRequestDTO;
import com.forknight.server.security.UserPrincipal;
import com.forknight.server.service.ContribService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contrib")
public class ContribController {

    private final ContribService contribService;

    public ContribController(ContribService contribService) {
        this.contribService = contribService;
    }

    @PostMapping("/requests")
    public ResponseEntity<ContribRequestDTO> requestContribution(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ContribCreateRequest request
    ) {
        ContribRequestDTO dto = contribService.createRequest(userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/requests/{id}/decision")
    public ResponseEntity<ContribRequestDTO> decideRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody ContribDecisionRequest decision
    ) {
        ContribRequestDTO dto = contribService.decideRequest(userPrincipal.id(), id, decision);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<ContribRequestDTO>> getIncomingRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<ContribRequestDTO> requests = contribService.getIncomingRequestsForOwner(userPrincipal.id());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<ContribRequestDTO>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<ContribRequestDTO> requests = contribService.getMySubmittedRequests(userPrincipal.id());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/repo/{repoId}/collaborators")
    public ResponseEntity<List<CollaboratorDTO>> getCollaborators(@PathVariable Long repoId) {
        List<CollaboratorDTO> collaborators = contribService.getCollaboratorsForRepo(repoId);
        return ResponseEntity.ok(collaborators);
    }
}
