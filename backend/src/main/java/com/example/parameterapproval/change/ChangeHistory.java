package com.example.parameterapproval.change;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "PM_CHANGE_HISTORY")
public class ChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pm_change_history_seq")
    @SequenceGenerator(name = "pm_change_history_seq", sequenceName = "PM_CHANGE_HISTORY_SEQ", allocationSize = 50)
    private Long id;

    @Column(name = "CHANGE_REQUEST_ID", nullable = false)
    private Long changeRequestId;

    @Column(name = "ACTION", nullable = false, length = 30)
    private String action;

    @Column(name = "ACTION_BY", nullable = false, length = 100)
    private String actionBy;

    @Column(name = "ACTION_BY_NAME", nullable = false, length = 200)
    private String actionByName;

    @Column(name = "ACTION_AT", nullable = false)
    private Instant actionAt;

    @Column(name = "NOTE", length = 1000)
    private String note;

    protected ChangeHistory() { }

    public ChangeHistory(Long changeRequestId, String action, String actionBy, String actionByName, String note) {
        this.changeRequestId = changeRequestId;
        this.action = action;
        this.actionBy = actionBy;
        this.actionByName = actionByName;
        this.note = note;
        this.actionAt = Instant.now();
    }
}

