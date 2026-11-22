INSERT INTO partners (name, type, status, email, phone, website, city, description, created_at, updated_at) VALUES
('Carthage Voyages', 'AGENCY', 'ACTIVE', 'contact@carthage-voyages.example', '+216 71 000 001', 'https://www.example.com/carthage-voyages', 'Tunis', 'Agence de voyages (démo).', now(), now()),
('Sahel Travel', 'AGENCY', 'ACTIVE', 'contact@sahel-travel.example', '+216 73 000 002', 'https://www.example.com/sahel-travel', 'Sousse', 'Agence de voyages (démo).', now(), now()),
('Medina Tours', 'AGENCY', 'ACTIVE', 'contact@medina-tours.example', '+216 72 000 003', 'https://www.example.com/medina-tours', 'Hammamet', 'Agence de voyages (démo).', now(), now()),
('Carthage Rent', 'CAR_RENTAL', 'ACTIVE', 'contact@carthage-rent.example', '+216 71 000 004', 'https://www.example.com/carthage-rent', 'Tunis', 'Loueur de voitures (démo).', now(), now()),
('Jasmin Cars', 'CAR_RENTAL', 'ACTIVE', 'contact@jasmin-cars.example', '+216 75 000 005', 'https://www.example.com/jasmin-cars', 'Djerba', 'Loueur de voitures (démo).', now(), now()),
('Sahara Auto', 'CAR_RENTAL', 'ACTIVE', 'contact@sahara-auto.example', '+216 76 000 006', 'https://www.example.com/sahara-auto', 'Monastir', 'Loueur de voitures (démo).', now(), now()),
('Dar Jasmin Médina', 'HOTEL', 'ACTIVE', 'contact@dar-jasmin.example', '+216 72 000 007', 'https://www.example.com/dar-jasmin', 'Hammamet', 'Maison d''hôtes (démo).', now(), now()),
('Villas du Cap', 'HOST', 'ACTIVE', 'contact@villas-du-cap.example', '+216 72 000 008', 'https://www.example.com/villas-du-cap', 'Nabeul', 'Locations de vacances (démo).', now(), now()),
('Sahara Expériences', 'ACTIVITY', 'ACTIVE', 'contact@sahara-experiences.example', '+216 76 000 009', 'https://www.example.com/sahara-experiences', 'Tozeur', 'Excursions et loisirs (démo).', now(), now());

INSERT INTO flight_offers (partner_id, airline_code, airline_name, flight_number, origin_code, destination_code,
                           departure_at, arrival_at, stops, cabin, baggage_included, seats_available, price, currency,
                           deeplink_url, active, created_at, updated_at)
SELECT r.partner_id, r.code, r.airline, r.num, r.o, r.dst,
       d::date + r.dep,
       d::date + r.arr,
       r.stops, 'ECONOMY', r.bag, 9,
       r.price + (EXTRACT(DOW FROM d)::int * 4),
       'EUR',
       'https://www.example.com/' || r.slug || '/vols?vol=' || replace(r.num, ' ', '') || '&date=' || to_char(d, 'YYYY-MM-DD'),
       true, now(), now()
