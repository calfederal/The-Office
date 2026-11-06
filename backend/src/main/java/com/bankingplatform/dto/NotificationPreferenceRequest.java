package com.bankingplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceRequest {
    @NotBlank
    private String category;          // TRANSACTION, SECURITY, ACCOUNT, MARKETING
    private boolean emailEnabled = true;
    private boolean smsEnabled = true;
    private boolean pushEnabled = true;
    private boolean inAppEnabled = true;
}
