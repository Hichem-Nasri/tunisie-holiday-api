package tn.tunisieconnect.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieconnect.listing.entity.Listing;
import tn.tunisieconnect.listing.entity.ListingStatus;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByOwnerId(Long ownerId);

    Optional<Listing> findByIdAndOwnerId(Long id, Long ownerId);

    List<Listing> findByStatus(ListingStatus status);
}