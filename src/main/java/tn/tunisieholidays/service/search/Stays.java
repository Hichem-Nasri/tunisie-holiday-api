package tn.tunisieholidays.service.search;

import tn.tunisieholidays.common.ApiException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class Stays {

    private Stays() {
    }

    public static int nights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw ApiException.badRequest("Les dates d'arrivée et de départ sont obligatoires");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw ApiException.badRequest("La date d'arrivée est passée");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1 || nights > 60) {
            throw ApiException.badRequest("La durée du séjour doit être comprise entre 1 et 60 nuits");
        }
        return (int) nights;
    }

    public static int rentalDays(LocalDateTime pickup, LocalDateTime dropoff) {
        if (pickup == null || dropoff == null) {
            throw ApiException.badRequest("Les dates de prise en charge et de restitution sont obligatoires");
        }
        if (pickup.isBefore(LocalDateTime.now().minusHours(1))) {
            throw ApiException.badRequest("La date de prise en charge est passée");
        }
        long minutes = Duration.between(pickup, dropoff).toMinutes();
        if (minutes <= 0) {
            throw ApiException.badRequest("La restitution doit suivre la prise en charge");
        }
        long days = (minutes + 1439) / 1440;
        if (days > 90) {
            throw ApiException.badRequest("La durée de location ne peut pas dépasser 90 jours");
        }
        return (int) Math.max(1, days);
    }
}
