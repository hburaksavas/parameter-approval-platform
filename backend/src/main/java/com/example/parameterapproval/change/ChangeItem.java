package com.example.parameterapproval.change;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "PM_CHANGE_ITEM")
public class ChangeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pm_change_item_seq")
    @SequenceGenerator(name = "pm_change_item_seq", sequenceName = "PM_CHANGE_ITEM_SEQ", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CHANGE_REQUEST_ID", nullable = false)
    private ChangeRequest changeRequest;

    @Column(name = "RESOURCE_CODE", nullable = false, length = 100)
    private String resourceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATION_TYPE", nullable = false, length = 20)
    private OperationType operationType;

    @Column(name = "RECORD_ID", length = 300)
    private String recordId;

    @Column(name = "CLIENT_REFERENCE", length = 100)
    private String clientReference;

    @Lob
    @Column(name = "REFERENCE_BINDINGS_JSON")
    private String referenceBindingsJson;

    @Lob
    @Column(name = "OLD_VALUE_JSON")
    private String oldValueJson;

    @Lob
    @Column(name = "NEW_VALUE_JSON")
    private String newValueJson;

    @Column(name = "EXPECTED_VERSION")
    private Long expectedVersion;

    @Column(name = "EXECUTION_ORDER", nullable = false)
    private Integer executionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ChangeItemStatus status;

    protected ChangeItem() { }

    public ChangeItem(
            String resourceCode,
            OperationType operationType,
            String recordId,
            String clientReference,
            String referenceBindingsJson,
            String oldValueJson,
            String newValueJson,
            Long expectedVersion,
            Integer executionOrder) {
        this.resourceCode = resourceCode;
        this.operationType = operationType;
        this.recordId = recordId;
        this.clientReference = clientReference;
        this.referenceBindingsJson = referenceBindingsJson;
        this.oldValueJson = oldValueJson;
        this.newValueJson = newValueJson;
        this.expectedVersion = expectedVersion;
        this.executionOrder = executionOrder;
        this.status = ChangeItemStatus.PENDING;
    }

    void attach(ChangeRequest request) { this.changeRequest = request; }
    public void markApplied() { this.status = ChangeItemStatus.APPLIED; }

    public Long getId() { return id; }
    public String getResourceCode() { return resourceCode; }
    public OperationType getOperationType() { return operationType; }
    public String getRecordId() { return recordId; }
    public String getClientReference() { return clientReference; }
    public String getReferenceBindingsJson() { return referenceBindingsJson; }
    public String getOldValueJson() { return oldValueJson; }
    public String getNewValueJson() { return newValueJson; }
    public Long getExpectedVersion() { return expectedVersion; }
    public Integer getExecutionOrder() { return executionOrder; }
    public ChangeItemStatus getStatus() { return status; }
}

