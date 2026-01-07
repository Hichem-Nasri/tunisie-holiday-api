package tn.tunisieconnect.listing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.tunisieconnect.listing.service.ListingImageService;
import tn.tunisieconnect.user.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/host/listings")
@RequiredArgsConstructor
public class ListingImageController {

    private final ListingImageService imageService;

    @PostMapping("/{id}/images")
    public ResponseEntity<?> uploadImages(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files, @AuthenticationPrincipal User user) {
        imageService.upload(id, files, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/images")
    public List<String> getImages(@PathVariable Long id) {
        return imageService.getListingImages(id);
    }
}
