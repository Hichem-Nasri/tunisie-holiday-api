package tn.tunisieholidays.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.tunisieholidays.domain.Partner;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.domain.PartnerType;

import java.time.Instant;

public final class PartnerDtos {

    private PartnerDtos() {
    }

    public record PartnerView(Long id, String name, PartnerType type, PartnerStatus status, String email, String phone,
                              String website, String city, String logoUrl, String description, Instant createdAt) {

        public static PartnerView of(Partner p) {
            return new PartnerView(p.getId(), p.getName(), p.getType(), p.getStatus(), p.getEmail(), p.getPhone(),
                    p.getWebsite(), p.getCity(), p.getLogoUrl(), p.getDescription(), p.getCreatedAt());
        }
    }

    public record PartnerUpdateRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 40) String phone,
            @Size(max = 300) String website,
            @Size(max = 100) String city,
            @Size(max = 500) String logoUrl,
            @Size(max = 4000) String description) {
    }

    public record PartnerStatusRequest(@NotNull PartnerStatus status) {
    }
}
