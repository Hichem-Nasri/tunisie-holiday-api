package tn.tunisieholidays.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class StatsDtos {

    private StatsDtos() {
    }

    public record DayCount(LocalDate date, long clicks) {
    }

    public record TypeCount(String type, long clicks) {
    }

    public record PartnerCount(Long partnerId, String name, long clicks) {
    }

    public record PartnerStats(String partnerStatus, long clicks30d, Map<String, Long> offers,
                               List<DayCount> perDay, List<TypeCount> perType) {
    }

    public record AdminStats(long activePartners, long pendingPartners, long clicks30d, List<DayCount> perDay,
                             List<TypeCount> perType, List<PartnerCount> topPartners) {
    }
}