FROM generate_series(CURRENT_DATE::timestamp, CURRENT_DATE::timestamp + INTERVAL '240 days', INTERVAL '1 day') AS d
CROSS JOIN (VALUES
    (1, 'TU', 'Tunisair', 'TU 723', 'ORY', 'DJE', TIME '07:40', TIME '10:25', 0, 145, true, 'carthage-voyages'),
    (3, 'TU', 'Tunisair', 'TU 723', 'ORY', 'DJE', TIME '07:40', TIME '10:25', 0, 149, true, 'medina-tours'),
    (1, 'TU', 'Tunisair', 'TU 724', 'DJE', 'ORY', TIME '11:30', TIME '14:20', 0, 150, true, 'carthage-voyages'),
    (3, 'TU', 'Tunisair', 'TU 724', 'DJE', 'ORY', TIME '11:30', TIME '14:20', 0, 154, true, 'medina-tours'),
    (2, 'BJ', 'Nouvelair', 'BJ 541', 'CDG', 'DJE', TIME '06:15', TIME '09:05', 0, 128, true, 'sahel-travel'),
    (1, 'BJ', 'Nouvelair', 'BJ 541', 'CDG', 'DJE', TIME '06:15', TIME '09:05', 0, 134, true, 'carthage-voyages'),
    (2, 'BJ', 'Nouvelair', 'BJ 542', 'DJE', 'CDG', TIME '19:40', TIME '22:30', 0, 132, true, 'sahel-travel'),
    (1, 'BJ', 'Nouvelair', 'BJ 542', 'DJE', 'CDG', TIME '19:40', TIME '22:30', 0, 138, true, 'carthage-voyages'),
    (3, 'TO', 'Transavia', 'TO 3410', 'ORY', 'DJE', TIME '13:10', TIME '16:05', 0, 116, false, 'medina-tours'),
    (3, 'TO', 'Transavia', 'TO 3411', 'DJE', 'ORY', TIME '17:00', TIME '19:55', 0, 118, false, 'medina-tours'),
    (3, 'TU', 'Tunisair', 'TU 715', 'ORY', 'DJE', TIME '15:20', TIME '20:50', 1, 107, true, 'medina-tours'),
    (3, 'TU', 'Tunisair', 'TU 716', 'DJE', 'ORY', TIME '08:00', TIME '13:10', 1, 109, true, 'medina-tours'),
    (1, 'TU', 'Tunisair', 'TU 711', 'ORY', 'TUN', TIME '09:00', TIME '11:20', 0, 120, true, 'carthage-voyages'),
    (1, 'TU', 'Tunisair', 'TU 712', 'TUN', 'ORY', TIME '12:30', TIME '14:55', 0, 125, true, 'carthage-voyages'),
    (3, 'AF', 'Air France', 'AF 1184', 'CDG', 'TUN', TIME '09:50', TIME '12:15', 0, 171, true, 'medina-tours'),
    (2, 'AF', 'Air France', 'AF 1184', 'CDG', 'TUN', TIME '09:50', TIME '12:15', 0, 168, true, 'sahel-travel'),
    (3, 'AF', 'Air France', 'AF 1185', 'TUN', 'CDG', TIME '13:30', TIME '16:00', 0, 175, true, 'medina-tours'),
    (2, 'AF', 'Air France', 'AF 1185', 'TUN', 'CDG', TIME '13:30', TIME '16:00', 0, 172, true, 'sahel-travel'),
    (2, 'BJ', 'Nouvelair', 'BJ 811', 'LYS', 'MIR', TIME '10:10', TIME '12:10', 0, 110, true, 'sahel-travel'),
    (2, 'BJ', 'Nouvelair', 'BJ 812', 'MIR', 'LYS', TIME '13:00', TIME '15:00', 0, 112, true, 'sahel-travel'),
    (1, 'TU', 'Tunisair', 'TU 621', 'MRS', 'TUN', TIME '08:20', TIME '09:55', 0, 98, true, 'carthage-voyages'),
    (1, 'TU', 'Tunisair', 'TU 622', 'TUN', 'MRS', TIME '17:10', TIME '18:45', 0, 102, true, 'carthage-voyages'),
    (2, 'BJ', 'Nouvelair', 'BJ 301', 'BRU', 'NBE', TIME '11:45', TIME '14:25', 0, 139, true, 'sahel-travel'),
    (2, 'BJ', 'Nouvelair', 'BJ 302', 'NBE', 'BRU', TIME '15:30', TIME '18:20', 0, 142, true, 'sahel-travel')
) AS r(partner_id, code, airline, num, o, dst, dep, arr, stops, price, bag, slug);

