package tn.tunisieconnect.listing.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.tunisieconnect.listing.dto.ListingRequest;
import tn.tunisieconnect.listing.dto.ListingResponse;
import tn.tunisieconnect.listing.entity.Listing;
import tn.tunisieconnect.listing.entity.ListingStatus;
import tn.tunisieconnect.listing.repository.ListingRepository;
import tn.tunisieconnect.listing.service.ListingService;
import tn.tunisieconnect.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public ListingResponse create(ListingRequest request, User owner) {

        Listing listing = Listing.builder().title(request.title()).description(request.description()).city(request.city()).address(request.address()).pricePerNight(request.pricePerNight()).maxGuests(request.maxGuests()).type(request.type())

                // 🔹 nouveaux champs
                .bedrooms(request.bedrooms()).beds(request.beds()).bathrooms(request.bathrooms()).amenities(request.amenities() != null ? new ArrayList<>(request.amenities()) : new ArrayList<>()).houseRules(request.houseRules()).checkInTime(request.checkInTime()).checkOutTime(request.checkOutTime())

                // 🔹 meta
                .status(ListingStatus.DRAFT).owner(owner).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        listingRepository.save(listing);
        return mapToResponse(listing);
    }

    @Override
    public List<ListingResponse> getMyListings(User owner) {
        return listingRepository.findByOwnerId(owner.getId()).stream().map(this::mapToResponse).toList();
    }

    @Override
    public ListingResponse update(Long id, ListingRequest request, User owner) {
        Listing listing = listingRepository.findById(id).orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setCity(request.city());
        listing.setAddress(request.address());
        listing.setPricePerNight(request.pricePerNight());
        listing.setMaxGuests(request.maxGuests());
        listing.setType(request.type());
        listing.setUpdatedAt(LocalDateTime.now());

        listingRepository.save(listing);
        return mapToResponse(listing);
    }

    @Override
    public void delete(Long id, User owner) {
        Listing listing = listingRepository.findById(id).orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        listingRepository.delete(listing);
    }

    @Override
    public ListingResponse publish(Long id, User owner) {
        Listing listing = getOwnedListing(id, owner);

        listing.setStatus(ListingStatus.PUBLISHED);
        listing.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(listingRepository.save(listing));
    }

    @Override
    public ListingResponse disable(Long id, User owner) {
        Listing listing = getOwnedListing(id, owner);

        listing.setStatus(ListingStatus.DISABLED);
        listing.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(listingRepository.save(listing));
    }

    @Override
    public ListingResponse getMyListingById(Long id, User owner) {
        Listing listing = getOwnedListing(id, owner);
        return mapToResponse(listing);
    }

    @Override
    public ListingResponse updateStatus(Long id, ListingStatus status, User owner) {

        Listing listing = listingRepository.findById(id).orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden");
        }

        listing.setStatus(status);
        listing.setUpdatedAt(LocalDateTime.now());

        listingRepository.save(listing);
        return mapToResponse(listing);
    }

    private Listing getOwnedListing(Long id, User owner) {
        return listingRepository.findByIdAndOwnerId(id, owner.getId()).orElseThrow(() -> new RuntimeException("Listing not found or access denied"));
    }

    private ListingResponse mapToResponse(Listing listing) {

        List<String> imageUrls = Optional.ofNullable(listing.getImages()).orElse(List.of()).stream().map(img -> "http://localhost:8080/listing/" + listing.getId() + "/" + img.getFileName()).toList();
        return ListingResponse.builder().id(listing.getId()).title(listing.getTitle()).description(listing.getDescription()).city(listing.getCity()).address(listing.getAddress()).pricePerNight(listing.getPricePerNight()).maxGuests(listing.getMaxGuests()).type(listing.getType()).status(listing.getStatus())

                // ✅ images
                .images(imageUrls)

                // ✅ nouveaux champs
                .bedrooms(listing.getBedrooms()).beds(listing.getBeds()).bathrooms(listing.getBathrooms()).amenities(listing.getAmenities() != null ? new ArrayList<>(listing.getAmenities()) : List.of()).houseRules(listing.getHouseRules()).checkInTime(listing.getCheckInTime()).checkOutTime(listing.getCheckOutTime())

                .build();
    }


}
