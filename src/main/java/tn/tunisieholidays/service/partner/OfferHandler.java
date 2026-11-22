package tn.tunisieholidays.service.partner;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tunisieholidays.domain.BaseOffer;
import tn.tunisieholidays.domain.OfferType;
import tn.tunisieholidays.domain.PartnerType;
import tn.tunisieholidays.dto.OfferDtos.OfferRequest;

import java.util.List;
import java.util.Set;

public interface OfferHandler<E extends BaseOffer, R extends OfferRequest> {

    OfferType type();

    Set<PartnerType> allowedPartnerTypes();

    Class<R> requestType();

    JpaRepository<E, Long> repository();

    List<E> findByPartner(Long partnerId);

    E newEntity();

    void apply(E entity, R request);

    Object toView(E entity, long clicks30d);
}
