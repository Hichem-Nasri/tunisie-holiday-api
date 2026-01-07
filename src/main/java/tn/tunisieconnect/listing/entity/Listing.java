package tn.tunisieconnect.listing.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.tunisieconnect.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String city;
    private String address;

    private BigDecimal pricePerNight;

    private Integer maxGuests;

    @Enumerated(EnumType.STRING)
    private ListingType type; // APARTMENT, ROOM, HOTEL_ROOM

    @Enumerated(EnumType.STRING)
    private ListingStatus status; // DRAFT, PUBLISHED, DISABLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingImage> images = new ArrayList<>();

    // Configuration
    private Integer bedrooms;
    private Integer beds;
    private Integer bathrooms;

    // Équipements (MVP simple)
    @ElementCollection
    @CollectionTable(name = "listing_amenities", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    // Règles
    @Column(length = 500)
    private String houseRules;

    // Check-in / Check-out
    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
