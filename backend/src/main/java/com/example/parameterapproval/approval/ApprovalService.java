package com.example.parameterapproval.approval;

import com.example.parameterapproval.change.ChangeHistory;
import com.example.parameterapproval.change.ChangeHistoryRepository;
import com.example.parameterapproval.change.ChangeRequest;
import com.example.parameterapproval.change.ChangeRequestMapper;
import com.example.parameterapproval.change.ChangeRequestRepository;
import com.example.parameterapproval.change.ChangeRequestResponse;
import com.example.parameterapproval.change.ChangeRequestStatus;
import com.example.parameterapproval.common.BusinessException;
import com.example.parameterapproval.common.NotFoundException;
import com.example.parameterapproval.parameter.ChangeConflictException;
import com.example.parameterapproval.security.CurrentUser;
import com.example.parameterapproval.security.HeaderUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {

    private final ApprovalExecutor executor;
    private final ConflictMarker conflictMarker;
    private final ChangeRequestRepository requestRepository;
    private final ChangeHistoryRepository historyRepository;
    private final ChangeRequestMapper mapper;
    private final CurrentUser currentUser;

    public ApprovalService(
            ApprovalExecutor executor,
            ConflictMarker conflictMarker,
            ChangeRequestRepository requestRepository,
            ChangeHistoryRepository historyRepository,
            ChangeRequestMapper mapper,
            CurrentUser currentUser) {
        this.executor = executor;
        this.conflictMarker = conflictMarker;
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public ChangeRequestResponse approve(Long id, String note) {
        HeaderUser user = currentUser.get();
        try {
            executor.apply(id, user, note);
        } catch (ChangeConflictException ex) {
            conflictMarker.mark(id, user, ex.getMessage());
            throw ex;
        }
        return mapper.toResponse(requestRepository.findDetailedById(id).orElseThrow(), true);
    }

    @Transactional
    public ChangeRequestResponse reject(Long id, String note) {
        HeaderUser user = currentUser.get();
        ChangeRequest request = requestRepository.findForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Talep bulunamadı: " + id));
        if (request.getStatus() != ChangeRequestStatus.WAITING_APPROVAL) {
            throw new BusinessException("Talep onay beklemiyor: " + request.getStatus());
        }
        if (request.getCreatedBy().equals(user.id())) {
            throw new BusinessException("Talebi oluşturan kullanıcı aynı talebi reddedemez");
        }
        if (note == null || note.isBlank()) {
            throw new BusinessException("Red açıklaması zorunludur");
        }
        request.reject(user.id(), user.displayName(), note.trim());
        historyRepository.save(new ChangeHistory(
                request.getId(), "REJECTED", user.id(), user.displayName(), note.trim()));
        return mapper.toResponse(request, true);
    }
}