INSERT INTO hotels (name, destination_id, type, stars, address, location_label, description, image_url, rating, review_count, latitude, longitude, created_at, updated_at) VALUES
('Résidence Yasmine Beach', (SELECT id FROM destinations WHERE slug = 'hammamet'), 'RESORT', 4, 'Yasmine Hammamet', 'Yasmine Hammamet · 50 m de la plage', 'Resort familial en bord de mer (démo).', '/assets/img/pool.svg', 8.7, 412, 36.3730, 10.5480, now(), now()),
('Dar Jasmin Médina', (SELECT id FROM destinations WHERE slug = 'hammamet'), 'DAR', 3, 'Médina de Hammamet', 'Médina de Hammamet · maison d''hôtes', 'Maison d''hôtes traditionnelle avec patio (démo).', '/assets/img/dar.svg', 9.1, 168, 36.3995, 10.6155, now(), now()),
('Palm Garden Thalasso', (SELECT id FROM destinations WHERE slug = 'hammamet'), 'RESORT', 5, 'Hammamet Nord', 'Hammamet Nord · front de mer', 'Resort 5 étoiles avec centre de thalasso (démo).', '/assets/img/djerba.svg', 8.4, 655, 36.4130, 10.5900, now(), now()),
('Hôtel Les Orangers', (SELECT id FROM destinations WHERE slug = 'hammamet'), 'HOTEL', 4, 'Hammamet centre', 'Hammamet centre · 200 m de la plage', 'Hôtel au cœur de Hammamet (démo).', '/assets/img/hammamet.svg', 8.0, 290, 36.4000, 10.6000, now(), now()),
('Djerba Lagoon Resort', (SELECT id FROM destinations WHERE slug = 'djerba'), 'RESORT', 4, 'Zone touristique Midoun', 'Midoun · accès direct plage', 'Resort tout compris sur la plage de Sidi Mahrez (démo).', '/assets/img/djerba.svg', 8.5, 520, 33.8350, 11.0400, now(), now()),
('Houch Djerba', (SELECT id FROM destinations WHERE slug = 'djerba'), 'DAR', 3, 'Houmt Souk', 'Houmt Souk · centre historique', 'Houch traditionnel restauré (démo).', '/assets/img/dar.svg', 9.3, 96, 33.8760, 10.8570, now(), now()),
('Marina Bay Sousse', (SELECT id FROM destinations WHERE slug = 'sousse'), 'HOTEL', 4, 'Port El Kantaoui', 'Port El Kantaoui · vue marina', 'Hôtel face à la marina (démo).', '/assets/img/sousse.svg', 8.2, 340, 35.8920, 10.5980, now(), now()),
('Oasis Palms Tozeur', (SELECT id FROM destinations WHERE slug = 'tozeur'), 'HOTEL', 4, 'Zone touristique', 'Tozeur · au bord de la palmeraie', 'Hôtel au cœur de la palmeraie (démo).', '/assets/img/tozeur.svg', 8.6, 210, 33.9100, 8.1300, now(), now()),
('Dar Sidi Bou', (SELECT id FROM destinations WHERE slug = 'sidi-bou-said'), 'DAR', 4, 'Sidi Bou Saïd', 'Sidi Bou Saïd · vue sur le golfe', 'Maison d''hôtes de charme (démo).', '/assets/img/sidibou.svg', 9.4, 134, 36.8700, 10.3470, now(), now()),
('Tabarka Corail Hôtel', (SELECT id FROM destinations WHERE slug = 'tabarka'), 'HOTEL', 3, 'Zone touristique', 'Tabarka · près du port', 'Hôtel proche des spots de plongée (démo).', '/assets/img/tabarka.svg', 7.9, 150, 36.9540, 8.7580, now(), now());

