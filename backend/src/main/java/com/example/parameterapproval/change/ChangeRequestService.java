package com.example.parameterapproval.change;

import com.example.parameterapproval.common.BusinessException;
import com.example.parameterapproval.common.NotFoundException;
import com.example.parameterapproval.parameter.EntityAccessService;
import com.example.parameterapproval.security.CurrentUser;
import com.example.parameterapproval.security.HeaderUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ChangeRequestService {

    private final ChangeRequestRepository requestRepository;
    private final ChangeHistoryRepository historyRepository;
    private final EntityAccessService entityAccess;
    private final ChangeRequestMapper mapper;
    private final ObjectMapper objectMapper;
    private final CurrentUser currentUser;

    public ChangeRequestService(
            ChangeRequestRepository requestRepository,
            ChangeHistoryRepository historyRepository,
            EntityAccessService entityAccess,
            ChangeRequestMapper mapper,
            ObjectMapper objectMapper,
            CurrentUser currentUser) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.entityAccess = entityAccess;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.currentUser = currentUser;
    }

    @Transactional
    public ChangeRequestResponse create(CreateChangeRequestCommand command) {
        HeaderUser user = currentUser.get();
        ChangeRequest request = new ChangeRequest(
                nextRequestNo(), command.title().trim(), trimToNull(command.description()), user.id(), user.displayName());
        Set<String> clientReferences = new HashSet<>();

        for (int index = 0; index < command.items().size(); index++) {
            ChangeItemCommand item = command.items().get(index);
            String resourceCode = item.resourceCode().trim().toUpperCase();
            JsonNode oldValue = null;
            Long expectedVersion = null;
            JsonNode newValue = item.newValue();

            switch (item.operation()) {
                case CREATE -> {
                    if (newValue == null) throw new BusinessException("CREATE için newValue zorunludur");
                    newValue = entityAccess.normalizePayload(
                            resourceCode, newValue, EntityAccessService.OperationMode.CREATE);
                    if (item.clientReference() != null && !clientReferences.add(item.clientReference())) {
                        throw new BusinessException("Tekrarlanan clientReference: " + item.clientReference());
                    }
                }
                case UPDATE -> {
                    requireRecordId(item);
                    if (newValue == null) throw new BusinessException("UPDATE için newValue zorunludur");
                    Object entity = entityAccess.findRequired(resourceCode, item.recordId());
                    oldValue = entityAccess.snapshot(resourceCode, entity, false);
                    expectedVersion = entityAccess.version(resourceCode, entity);
                    newValue = entityAccess.normalizePayload(
                            resourceCode, newValue, EntityAccessService.OperationMode.UPDATE);
                }
                case DELETE -> {
                    requireRecordId(item);
                    Object entity = entityAccess.findRequired(resourceCode, item.recordId());
                    oldValue = entityAccess.snapshot(resourceCode, entity, false);
                    expectedVersion = entityAccess.version(resourceCode, entity);
                    newValue = null;
                }
            }

            request.addItem(new ChangeItem(
                    resourceCode, item.operation(), trimToNull(item.recordId()), trimToNull(item.clientReference()),
                    write(item.referenceBindings() == null ? Map.of() : item.referenceBindings()),
                    writeNullable(oldValue), writeNullable(newValue), expectedVersion,
                    item.executionOrder() == null ? index * 10 : item.executionOrder()));
        }

        validateReferences(command, clientReferences);
        requestRepository.saveAndFlush(request);
        historyRepository.save(new ChangeHistory(
                request.getId(), "SUBMITTED", user.id(), user.displayName(), request.getDescription()));
        return mapper.toResponse(request, true);
    }

    @Transactional(readOnly = true)
    public ChangeRequestResponse detail(Long id) {
        return mapper.toResponse(requestRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Talep bulunamadı: " + id)), true);
    }

    @Transactional(readOnly = true)
    public Page<ChangeRequestResponse> list(ChangeRequestStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChangeRequest> result = status == null
                ? requestRepository.findAll(pageable)
                : requestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return result.map(request -> mapper.toResponse(request, false));
    }

    @Transactional
    public ChangeRequestResponse withdraw(Long id) {
        HeaderUser user = currentUser.get();
        ChangeRequest request = requestRepository.findForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Talep bulunamadı: " + id));
        if (request.getStatus() != ChangeRequestStatus.WAITING_APPROVAL) {
            throw new BusinessException("Yalnızca bekleyen talep geri çekilebilir");
        }
        if (!request.getCreatedBy().equals(user.id())) {
            throw new BusinessException("Yalnızca talebi oluşturan kullanıcı geri çekebilir");
        }
        request.withdraw();
        historyRepository.save(new ChangeHistory(request.getId(), "WITHDRAWN", user.id(), user.displayName(), null));
        return mapper.toResponse(request, true);
    }

    private void validateReferences(CreateChangeRequestCommand command, Set<String> references) {
        command.items().stream()
                .flatMap(item -> (item.referenceBindings() == null ? Map.<String, String>of() : item.referenceBindings())
                        .values().stream())
                .filter(reference -> !references.contains(reference))
                .findFirst()
                .ifPresent(reference -> {
                    throw new BusinessException("Talep içinde bulunmayan clientReference: " + reference);
                });
    }

    private static void requireRecordId(ChangeItemCommand item) {
        if (item.recordId() == null || item.recordId().isBlank()) {
            throw new BusinessException(item.operation() + " için recordId zorunludur");
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON oluşturulamadı", ex);
        }
    }

    private String writeNullable(Object value) {
        return value == null ? null : write(value);
    }

    private static String nextRequestNo() {
        return "PR-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

