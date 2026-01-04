package tn.tunisieconnect.listing.service;

import tn.tunisieconnect.listing.dto.ListingRequest;
import tn.tunisieconnect.listing.dto.ListingResponse;
import tn.tunisieconnect.listing.entity.ListingStatus;
import tn.tunisieconnect.user.entity.User;

import java.util.List;

public interface ListingService {

    ListingResponse create(ListingRequest request, User owner);

    List<ListingResponse> getMyListings(User owner);

    ListingResponse update(Long id, ListingRequest request, User owner);

    void delete(Long id, User owner);

    ListingResponse publish(Long id, User owner);

    ListingResponse disable(Long id, User owner);

    ListingResponse getMyListingById(Long id, User owner);

    ListingResponse updateStatus(Long id, ListingStatus status, User owner);
}