INSERT INTO hotel_amenities (hotel_id, amenity)
SELECT h.id, a.amenity FROM hotels h
JOIN (VALUES
    ('Résidence Yasmine Beach', 'BEACH'), ('Résidence Yasmine Beach', 'POOL'), ('Résidence Yasmine Beach', 'KIDS_CLUB'), ('Résidence Yasmine Beach', 'WIFI'),
    ('Dar Jasmin Médina', 'WIFI'), ('Dar Jasmin Médina', 'TERRACE'),
    ('Palm Garden Thalasso', 'BEACH'), ('Palm Garden Thalasso', 'POOL'), ('Palm Garden Thalasso', 'SPA'), ('Palm Garden Thalasso', 'AQUAPARK'), ('Palm Garden Thalasso', 'WIFI'),
    ('Hôtel Les Orangers', 'POOL'), ('Hôtel Les Orangers', 'WIFI'),
    ('Djerba Lagoon Resort', 'BEACH'), ('Djerba Lagoon Resort', 'POOL'), ('Djerba Lagoon Resort', 'KIDS_CLUB'), ('Djerba Lagoon Resort', 'AQUAPARK'),
    ('Houch Djerba', 'WIFI'), ('Houch Djerba', 'POOL'),
    ('Marina Bay Sousse', 'POOL'), ('Marina Bay Sousse', 'SPA'), ('Marina Bay Sousse', 'WIFI'),
    ('Oasis Palms Tozeur', 'POOL'), ('Oasis Palms Tozeur', 'SPA'),
    ('Dar Sidi Bou', 'WIFI'), ('Dar Sidi Bou', 'TERRACE'),
    ('Tabarka Corail Hôtel', 'BEACH'), ('Tabarka Corail Hôtel', 'POOL')
) AS a(hotel, amenity) ON a.hotel = h.name;

INSERT INTO hotel_offers (partner_id, hotel_id, board, room_type, max_guests, free_cancellation, price, currency, deeplink_url, active, created_at, updated_at)
SELECT o.partner_id, h.id, o.board, o.room, o.guests, o.cancel, o.price, 'TND',
       'https://www.example.com/' || o.slug || '/hotel/' || h.id, true, now(), now()
FROM hotels h
JOIN (VALUES
    ('Résidence Yasmine Beach', 1, 'ALL_INCLUSIVE', 'Chambre double vue jardin', 3, true, 413, 'carthage-voyages'),
    ('Résidence Yasmine Beach', 2, 'ALL_INCLUSIVE', 'Chambre double vue jardin', 3, false, 423, 'sahel-travel'),
    ('Résidence Yasmine Beach', 3, 'ALL_INCLUSIVE', 'Chambre double vue jardin', 3, true, 446, 'medina-tours'),
    ('Résidence Yasmine Beach', 1, 'HALF_BOARD', 'Chambre double vue jardin', 3, true, 330, 'carthage-voyages'),
    ('Dar Jasmin Médina', 7, 'BREAKFAST', 'Chambre patio', 3, true, 270, 'dar-jasmin'),
    ('Dar Jasmin Médina', 3, 'BREAKFAST', 'Chambre patio', 3, false, 283, 'medina-tours'),
    ('Palm Garden Thalasso', 2, 'ALL_INCLUSIVE', 'Chambre supérieure vue mer', 3, true, 621, 'sahel-travel'),
    ('Palm Garden Thalasso', 1, 'ALL_INCLUSIVE', 'Chambre supérieure vue mer', 3, true, 630, 'carthage-voyages'),
    ('Palm Garden Thalasso', 3, 'ALL_INCLUSIVE', 'Chambre supérieure vue mer', 3, false, 656, 'medina-tours'),
    ('Hôtel Les Orangers', 3, 'HALF_BOARD', 'Chambre double', 3, true, 320, 'medina-tours'),
    ('Hôtel Les Orangers', 1, 'HALF_BOARD', 'Chambre double', 3, false, 330, 'carthage-voyages'),
    ('Djerba Lagoon Resort', 1, 'ALL_INCLUSIVE', 'Chambre familiale', 4, true, 455, 'carthage-voyages'),
    ('Djerba Lagoon Resort', 2, 'ALL_INCLUSIVE', 'Chambre familiale', 4, true, 470, 'sahel-travel'),
    ('Houch Djerba', 3, 'BREAKFAST', 'Suite traditionnelle', 2, true, 240, 'medina-tours'),
    ('Marina Bay Sousse', 2, 'HALF_BOARD', 'Chambre vue marina', 3, true, 295, 'sahel-travel'),
    ('Marina Bay Sousse', 1, 'ALL_INCLUSIVE', 'Chambre vue marina', 3, true, 385, 'carthage-voyages'),
    ('Oasis Palms Tozeur', 1, 'HALF_BOARD', 'Chambre double', 3, true, 260, 'carthage-voyages'),
    ('Oasis Palms Tozeur', 3, 'HALF_BOARD', 'Chambre double', 3, false, 252, 'medina-tours'),
    ('Dar Sidi Bou', 1, 'BREAKFAST', 'Chambre vue mer', 2, true, 390, 'carthage-voyages'),
    ('Tabarka Corail Hôtel', 2, 'HALF_BOARD', 'Chambre double', 3, true, 190, 'sahel-travel')
) AS o(hotel, partner_id, board, room, guests, cancel, price, slug) ON o.hotel = h.name;

