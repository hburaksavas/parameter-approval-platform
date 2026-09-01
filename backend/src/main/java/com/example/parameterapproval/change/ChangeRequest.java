package com.example.parameterapproval.change;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PM_CHANGE_REQUEST")
public class ChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pm_change_request_seq")
    @SequenceGenerator(name = "pm_change_request_seq", sequenceName = "PM_CHANGE_REQUEST_SEQ", allocationSize = 20)
    private Long id;

    @Column(name = "REQUEST_NO", nullable = false, unique = true, length = 40)
    private String requestNo;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 30)
    private ChangeRequestStatus status;

    @Column(name = "CREATED_BY", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "CREATED_BY_NAME", nullable = false, length = 200)
    private String createdByName;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "DECIDED_BY", length = 100)
    private String decidedBy;

    @Column(name = "DECIDED_BY_NAME", length = 200)
    private String decidedByName;

    @Column(name = "DECIDED_AT")
    private Instant decidedAt;

    @Column(name = "DECISION_NOTE", length = 1000)
    private String decisionNote;

    @Version
    @Column(name = "ROW_VERSION", nullable = false)
    private Long rowVersion;

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("executionOrder asc, id asc")
    private List<ChangeItem> items = new ArrayList<>();

    protected ChangeRequest() { }

    public ChangeRequest(String requestNo, String title, String description, String createdBy, String createdByName) {
        this.requestNo = requestNo;
        this.title = title;
        this.description = description;
        this.status = ChangeRequestStatus.WAITING_APPROVAL;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = Instant.now();
    }

    public void addItem(ChangeItem item) {
        item.attach(this);
        items.add(item);
    }

    public void approve(String userId, String userName, String note) {
        this.status = ChangeRequestStatus.APPROVED;
        this.decidedBy = userId;
        this.decidedByName = userName;
        this.decisionNote = note;
        this.decidedAt = Instant.now();
    }

    public void reject(String userId, String userName, String note) {
        this.status = ChangeRequestStatus.REJECTED;
        this.decidedBy = userId;
        this.decidedByName = userName;
        this.decisionNote = note;
        this.decidedAt = Instant.now();
    }

    public void withdraw() {
        this.status = ChangeRequestStatus.WITHDRAWN;
        this.decidedAt = Instant.now();
    }

    public void markConflict(String userId, String userName, String note) {
        this.status = ChangeRequestStatus.FAILED_CONFLICT;
        this.decidedBy = userId;
        this.decidedByName = userName;
        this.decisionNote = note;
        this.decidedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getRequestNo() { return requestNo; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ChangeRequestStatus getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public Instant getCreatedAt() { return createdAt; }
    public String getDecidedBy() { return decidedBy; }
    public String getDecidedByName() { return decidedByName; }
    public Instant getDecidedAt() { return decidedAt; }
    public String getDecisionNote() { return decisionNote; }
    public Long getRowVersion() { return rowVersion; }
    public List<ChangeItem> getItems() { return items; }
}

