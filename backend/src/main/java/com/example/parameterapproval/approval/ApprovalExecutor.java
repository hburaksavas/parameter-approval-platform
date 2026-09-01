package com.example.parameterapproval.approval;

import com.example.parameterapproval.change.ChangeHistory;
import com.example.parameterapproval.change.ChangeHistoryRepository;
import com.example.parameterapproval.change.ChangeItem;
import com.example.parameterapproval.change.ChangeRequest;
import com.example.parameterapproval.change.ChangeRequestRepository;
import com.example.parameterapproval.change.ChangeRequestStatus;
import com.example.parameterapproval.change.OperationType;
import com.example.parameterapproval.common.BusinessException;
import com.example.parameterapproval.common.NotFoundException;
import com.example.parameterapproval.parameter.EntityAccessService;
import com.example.parameterapproval.security.HeaderUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ApprovalExecutor {

    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() { };

    private final ChangeRequestRepository requestRepository;
    private final ChangeHistoryRepository historyRepository;
    private final EntityAccessService entityAccess;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public ApprovalExecutor(
            ChangeRequestRepository requestRepository,
            ChangeHistoryRepository historyRepository,
            EntityAccessService entityAccess,
            ObjectMapper objectMapper,
            EntityManager entityManager) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.entityAccess = entityAccess;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Transactional
    public void apply(Long id, HeaderUser approver, String note) {
        ChangeRequest request = requestRepository.findForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Talep bulunamadı: " + id));
        assertApprovable(request, approver);

        Map<String, Object> entitiesByReference = new LinkedHashMap<>();
        request.getItems().stream()
                .sorted(Comparator.comparing(ChangeItem::getExecutionOrder).thenComparing(ChangeItem::getId))
                .forEach(item -> applyItem(item, entitiesByReference));

        entityManager.flush();
        request.approve(approver.id(), approver.displayName(), note);
        historyRepository.save(new ChangeHistory(
                request.getId(), "APPROVED", approver.id(), approver.displayName(), note));
    }

    private void applyItem(ChangeItem item, Map<String, Object> entitiesByReference) {
        Map<String, Object> references = entityAccess.resolveReferenceBindings(
                readBindings(item.getReferenceBindingsJson()), entitiesByReference);
        Object affected;
        if (item.getOperationType() == OperationType.CREATE) {
            affected = entityAccess.newEntity(item.getResourceCode(), readTree(item.getNewValueJson()), references);
        } else if (item.getOperationType() == OperationType.UPDATE) {
            affected = entityAccess.updateEntity(
                    item.getResourceCode(), item.getRecordId(), readTree(item.getNewValueJson()),
                    item.getExpectedVersion(), references);
        } else {
            affected = entityAccess.deleteEntity(
                    item.getResourceCode(), item.getRecordId(), item.getExpectedVersion());
        }
        if (item.getClientReference() != null) {
            entitiesByReference.put(item.getClientReference(), affected);
        }
        item.markApplied();
    }

    private static void assertApprovable(ChangeRequest request, HeaderUser approver) {
        if (request.getStatus() != ChangeRequestStatus.WAITING_APPROVAL) {
            throw new BusinessException("Talep onay beklemiyor: " + request.getStatus());
        }
        if (request.getCreatedBy().equals(approver.id())) {
            throw new BusinessException("Talebi oluşturan kullanıcı aynı talebi onaylayamaz");
        }
    }

    private JsonNode readTree(String json) {
        try {
            return json == null ? null : objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Saklanan JSON okunamadı", ex);
        }
    }

    private Map<String, String> readBindings(String json) {
        try {
            return json == null ? Map.of() : objectMapper.readValue(json, STRING_MAP);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Reference bindings okunamadı", ex);
        }
    }
}

