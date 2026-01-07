package tn.tunisieconnect.listing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.tunisieconnect.listing.dto.ListingRequest;
import tn.tunisieconnect.listing.dto.ListingResponse;
import tn.tunisieconnect.listing.entity.ListingStatus;
import tn.tunisieconnect.listing.service.ListingService;
import tn.tunisieconnect.user.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/host/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ListingResponse create(@RequestBody ListingRequest request, @AuthenticationPrincipal User user) {
        return listingService.create(request, user);
    }

    @PatchMapping("/{id}/status")
    public ListingResponse updateStatus(@PathVariable Long id, @RequestParam ListingStatus status, Authentication authentication) {
        User owner = (User) authentication.getPrincipal();
        return listingService.updateStatus(id, status, owner);
    }

    @GetMapping
    public List<ListingResponse> myListings(@AuthenticationPrincipal User user) {
        return listingService.getMyListings(user);
    }

    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable Long id, @RequestBody ListingRequest request, @AuthenticationPrincipal User user) {
        return listingService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        listingService.delete(id, user);
    }

    @PatchMapping("/{id}/publish")
    public ListingResponse publish(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return listingService.publish(id, user);
    }

    @PatchMapping("/{id}/disable")
    public ListingResponse disable(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return listingService.disable(id, user);
    }

    @GetMapping("/{id}")
    public ListingResponse getOne(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return listingService.getMyListingById(id, user);
    }
}
