package tn.tunisieholidays.service.partner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.common.ApiException;
import tn.tunisieholidays.domain.BaseOffer;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.Partner;
import tn.tunisieholidays.domain.PartnerStatus;
import tn.tunisieholidays.dto.OfferDtos.OfferRequest;
import tn.tunisieholidays.repository.ClickEventRepository;
import tn.tunisieholidays.repository.PartnerRepository;
import tn.tunisieholidays.security.AuthPrincipal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PartnerOfferService {

    private final Map<OfferType, OfferHandler<?, ?>> handlers = new EnumMap<>(OfferType.class);
    private final PartnerRepository partnerRepository;
    private final ClickEventRepository clickRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public PartnerOfferService(List<OfferHandler<?, ?>> handlerList, PartnerRepository partnerRepository,
                               ClickEventRepository clickRepository, ObjectMapper objectMapper, Validator validator) {
        handlerList.forEach(h -> handlers.put(h.type(), h));
        this.partnerRepository = partnerRepository;
        this.clickRepository = clickRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<Object> list(AuthPrincipal principal, OfferType type) {
        return doList(handler(type), partnerId(principal));
    }

    @Transactional
    public Object create(AuthPrincipal principal, OfferType type, JsonNode body) {
        return doCreate(handler(type), activePartner(principal), body);
    }

    @Transactional
    public Object update(AuthPrincipal principal, OfferType type, Long id, JsonNode body) {
        return doUpdate(handler(type), activePartner(principal), id, body);
    }

    @Transactional
    public Object setActive(AuthPrincipal principal, OfferType type, Long id, boolean active) {
        return doSetActive(handler(type), partnerId(principal), id, active);
    }

    @Transactional
    public void delete(AuthPrincipal principal, OfferType type, Long id) {
        doDelete(handler(type), partnerId(principal), id);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countOffers(Long partnerId) {
        Map<String, Long> counts = new HashMap<>();
        handlers.forEach((type, h) -> counts.put(type.name(), (long) h.findByPartner(partnerId).size()));
        return counts;
    }

    private <E extends BaseOffer, R extends OfferRequest> List<Object> doList(OfferHandler<E, R> h, Long partnerId) {
        Map<Long, Long> clicks = clicksPerOffer(partnerId, h.type());
        return h.findByPartner(partnerId).stream()
                .map(e -> h.toView(e, clicks.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    private <E extends BaseOffer, R extends OfferRequest> Object doCreate(OfferHandler<E, R> h, Partner partner,
                                                                          JsonNode body) {
        checkType(h, partner);
        R request = read(h.requestType(), body);
        E entity = h.newEntity();
        entity.setPartner(partner);
        entity.setActive(true);
        applyCommon(entity, request);
        h.apply(entity, request);
        return h.toView(h.repository().save(entity), 0);
    }

    private <E extends BaseOffer, R extends OfferRequest> Object doUpdate(OfferHandler<E, R> h, Partner partner,
                                                                          Long id, JsonNode body) {
        E entity = owned(h, partner.getId(), id);
        R request = read(h.requestType(), body);
        applyCommon(entity, request);
        h.apply(entity, request);
        return h.toView(h.repository().save(entity), clicksPerOffer(partner.getId(), h.type()).getOrDefault(id, 0L));
    }

    private <E extends BaseOffer, R extends OfferRequest> Object doSetActive(OfferHandler<E, R> h, Long partnerId,
                                                                             Long id, boolean active) {
        E entity = owned(h, partnerId, id);
        entity.setActive(active);
        return h.toView(h.repository().save(entity), clicksPerOffer(partnerId, h.type()).getOrDefault(id, 0L));
    }

    private <E extends BaseOffer, R extends OfferRequest> void doDelete(OfferHandler<E, R> h, Long partnerId, Long id) {
        h.repository().delete(owned(h, partnerId, id));
    }

    private <E extends BaseOffer> E owned(OfferHandler<E, ?> h, Long partnerId, Long id) {
        E entity = h.repository().findById(id).orElseThrow(() -> ApiException.notFound("Offre introuvable"));
        if (!entity.getPartner().getId().equals(partnerId)) {
            throw ApiException.notFound("Offre introuvable");
        }
        return entity;
    }

    private void applyCommon(BaseOffer entity, OfferRequest request) {
        entity.setPrice(request.price());
        entity.setCurrency(request.currency());
        entity.setDeeplinkUrl(request.deeplinkUrl().trim());
    }

    private <R> R read(Class<R> type, JsonNode body) {
        R request;
        try {
            request = objectMapper.treeToValue(body, type);
        } catch (Exception ex) {
            throw ApiException.badRequest("Format de l'offre invalide");
        }
        Set<ConstraintViolation<R>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining(" ; "));
            throw ApiException.badRequest(message);
        }
        return request;
    }

    private void checkType(OfferHandler<?, ?> h, Partner partner) {
        if (!h.allowedPartnerTypes().contains(partner.getType())) {
            throw ApiException.forbidden("Votre type de compte ne permet pas de publier ce type d'offre");
        }
    }

    private Map<Long, Long> clicksPerOffer(Long partnerId, OfferType type) {
        Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
        return clickRepository.countPerOffer(partnerId, type.name(), from).stream()
                .collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));
    }

    private OfferHandler<?, ?> handler(OfferType type) {
        OfferHandler<?, ?> h = handlers.get(type);
        if (h == null) {
            throw ApiException.badRequest("Type d'offre inconnu");
        }
        return h;
    }

    private Long partnerId(AuthPrincipal principal) {
        if (principal.partnerId() == null) {
            throw ApiException.forbidden("Aucun compte partenaire associé");
        }
        return principal.partnerId();
    }

    private Partner activePartner(AuthPrincipal principal) {
        Partner partner = partnerRepository.findById(partnerId(principal))
                .orElseThrow(() -> ApiException.forbidden("Compte partenaire introuvable"));
        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            throw ApiException.forbidden("Votre compte partenaire doit être validé avant de publier des offres");
        }
        return partner;
    }
}
