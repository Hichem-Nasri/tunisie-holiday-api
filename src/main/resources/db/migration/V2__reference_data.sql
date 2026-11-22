INSERT INTO airports (code, name, city, city_code, country_code) VALUES
('TUN', 'Aéroport Tunis-Carthage', 'Tunis', 'TUN', 'TN'),
('DJE', 'Aéroport Djerba-Zarzis', 'Djerba', 'DJE', 'TN'),
('MIR', 'Aéroport Monastir Habib-Bourguiba', 'Monastir', 'MIR', 'TN'),
('NBE', 'Aéroport Enfidha-Hammamet', 'Enfidha', 'NBE', 'TN'),
('SFA', 'Aéroport Sfax-Thyna', 'Sfax', 'SFA', 'TN'),
('TOE', 'Aéroport Tozeur-Nefta', 'Tozeur', 'TOE', 'TN'),
('TBJ', 'Aéroport Tabarka-Aïn Draham', 'Tabarka', 'TBJ', 'TN'),
('GAF', 'Aéroport Gafsa-Ksar', 'Gafsa', 'GAF', 'TN'),
('ORY', 'Paris-Orly', 'Paris', 'PAR', 'FR'),
('CDG', 'Paris-Charles de Gaulle', 'Paris', 'PAR', 'FR'),
('LYS', 'Lyon-Saint Exupéry', 'Lyon', 'LYS', 'FR'),
('MRS', 'Marseille Provence', 'Marseille', 'MRS', 'FR'),
('NCE', 'Nice Côte d''Azur', 'Nice', 'NCE', 'FR'),
('TLS', 'Toulouse-Blagnac', 'Toulouse', 'TLS', 'FR'),
('NTE', 'Nantes Atlantique', 'Nantes', 'NTE', 'FR'),
('BRU', 'Bruxelles-National', 'Bruxelles', 'BRU', 'BE'),
('GVA', 'Genève', 'Genève', 'GVA', 'CH'),
('FRA', 'Francfort', 'Francfort', 'FRA', 'DE'),
('FCO', 'Rome Fiumicino', 'Rome', 'ROM', 'IT'),
('MXP', 'Milan Malpensa', 'Milan', 'MIL', 'IT'),
('LHR', 'Londres Heathrow', 'Londres', 'LON', 'GB'),
('YUL', 'Montréal-Trudeau', 'Montréal', 'YMQ', 'CA'),
('IST', 'Istanbul', 'Istanbul', 'IST', 'TR'),
('DXB', 'Dubaï', 'Dubaï', 'DXB', 'AE'),
('ALG', 'Alger Houari-Boumédiène', 'Alger', 'ALG', 'DZ'),
('CMN', 'Casablanca Mohammed V', 'Casablanca', 'CAS', 'MA');

INSERT INTO destinations (name, slug, region, tagline, description, image_url, airport_code, popular, sort_order, created_at, updated_at) VALUES
('Djerba', 'djerba', 'Médenine', 'Plages et île', 'L''île des rêves : plages de sable fin, houchs traditionnels et la synagogue de la Ghriba.', '/assets/img/djerba.svg', 'DJE', true, 1, now(), now()),
('Hammamet', 'hammamet', 'Nabeul', 'Balnéaire et médina', 'Station balnéaire emblématique, sa médina fortifiée et Yasmine Hammamet.', '/assets/img/hammamet.svg', 'NBE', true, 2, now(), now()),
('Sousse', 'sousse', 'Sousse', 'Port El Kantaoui', 'La perle du Sahel, sa médina classée à l''UNESCO et la marina de Port El Kantaoui.', '/assets/img/sousse.svg', 'MIR', true, 3, now(), now()),
('Tozeur', 'tozeur', 'Tozeur', 'Oasis et désert', 'Porte du Sahara : palmeraies, chott El Jérid et oasis de montagne.', '/assets/img/tozeur.svg', 'TOE', true, 4, now(), now()),
('Sidi Bou Saïd', 'sidi-bou-said', 'Tunis', 'Village bleu et blanc', 'Village perché aux portes bleues, face au golfe de Tunis.', '/assets/img/sidibou.svg', 'TUN', true, 5, now(), now()),
('Tabarka', 'tabarka', 'Jendouba', 'Forêts et plongée', 'Côte de corail, forêts de chênes-lièges et fonds marins préservés.', '/assets/img/tabarka.svg', 'TBJ', true, 6, now(), now()),
('Tunis', 'tunis', 'Tunis', 'Médina et culture', 'La capitale, sa médina, le musée du Bardo et Carthage.', '/assets/img/sidibou.svg', 'TUN', false, 7, now(), now()),
('Monastir', 'monastir', 'Monastir', 'Ribat et plages', 'Ville côtière, son ribat et ses plages familiales.', '/assets/img/sousse.svg', 'MIR', false, 8, now(), now()),
('Douz', 'douz', 'Kébili', 'Porte du Sahara', 'Point de départ des excursions dans le Grand Erg oriental.', '/assets/img/sahara.svg', 'TOE', false, 9, now(), now());

INSERT INTO exchange_rates (currency, label, symbol, rate_to_tnd, updated_at) VALUES
('TND', 'Dinar tunisien', 'DT', 1.000000, now()),
('EUR', 'Euro', '€', 3.400000, now()),
('USD', 'Dollar américain', '$', 3.100000, now()),
('GBP', 'Livre sterling', '£', 3.950000, now());