INSERT INTO car_offers (partner_id, pickup_code, pickup_label, category, model, seats, doors, bags, transmission, air_conditioning, unlimited_mileage, free_cancellation, min_driver_age, image_url, price, currency, deeplink_url, active, created_at, updated_at)
SELECT c.partner_id, c.code, c.label, c.cat, c.model, c.seats, c.doors, c.bags, c.trans, true, true, c.cancel, c.age, '/assets/img/road.svg', c.price, 'TND',
       'https://www.example.com/' || c.slug || '/reservation?agence=' || c.code || '&modele=' || replace(c.model, ' ', '-'), true, now(), now()
FROM (VALUES
    (4, 'TUN', 'Aéroport Tunis-Carthage', 'CITY', 'Kia Picanto', 4, 5, 1, 'MANUAL', true, 21, 85, 'carthage-rent'),
    (5, 'TUN', 'Aéroport Tunis-Carthage', 'CITY', 'Hyundai i10', 4, 5, 1, 'AUTOMATIC', true, 21, 98, 'jasmin-cars'),
    (6, 'TUN', 'Aéroport Tunis-Carthage', 'COMPACT', 'Renault Clio', 5, 5, 2, 'MANUAL', false, 23, 115, 'sahara-auto'),
    (4, 'TUN', 'Aéroport Tunis-Carthage', 'SUV', 'Dacia Duster', 5, 5, 3, 'MANUAL', true, 25, 165, 'carthage-rent'),
    (5, 'TUN', 'Aéroport Tunis-Carthage', 'VAN', 'Peugeot Rifter', 7, 5, 4, 'MANUAL', true, 25, 190, 'jasmin-cars'),
    (5, 'DJE', 'Aéroport Djerba-Zarzis', 'CITY', 'Kia Picanto', 4, 5, 1, 'MANUAL', true, 21, 80, 'jasmin-cars'),
    (5, 'DJE', 'Aéroport Djerba-Zarzis', 'SUV', 'Hyundai Tucson', 5, 5, 3, 'AUTOMATIC', true, 25, 210, 'jasmin-cars'),
    (4, 'DJE', 'Aéroport Djerba-Zarzis', 'COMPACT', 'Volkswagen Polo', 5, 5, 2, 'MANUAL', true, 23, 110, 'carthage-rent'),
    (6, 'MIR', 'Aéroport Monastir', 'CITY', 'Fiat Panda', 4, 5, 1, 'MANUAL', true, 21, 78, 'sahara-auto'),
    (6, 'MIR', 'Aéroport Monastir', 'SEDAN', 'Peugeot 301', 5, 4, 3, 'MANUAL', true, 23, 125, 'sahara-auto'),
    (6, 'NBE', 'Aéroport Enfidha-Hammamet', 'COMPACT', 'Seat Ibiza', 5, 5, 2, 'MANUAL', true, 23, 105, 'sahara-auto'),
    (4, 'NBE', 'Aéroport Enfidha-Hammamet', 'SUV', 'Dacia Duster', 5, 5, 3, 'MANUAL', true, 25, 160, 'carthage-rent')
) AS c(partner_id, code, label, cat, model, seats, doors, bags, trans, cancel, age, price, slug);

