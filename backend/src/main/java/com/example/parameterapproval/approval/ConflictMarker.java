package com.example.parameterapproval.approval;

import com.example.parameterapproval.change.ChangeHistory;
import com.example.parameterapproval.change.ChangeHistoryRepository;
import com.example.parameterapproval.change.ChangeRequest;
import com.example.parameterapproval.change.ChangeRequestRepository;
import com.example.parameterapproval.change.ChangeRequestStatus;
import com.example.parameterapproval.security.HeaderUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConflictMarker {

    private final ChangeRequestRepository requestRepository;
    private final ChangeHistoryRepository historyRepository;

    public ConflictMarker(ChangeRequestRepository requestRepository, ChangeHistoryRepository historyRepository) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mark(Long id, HeaderUser user, String message) {
        ChangeRequest request = requestRepository.findForUpdate(id).orElse(null);
        if (request == null || request.getStatus() != ChangeRequestStatus.WAITING_APPROVAL) return;
        request.markConflict(user.id(), user.displayName(), message);
        historyRepository.save(new ChangeHistory(
                request.getId(), "FAILED_CONFLICT", user.id(), user.displayName(), message));
    }
}

