package tn.tunisieconnect.listing.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.tunisieconnect.listing.entity.Listing;
import tn.tunisieconnect.listing.entity.ListingImage;
import tn.tunisieconnect.listing.repository.ListingImageRepository;
import tn.tunisieconnect.listing.repository.ListingRepository;
import tn.tunisieconnect.listing.service.ListingImageService;
import tn.tunisieconnect.user.entity.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingImageServiceImpl implements ListingImageService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository imageRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void upload(Long listingId, List<MultipartFile> files, User owner) {

        Listing listing = listingRepository.findByIdAndOwnerId(listingId, owner.getId()).orElseThrow(() -> new RuntimeException("Access denied"));

        Path listingPath = Paths.get(uploadDir+"/listings", listingId.toString());
        try {
            Files.createDirectories(listingPath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create directory");
        }

        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = listingPath.resolve(fileName);

            try {
                Files.copy(file.getInputStream(), filePath);
            } catch (IOException e) {
                throw new RuntimeException("Upload failed");
            }

            imageRepository.save(ListingImage.builder().fileName(fileName).filePath(filePath.toString()).isCover(false).listing(listing).build());
        }
    }

    @Override
    public List<String> getListingImages(Long listingId) {
        return imageRepository.findByListingId(listingId).stream().map(ListingImage::getFilePath).toList();
    }
}
