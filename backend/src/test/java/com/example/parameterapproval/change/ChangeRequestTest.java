package com.example.parameterapproval.change;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeRequestTest {

    @Test
    void shouldStartWaitingAndRecordApproval() {
        ChangeRequest request = new ChangeRequest("PR-1", "Başlık", null, "maker", "Maker");

        assertThat(request.getStatus()).isEqualTo(ChangeRequestStatus.WAITING_APPROVAL);

        request.approve("approver", "Approver", "uygun");

        assertThat(request.getStatus()).isEqualTo(ChangeRequestStatus.APPROVED);
        assertThat(request.getDecidedBy()).isEqualTo("approver");
        assertThat(request.getDecisionNote()).isEqualTo("uygun");
    }
}

