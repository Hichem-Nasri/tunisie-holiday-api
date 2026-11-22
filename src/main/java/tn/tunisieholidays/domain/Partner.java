package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tn.tunisieholidays.common.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "partners")
public class Partner extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnerStatus status = PartnerStatus.PENDING;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 300)
    private String website;

    @Column(length = 100)
    private String city;

    @Column(length = 500)
    private String logoUrl;

    @Column(columnDefinition = "text")
    private String description;
}