INSERT INTO rental_offers (partner_id, destination_id, title, type, capacity, bedrooms, bathrooms, pool, wifi, sea_view, min_nights, description, image_url, price, currency, deeplink_url, active, created_at, updated_at)
SELECT 8, d.id, r.title, r.type, r.cap, r.bed, r.bath, r.pool, true, r.sea, r.minn, r.descr, r.img, r.price, 'TND',
       'https://www.example.com/villas-du-cap/' || r.ref, true, now(), now()
FROM (VALUES
    ('hammamet', 'Villa Yasmine avec piscine privée', 'VILLA', 8, 4, 3, true, false, 3, 'Grande villa avec jardin et piscine (démo).', '/assets/img/pool.svg', 650, 'villa-yasmine'),
    ('hammamet', 'Appartement front de mer', 'APARTMENT', 4, 2, 1, false, true, 2, 'Appartement lumineux face à la mer (démo).', '/assets/img/hammamet.svg', 220, 'appart-front-mer'),
    ('djerba', 'Houch familial à Midoun', 'HOUSE', 6, 3, 2, true, false, 3, 'Houch traditionnel avec piscine (démo).', '/assets/img/dar.svg', 380, 'houch-midoun'),
    ('sidi-bou-said', 'Dar bleu et blanc vue golfe', 'DAR', 4, 2, 2, false, true, 2, 'Maison traditionnelle avec terrasse vue mer (démo).', '/assets/img/sidibou.svg', 450, 'dar-golfe'),
    ('sousse', 'Appartement marina El Kantaoui', 'APARTMENT', 5, 2, 1, true, true, 2, 'Résidence avec piscine commune (démo).', '/assets/img/sousse.svg', 240, 'appart-marina')
) AS r(dest, title, type, cap, bed, bath, pool, sea, minn, descr, img, price, ref)
JOIN destinations d ON d.slug = r.dest;

INSERT INTO activity_offers (partner_id, destination_id, title, category, duration_hours, meeting_point, description, image_url, price, currency, deeplink_url, active, created_at, updated_at)
SELECT 9, d.id, a.title, a.cat, a.dur, a.meet, a.descr, a.img, a.price, 'TND',
       'https://www.example.com/sahara-experiences/' || a.ref, true, now(), now()
FROM (VALUES
    ('douz', 'Nuit sous tente au Sahara', 'DESERT', 20.0, 'Douz centre', 'Balade à dromadaire et dîner sous les étoiles (démo).', '/assets/img/sahara.svg', 290, 'nuit-sahara'),
    ('tozeur', 'Excursion oasis de montagne en 4x4', 'DESERT', 8.0, 'Hôtel à Tozeur', 'Chebika, Tamerza et Mides (démo).', '/assets/img/tozeur.svg', 160, 'oasis-4x4'),
    ('djerba', 'Sortie en mer et île aux flamants', 'WATER_SPORTS', 6.0, 'Port de Houmt Souk', 'Croisière, baignade et déjeuner (démo).', '/assets/img/djerba.svg', 95, 'ile-flamants'),
    ('hammamet', 'Journée thalasso et hammam', 'WELLNESS', 4.0, 'Hammamet Nord', 'Accès thalasso, hammam et massage (démo).', '/assets/img/pool.svg', 180, 'thalasso'),
    ('tunis', 'Visite guidée Carthage et Sidi Bou Saïd', 'CULTURE', 5.0, 'Gare TGM Carthage', 'Thermes d''Antonin, port punique, village bleu (démo).', '/assets/img/sidibou.svg', 85, 'carthage'),
    ('tabarka', 'Baptême de plongée', 'WATER_SPORTS', 3.0, 'Port de Tabarka', 'Plongée encadrée sur les fonds de corail (démo).', '/assets/img/tabarka.svg', 140, 'plongee')
) AS a(dest, title, cat, dur, meet, descr, img, price, ref)
JOIN destinations d ON d.slug = a.dest;
