package tn.tunisieholidays.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tn.tunisieholidays.common.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "destinations")
public class Destination extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(length = 120)
    private String region;

    @Column(length = 200)
    private String tagline;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 3)
    private String airportCode;

    @Column(nullable = false)
    private boolean popular;

    @Column(nullable = false)
    private int sortOrder;
}
