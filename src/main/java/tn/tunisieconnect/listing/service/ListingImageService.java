package tn.tunisieconnect.listing.service;

import org.springframework.web.multipart.MultipartFile;
import tn.tunisieconnect.user.entity.User;

import java.util.List;

public interface ListingImageService {
    void upload(Long listingId, List<MultipartFile> files, User owner);

    List<String> getListingImages(Long listingId);
}