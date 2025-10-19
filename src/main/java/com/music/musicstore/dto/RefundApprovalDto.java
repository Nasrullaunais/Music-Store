package com.music.musicstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RefundApprovalDto {

    private boolean approved;

    @Size(max = 1000, message = "Admin notes cannot exceed 1000 characters")
    private String adminNotes;

    public RefundApprovalDto() {}

    public RefundApprovalDto(boolean approved, String adminNotes) {
        this.approved = approved;
        this.adminNotes = adminNotes;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}
