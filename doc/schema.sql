-- TheKnife PostgreSQL schema.
-- Destructive: drops and recreates everything. Run only on an empty/dev database.

DROP TRIGGER  IF EXISTS trg_review_like_delete  ON review_like;
DROP TRIGGER  IF EXISTS trg_review_like_insert  ON review_like;
DROP FUNCTION IF EXISTS sync_like_count();

DROP VIEW  IF EXISTS view_location_rating    CASCADE;
DROP VIEW  IF EXISTS view_restaurant_rating  CASCADE;
DROP VIEW  IF EXISTS vista_media_stelle      CASCADE;

DROP TABLE IF EXISTS booking            CASCADE;
DROP TABLE IF EXISTS review_like        CASCADE;
DROP TABLE IF EXISTS review_reply       CASCADE;
DROP TABLE IF EXISTS review             CASCADE;
DROP TABLE IF EXISTS favourite          CASCADE;
DROP TABLE IF EXISTS photo              CASCADE;
DROP TABLE IF EXISTS location           CASCADE;
DROP TABLE IF EXISTS restaurant_owner   CASCADE;
DROP TABLE IF EXISTS restaurant         CASCADE;
DROP TABLE IF EXISTS app_user           CASCADE;

DROP TYPE IF EXISTS booking_status  CASCADE;
DROP TYPE IF EXISTS user_role       CASCADE;


CREATE TYPE user_role       AS ENUM ('customer', 'manager');
CREATE TYPE booking_status  AS ENUM ('confirmed', 'waiting', 'cancelled', 'expired');


-- A user is either a customer or a restaurant manager.
CREATE TABLE app_user (
    user_id         CHAR(36)        PRIMARY KEY,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    date_of_birth   DATE,
    city            VARCHAR(150),
    role            user_role       NOT NULL
);

CREATE INDEX idx_user_email ON app_user (email);
CREATE INDEX idx_user_role  ON app_user (role);


-- A restaurant is just the brand. Its branches are in the location table.
CREATE TABLE restaurant (
    restaurant_id   CHAR(36)        PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    cuisine_type    VARCHAR(100)
);

CREATE INDEX idx_restaurant_name    ON restaurant (name);
CREATE INDEX idx_restaurant_cuisine ON restaurant (cuisine_type);


-- A restaurant can have multiple managers, and a manager can co-own multiple restaurants.
CREATE TABLE restaurant_owner (
    restaurant_id   CHAR(36)    NOT NULL
        REFERENCES restaurant (restaurant_id) ON DELETE CASCADE,
    user_id         CHAR(36)    NOT NULL
        REFERENCES app_user (user_id) ON DELETE CASCADE,
    PRIMARY KEY (restaurant_id, user_id)
);

CREATE INDEX idx_owner_restaurant ON restaurant_owner (restaurant_id);
CREATE INDEX idx_owner_user       ON restaurant_owner (user_id);


