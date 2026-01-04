package tn.tunisieconnect.listing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieconnect.listing.entity.ListingImage;

import java.util.List;

public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    List<ListingImage> findByListingId(Long listingId);
}