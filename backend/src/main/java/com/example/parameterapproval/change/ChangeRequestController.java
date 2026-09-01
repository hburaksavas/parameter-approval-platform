package com.example.parameterapproval.change;

import com.example.parameterapproval.approval.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/change-requests")
public class ChangeRequestController {

    private final ChangeRequestService changeRequestService;
    private final ApprovalService approvalService;

    public ChangeRequestController(ChangeRequestService changeRequestService, ApprovalService approvalService) {
        this.changeRequestService = changeRequestService;
        this.approvalService = approvalService;
    }

    @PostMapping
    ChangeRequestResponse create(@Valid @RequestBody CreateChangeRequestCommand command) {
        return changeRequestService.create(command);
    }

    @GetMapping
    Page<ChangeRequestResponse> list(
            @RequestParam(required = false) ChangeRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return changeRequestService.list(status, page, size);
    }

    @GetMapping("/{id}")
    ChangeRequestResponse detail(@PathVariable Long id) {
        return changeRequestService.detail(id);
    }

    @PostMapping("/{id}/approve")
    ChangeRequestResponse approve(@PathVariable Long id, @Valid @RequestBody DecisionCommand command) {
        return approvalService.approve(id, command.note());
    }

    @PostMapping("/{id}/reject")
    ChangeRequestResponse reject(@PathVariable Long id, @Valid @RequestBody DecisionCommand command) {
        return approvalService.reject(id, command.note());
    }

    @PostMapping("/{id}/withdraw")
    ChangeRequestResponse withdraw(@PathVariable Long id) {
        return changeRequestService.withdraw(id);
    }
}

