CREATE TABLE partners (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    email        VARCHAR(180) NOT NULL,
    phone        VARCHAR(40),
    website      VARCHAR(300),
    city         VARCHAR(100),
    logo_url     VARCHAR(500),
    description  TEXT,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL
);

CREATE TABLE app_users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    partner_id    BIGINT REFERENCES partners (id),
    enabled       BOOLEAN      NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE destinations (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(120) NOT NULL,
    slug         VARCHAR(120) NOT NULL UNIQUE,
    region       VARCHAR(120),
    tagline      VARCHAR(200),
    description  TEXT,
    image_url    VARCHAR(500),
    airport_code VARCHAR(3),
    popular      BOOLEAN      NOT NULL,
    sort_order   INTEGER      NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL
);

CREATE TABLE airports (
    code         VARCHAR(3)   PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    city_code    VARCHAR(3)   NOT NULL,
    country_code VARCHAR(2)   NOT NULL
);

CREATE TABLE hotels (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(180) NOT NULL,
    destination_id BIGINT       NOT NULL REFERENCES destinations (id),
    type           VARCHAR(20)  NOT NULL,
    stars          INTEGER      NOT NULL,
    address        VARCHAR(300),
    location_label VARCHAR(200),
    description    TEXT,
    image_url      VARCHAR(500),
    rating         NUMERIC(3, 1),
    review_count   INTEGER      NOT NULL,
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

CREATE TABLE hotel_amenities (
    hotel_id BIGINT      NOT NULL REFERENCES hotels (id) ON DELETE CASCADE,
    amenity  VARCHAR(60) NOT NULL,
    PRIMARY KEY (hotel_id, amenity)
);

CREATE TABLE hotel_offers (
    id                BIGSERIAL PRIMARY KEY,
    partner_id        BIGINT         NOT NULL REFERENCES partners (id),
    hotel_id          BIGINT         NOT NULL REFERENCES hotels (id),
    board             VARCHAR(20)    NOT NULL,
    room_type         VARCHAR(120)   NOT NULL,
    max_guests        INTEGER        NOT NULL,
    free_cancellation BOOLEAN        NOT NULL,
    price             NUMERIC(12, 2) NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    deeplink_url      VARCHAR(1000)  NOT NULL,
    active            BOOLEAN        NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL
);

CREATE TABLE flight_offers (
    id               BIGSERIAL PRIMARY KEY,
    partner_id       BIGINT         NOT NULL REFERENCES partners (id),
    airline_code     VARCHAR(3)     NOT NULL,
    airline_name     VARCHAR(100)   NOT NULL,
    flight_number    VARCHAR(10),
    origin_code      VARCHAR(3)     NOT NULL,
    destination_code VARCHAR(3)     NOT NULL,
    departure_at     TIMESTAMP      NOT NULL,
    arrival_at       TIMESTAMP      NOT NULL,
    stops            INTEGER        NOT NULL,
    cabin            VARCHAR(20)    NOT NULL,
    baggage_included BOOLEAN        NOT NULL,
    seats_available  INTEGER        NOT NULL,
    price            NUMERIC(12, 2) NOT NULL,
    currency         VARCHAR(3)     NOT NULL,
    deeplink_url     VARCHAR(1000)  NOT NULL,
    active           BOOLEAN        NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_flight_route ON flight_offers (origin_code, destination_code, departure_at);

CREATE TABLE car_offers (
    id                BIGSERIAL PRIMARY KEY,
    partner_id        BIGINT         NOT NULL REFERENCES partners (id),
    pickup_code       VARCHAR(20)    NOT NULL,
    pickup_label      VARCHAR(150)   NOT NULL,
    category          VARCHAR(20)    NOT NULL,
    model             VARCHAR(100)   NOT NULL,
    seats             INTEGER        NOT NULL,
    doors             INTEGER        NOT NULL,
    bags              INTEGER        NOT NULL,
    transmission      VARCHAR(20)    NOT NULL,
    air_conditioning  BOOLEAN        NOT NULL,
    unlimited_mileage BOOLEAN        NOT NULL,
    free_cancellation BOOLEAN        NOT NULL,
    min_driver_age    INTEGER        NOT NULL,
    image_url         VARCHAR(500),
    price             NUMERIC(12, 2) NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    deeplink_url      VARCHAR(1000)  NOT NULL,
    active            BOOLEAN        NOT NULL,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_car_pickup ON car_offers (pickup_code);

CREATE TABLE rental_offers (
    id             BIGSERIAL PRIMARY KEY,
    partner_id     BIGINT         NOT NULL REFERENCES partners (id),
    destination_id BIGINT         NOT NULL REFERENCES destinations (id),
    title          VARCHAR(180)   NOT NULL,
    type           VARCHAR(20)    NOT NULL,
    capacity       INTEGER        NOT NULL,
    bedrooms       INTEGER        NOT NULL,
    bathrooms      INTEGER        NOT NULL,
    pool           BOOLEAN        NOT NULL,
    wifi           BOOLEAN        NOT NULL,
    sea_view       BOOLEAN        NOT NULL,
    min_nights     INTEGER        NOT NULL,
    description    TEXT,
    image_url      VARCHAR(500),
    price          NUMERIC(12, 2) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    deeplink_url   VARCHAR(1000)  NOT NULL,
    active         BOOLEAN        NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL
);

CREATE TABLE activity_offers (
    id             BIGSERIAL PRIMARY KEY,
    partner_id     BIGINT         NOT NULL REFERENCES partners (id),
    destination_id BIGINT         NOT NULL REFERENCES destinations (id),
    title          VARCHAR(180)   NOT NULL,
    category       VARCHAR(20)    NOT NULL,
    duration_hours NUMERIC(4, 1)  NOT NULL,
    meeting_point  VARCHAR(200),
    description    TEXT,
    image_url      VARCHAR(500),
    price          NUMERIC(12, 2) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    deeplink_url   VARCHAR(1000)  NOT NULL,
    active         BOOLEAN        NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ    NOT NULL
);

CREATE TABLE click_events (
    id           BIGSERIAL PRIMARY KEY,
    offer_type   VARCHAR(20) NOT NULL,
    offer_id     BIGINT,
    partner_id   BIGINT,
    source       VARCHAR(20) NOT NULL,
    visitor_hash VARCHAR(64),
    user_agent   VARCHAR(300),
    created_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_click_partner_date ON click_events (partner_id, created_at);
CREATE INDEX idx_click_date ON click_events (created_at);

CREATE TABLE exchange_rates (
    currency    VARCHAR(3)     PRIMARY KEY,
    label       VARCHAR(60)    NOT NULL,
    symbol      VARCHAR(8)     NOT NULL,
    rate_to_tnd NUMERIC(14, 6) NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL
);

CREATE TABLE price_alerts (
    id               BIGSERIAL PRIMARY KEY,
    email            VARCHAR(180)   NOT NULL,
    origin_code      VARCHAR(3)     NOT NULL,
    destination_code VARCHAR(3)     NOT NULL,
    departure_date   DATE           NOT NULL,
    return_date      DATE,
    max_price        NUMERIC(12, 2),
    currency         VARCHAR(3)     NOT NULL,
    active           BOOLEAN        NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL
);
