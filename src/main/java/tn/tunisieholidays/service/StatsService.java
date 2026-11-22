package tn.tunisieholidays.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tunisieholidays.dto.StatsDtos.DayCount;
import tn.tunisieholidays.dto.StatsDtos.PartnerCount;
import tn.tunisieholidays.dto.StatsDtos.TypeCount;
import tn.tunisieholidays.repository.ClickEventRepository;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ClickEventRepository repository;

    public static Instant since30Days() {
        return Instant.now().minus(30, ChronoUnit.DAYS);
    }

    @Transactional(readOnly = true)
    public List<DayCount> perDay(Long partnerId) {
        return repository.countPerDay(partnerId, since30Days()).stream()
                .map(r -> new DayCount(toDate(r[0]), ((Number) r[1]).longValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TypeCount> perType(Long partnerId) {
        return repository.countPerType(partnerId, since30Days()).stream()
                .map(r -> new TypeCount((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PartnerCount> perPartner() {
        return repository.countPerPartner(since30Days()).stream()
                .map(r -> new PartnerCount(((Number) r[0]).longValue(), (String) r[1], ((Number) r[2]).longValue()))
                .toList();
    }

    private LocalDate toDate(Object value) {
        if (value instanceof LocalDate d) {
            return d;
        }
        if (value instanceof Date d) {
            return d.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
