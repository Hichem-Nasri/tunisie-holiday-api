package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.Destination;
import tn.tunisieholidays.dto.CatalogDtos.DestinationRequest;
import tn.tunisieholidays.dto.CatalogDtos.DestinationView;
import tn.tunisieholidays.repository.DestinationRepository;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository repository;

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return decomposed.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    @Transactional(readOnly = true)
    public List<DestinationView> list() {
        return repository.findAllByOrderBySortOrderAscNameAsc().stream().map(DestinationView::of).toList();
    }

    @Transactional(readOnly = true)
    public DestinationView get(String slug) {
        return repository.findBySlug(slug).map(DestinationView::of)
                .orElseThrow(() -> ApiException.notFound("Destination introuvable"));
    }

    @Transactional(readOnly = true)
    public Destination require(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Destination introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Long> resolveIds(String query) {
        List<Destination> all = repository.findAll();
        String q = normalize(query);
        if (q.isEmpty()) {
            return all.stream().map(Destination::getId).toList();
        }
        return all.stream()
                .filter(d -> d.getSlug().equals(query.trim().toLowerCase(Locale.ROOT))
                        || normalize(d.getName()).contains(q)
                        || q.contains(normalize(d.getName()))
                        || normalize(d.getRegion()).equals(q))
                .map(Destination::getId)
                .toList();
    }

    @Transactional
    public DestinationView create(DestinationRequest request) {
        if (repository.existsBySlug(request.slug())) {
            throw ApiException.conflict("Ce slug existe déjà");
        }
        Destination d = new Destination();
        apply(d, request);
        return DestinationView.of(repository.save(d));
    }

    @Transactional
    public DestinationView update(Long id, DestinationRequest request) {
        Destination d = require(id);
        if (!d.getSlug().equals(request.slug()) && repository.existsBySlug(request.slug())) {
            throw ApiException.conflict("Ce slug existe déjà");
        }
        apply(d, request);
        return DestinationView.of(repository.save(d));
    }

    private void apply(Destination d, DestinationRequest r) {
        d.setName(r.name());
        d.setSlug(r.slug());
        d.setRegion(r.region());
        d.setTagline(r.tagline());
        d.setDescription(r.description());
        d.setImageUrl(r.imageUrl());
        d.setAirportCode(r.airportCode());
        d.setPopular(r.popular());
        d.setSortOrder(r.sortOrder());
    }
}