-- A single physical branch of a restaurant.
-- opening_hours is JSONB shaped like { "monday": "09:00-22:00", ... }, validated by the application.
CREATE TABLE location (
    location_id         CHAR(36)        PRIMARY KEY,
    restaurant_id       CHAR(36)        NOT NULL
        REFERENCES restaurant (restaurant_id) ON DELETE CASCADE,
    name                VARCHAR(255)    NOT NULL,
    country             VARCHAR(100)    NOT NULL,
    city                VARCHAR(150)    NOT NULL,
    address             VARCHAR(255)    NOT NULL,
    latitude            DECIMAL(10,7)   NOT NULL,
    longitude           DECIMAL(10,7)   NOT NULL,
    price_range         DECIMAL(8,2),
    delivery            BOOLEAN         NOT NULL DEFAULT FALSE,
    takeaway            BOOLEAN         NOT NULL DEFAULT FALSE,
    max_capacity        INTEGER         NOT NULL CHECK (max_capacity > 0),
    opening_hours       JSONB,
    vegetarian_menu     BOOLEAN         NOT NULL DEFAULT FALSE,
    vegan_menu          BOOLEAN         NOT NULL DEFAULT FALSE,
    gluten_free_menu    BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_location_restaurant ON location (restaurant_id);
CREATE INDEX idx_location_city       ON location (city);
CREATE INDEX idx_location_geo        ON location (latitude, longitude);
CREATE INDEX idx_location_price      ON location (price_range);
CREATE INDEX idx_location_delivery   ON location (delivery);


-- Each location has its own public photo gallery.
CREATE TABLE photo (
    photo_id        CHAR(36)        PRIMARY KEY,
    location_id     CHAR(36)        NOT NULL
        REFERENCES location (location_id) ON DELETE CASCADE,
    url             VARCHAR(500)    NOT NULL,
    uploaded_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_photo_location ON photo (location_id);


-- Locations a user bookmarked.
CREATE TABLE favourite (
    user_id         CHAR(36)    NOT NULL
        REFERENCES app_user (user_id) ON DELETE CASCADE,
    location_id     CHAR(36)    NOT NULL
        REFERENCES location (location_id) ON DELETE CASCADE,
    added_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, location_id)
);

CREATE INDEX idx_favourite_user     ON favourite (user_id);
CREATE INDEX idx_favourite_location ON favourite (location_id);


-- One review per user per location.
CREATE TABLE review (
    review_id           CHAR(36)    PRIMARY KEY,
    user_id              CHAR(36)    NOT NULL
        REFERENCES app_user (user_id) ON DELETE CASCADE,
    location_id          CHAR(36)    NOT NULL
        REFERENCES location (location_id) ON DELETE CASCADE,
    rating               SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    rating_price         SMALLINT    CHECK (rating_price BETWEEN 0 AND 5),
    rating_hospitality   SMALLINT    CHECK (rating_hospitality BETWEEN 0 AND 5),
    rating_service       SMALLINT    CHECK (rating_service BETWEEN 0 AND 5),
    body                 TEXT,
    created_at           TIMESTAMP   NOT NULL DEFAULT NOW(),
    like_count           INTEGER     NOT NULL DEFAULT 0 CHECK (like_count >= 0),
    UNIQUE (user_id, location_id)
);

CREATE INDEX idx_review_location ON review (location_id);
CREATE INDEX idx_review_user     ON review (user_id);
CREATE INDEX idx_review_rating   ON review (rating);


-- A manager's reply to a review, at most one per review.
CREATE TABLE review_reply (
    review_id   CHAR(36)    PRIMARY KEY
        REFERENCES review (review_id) ON DELETE CASCADE,
    manager_id  CHAR(36)    NOT NULL
        REFERENCES app_user (user_id) ON DELETE RESTRICT,
    body        TEXT        NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reply_manager ON review_reply (manager_id);


CREATE TABLE review_like (
    user_id     CHAR(36)    NOT NULL
        REFERENCES app_user (user_id) ON DELETE CASCADE,
    review_id   CHAR(36)    NOT NULL
        REFERENCES review (review_id) ON DELETE CASCADE,
    liked_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, review_id)
);

CREATE INDEX idx_review_like_review ON review_like (review_id);
CREATE INDEX idx_review_like_user   ON review_like (user_id);


CREATE TABLE booking (
    booking_id          CHAR(36)            PRIMARY KEY,
    user_id              CHAR(36)            NOT NULL
        REFERENCES app_user (user_id) ON DELETE CASCADE,
    location_id          CHAR(36)            NOT NULL
        REFERENCES location (location_id) ON DELETE RESTRICT,
    booking_date         DATE                NOT NULL,
    time_slot            TIME                NOT NULL,
    seats                SMALLINT            NOT NULL CHECK (seats > 0),
    status               booking_status      NOT NULL DEFAULT 'confirmed',
    waiting_position     INTEGER             CHECK (waiting_position > 0)
);

CREATE INDEX idx_booking_user     ON booking (user_id);
CREATE INDEX idx_booking_location ON booking (location_id);
CREATE INDEX idx_booking_slot     ON booking (location_id, booking_date, time_slot);
CREATE INDEX idx_booking_status   ON booking (status);


-- Keeps review.like_count in sync whenever a review_like row is added or removed.
CREATE OR REPLACE FUNCTION sync_like_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE review SET like_count = like_count + 1
        WHERE review_id = NEW.review_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE review SET like_count = like_count - 1
        WHERE review_id = OLD.review_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_review_like_insert
    AFTER INSERT ON review_like
    FOR EACH ROW EXECUTE FUNCTION sync_like_count();

CREATE TRIGGER trg_review_like_delete
    AFTER DELETE ON review_like
    FOR EACH ROW EXECUTE FUNCTION sync_like_count();


-- Average rating and review count per location.
CREATE VIEW view_location_rating AS
SELECT
    l.location_id,
    l.city,
    l.address,
    r.name                              AS restaurant_name,
    r.cuisine_type,
    ROUND(AVG(rv.rating)::NUMERIC, 2)   AS avg_rating,
    COUNT(rv.review_id)                 AS review_count
FROM location l
JOIN  restaurant r  ON r.restaurant_id = l.restaurant_id
LEFT JOIN review rv ON rv.location_id  = l.location_id
GROUP BY l.location_id, l.city, l.address, r.name, r.cuisine_type;
