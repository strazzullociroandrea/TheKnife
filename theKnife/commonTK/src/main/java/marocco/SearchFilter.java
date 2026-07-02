package marocco;

import sibilla.Cuisine;
import sibilla.Day;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Filter parameters for searching restaurant locations.
 * Immutable and serializable so it can travel safely over RMI.
 * Built via {@link Builder}, which validates the criteria on {@link Builder#build()}.
 *
 * @version 1.1
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchFilter implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String restaurantName;
    private final String locationName;
    private final Cuisine cuisineType;
    private final String country;
    private final String city;
    private final String address;
    /** Maximum price the customer is willing to pay (matched as {@code location.price_range <= maxPriceRange}). */
    private final Double maxPriceRange;
    private final Boolean delivery;
    private final Boolean takeaway;
    private final Integer maxCapacity;
    private final Boolean vegetarianMenu;
    private final Boolean veganMenu;
    private final Boolean glutenFreeMenu;
    /** Day the location must be open on. */
    private final Day openDay;
    /** Time of day (HH:mm) the location must be open at; only meaningful together with {@link #openDay}. */
    private final String openTime;
    private final Double latRef;
    private final Double lonRef;
    private final Double radiusKm;
    /** Address to geocode for distance search, used when the caller has no coordinates at hand. */
    private final String addressRef;
    private final Double minRating;
    /** Max number of results to return; null means no limit. */
    private final Integer limit;
    /** Number of results to skip, for pagination; null means no offset. */
    private final Integer offset;

    /**
     * SearchFilter constructor, copying every criterion from a validated {@link Builder}.
     *
     * @param builder the builder to copy criteria from
     */
    private SearchFilter(Builder builder) {
        this.restaurantName = builder.restaurantName;
        this.locationName = builder.locationName;
        this.cuisineType = builder.cuisineType;
        this.country = builder.country;
        this.city = builder.city;
        this.address = builder.address;
        this.maxPriceRange = builder.maxPriceRange;
        this.delivery = builder.delivery;
        this.takeaway = builder.takeaway;
        this.maxCapacity = builder.maxCapacity;
        this.vegetarianMenu = builder.vegetarianMenu;
        this.veganMenu = builder.veganMenu;
        this.glutenFreeMenu = builder.glutenFreeMenu;
        this.openDay = builder.openDay;
        this.openTime = builder.openTime;
        this.latRef = builder.latRef;
        this.lonRef = builder.lonRef;
        this.radiusKm = builder.radiusKm;
        this.addressRef = builder.addressRef;
        this.minRating = builder.minRating;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    /** @return the restaurant name to match, or null for any */
    public String getRestaurantName() { return restaurantName; }
    /** @return the location name to match, or null for any */
    public String getLocationName() { return locationName; }
    /** @return the required cuisine type, or null for any */
    public Cuisine getCuisineType() { return cuisineType; }
    /** @return the country to match, or null for any */
    public String getCountry() { return country; }
    /** @return the city to match, or null for any */
    public String getCity() { return city; }
    /** @return the address substring to match, or null for any */
    public String getAddress() { return address; }
    /** @return the maximum price the customer is willing to pay, or null for any */
    public Double getMaxPriceRange() { return maxPriceRange; }
    /** @return whether delivery must be available, or null to not filter on it */
    public Boolean getDelivery() { return delivery; }
    /** @return whether takeaway must be available, or null to not filter on it */
    public Boolean getTakeaway() { return takeaway; }
    /** @return the minimum seating capacity required, or null for any */
    public Integer getMaxCapacity() { return maxCapacity; }
    /** @return whether a vegetarian menu is required, or null to not filter on it */
    public Boolean getVegetarianMenu() { return vegetarianMenu; }
    /** @return whether a vegan menu is required, or null to not filter on it */
    public Boolean getVeganMenu() { return veganMenu; }
    /** @return whether a gluten-free menu is required, or null to not filter on it */
    public Boolean getGlutenFreeMenu() { return glutenFreeMenu; }
    /** @return the day the location must be open on, or null for any */
    public Day getOpenDay() { return openDay; }
    /** @return the time of day (HH:mm) the location must be open at, or null for any */
    public String getOpenTime() { return openTime; }
    /** @return the reference latitude for distance search, or null if unset */
    public Double getLatRef() { return latRef; }
    /** @return the reference longitude for distance search, or null if unset */
    public Double getLonRef() { return lonRef; }
    /** @return the search radius in km, or null if unset */
    public Double getRadiusKm() { return radiusKm; }
    /** @return the address to geocode for distance search, or null if unset */
    public String getAddressRef() { return addressRef; }
    /** @return the minimum average rating required, or null for any */
    public Double getMinRating() { return minRating; }
    /** @return the maximum number of results to return, or null for no limit */
    public Integer getLimit() { return limit; }
    /** @return the number of results to skip, for pagination, or null for none */
    public Integer getOffset() { return offset; }

    /**
     * @return true if a distance filter of either kind (coordinates or address) is specified
     */
    public boolean hasDistanceFilter() {
        return hasCoordinatesDistanceFilter() || hasAddressDistanceFilter();
    }

    /** @return true if latitude, longitude, and radius are all specified */
    public boolean hasCoordinatesDistanceFilter() {
        return latRef != null && lonRef != null && radiusKm != null;
    }

    /** @return true if an address and radius are specified */
    public boolean hasAddressDistanceFilter() {
        return addressRef != null && !addressRef.trim().isEmpty() && radiusKm != null;
    }

    /** @return true if pagination (limit and/or offset) was requested */
    public boolean hasPagination() {
        return limit != null || offset != null;
    }

    /**
     * @return a debug-friendly string listing every criterion's value
     */
    @Override
    public String toString() {
        return "SearchFilter{" +
                "restaurantName='" + restaurantName + '\'' +
                ", locationName='" + locationName + '\'' +
                ", cuisineType=" + cuisineType +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", maxPriceRange=" + maxPriceRange +
                ", delivery=" + delivery +
                ", takeaway=" + takeaway +
                ", maxCapacity=" + maxCapacity +
                ", vegetarianMenu=" + vegetarianMenu +
                ", veganMenu=" + veganMenu +
                ", glutenFreeMenu=" + glutenFreeMenu +
                ", openDay=" + openDay +
                ", openTime='" + openTime + '\'' +
                ", latRef=" + latRef +
                ", lonRef=" + lonRef +
                ", radiusKm=" + radiusKm +
                ", addressRef='" + addressRef + '\'' +
                ", minRating=" + minRating +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }

    /**
     * Builder for {@link SearchFilter}. Validates the assembled criteria in {@link #build()}.
     */
    public static class Builder {
        private String restaurantName;
        private String locationName;
        private Cuisine cuisineType;
        private String country;
        private String city;
        private String address;
        private Double maxPriceRange;
        private Boolean delivery;
        private Boolean takeaway;
        private Integer maxCapacity;
        private Boolean vegetarianMenu;
        private Boolean veganMenu;
        private Boolean glutenFreeMenu;
        private Day openDay;
        private String openTime;
        private Double latRef;
        private Double lonRef;
        private Double radiusKm;
        private String addressRef;
        private Double minRating;
        private Integer limit;
        private Integer offset;

        /** Builder constructor, starting with every criterion unset. */
        public Builder() {}

        /**
         * @param restaurantName the restaurant name to match, or null for any
         */
        public Builder restaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
            return this;
        }

        /**
         * @param locationName the location name to match, or null for any
         */
        public Builder locationName(String locationName) {
            this.locationName = locationName;
            return this;
        }

        /**
         * @param cuisineType the required cuisine type, or null for any
         */
        public Builder cuisineType(Cuisine cuisineType) {
            this.cuisineType = cuisineType;
            return this;
        }

        /**
         * @param country the country to match, or null for any
         */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        /**
         * @param city the city to match, or null for any
         */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /**
         * @param address the address substring to match, or null for any
         */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * @param maxPriceRange the maximum price the customer is willing to pay
         */
        public Builder maxPriceRange(Double maxPriceRange) {
            this.maxPriceRange = maxPriceRange;
            return this;
        }

        /**
         * @param delivery whether delivery must be available, or null to not filter on it
         */
        public Builder delivery(Boolean delivery) {
            this.delivery = delivery;
            return this;
        }

        /**
         * @param takeaway whether takeaway must be available, or null to not filter on it
         */
        public Builder takeaway(Boolean takeaway) {
            this.takeaway = takeaway;
            return this;
        }

        /**
         * @param maxCapacity the minimum seating capacity required of the location
         */
        public Builder maxCapacity(Integer maxCapacity) {
            this.maxCapacity = maxCapacity;
            return this;
        }

        /**
         * @param vegetarianMenu whether a vegetarian menu is required, or null to not filter on it
         */
        public Builder vegetarianMenu(Boolean vegetarianMenu) {
            this.vegetarianMenu = vegetarianMenu;
            return this;
        }

        /**
         * @param veganMenu whether a vegan menu is required, or null to not filter on it
         */
        public Builder veganMenu(Boolean veganMenu) {
            this.veganMenu = veganMenu;
            return this;
        }

        /**
         * @param glutenFreeMenu whether a gluten-free menu is required, or null to not filter on it
         */
        public Builder glutenFreeMenu(Boolean glutenFreeMenu) {
            this.glutenFreeMenu = glutenFreeMenu;
            return this;
        }

        /**
         * @param openDay the day the location must be open on, or null for any
         */
        public Builder openDay(Day openDay) {
            this.openDay = openDay;
            return this;
        }

        /**
         * Requires {@link #openDay(Day)} to also be set.
         *
         * @param openTime the time of day, formatted "HH:mm", the location must be open at
         */
        public Builder openTime(String openTime) {
            this.openTime = openTime;
            return this;
        }

        /**
         * @param latRef the reference latitude
         * @param lonRef the reference longitude
         * @param radiusKm the search radius in km
         */
        public Builder distance(Double latRef, Double lonRef, Double radiusKm) {
            this.latRef = latRef;
            this.lonRef = lonRef;
            this.radiusKm = radiusKm;
            return this;
        }

        /**
         * Used when the caller has no coordinates at hand; the server resolves the
         * address to coordinates via geocoding before running the search.
         *
         * @param address the address to geocode
         * @param radiusKm the search radius in km
         */
        public Builder distanceFromAddress(String address, Double radiusKm) {
            this.addressRef = address;
            this.radiusKm = radiusKm;
            return this;
        }

        /**
         * @param minRating the minimum average rating required, or null for any
         */
        public Builder minRating(Double minRating) {
            this.minRating = minRating;
            return this;
        }

        /**
         * @param pageNumber zero-based page index
         * @param pageSize number of results per page
         */
        public Builder page(int pageNumber, int pageSize) {
            this.limit = pageSize;
            this.offset = pageNumber * pageSize;
            return this;
        }

        /**
         * Validates the assembled criteria and builds the {@link SearchFilter}.
         *
         * @throws IllegalArgumentException if any criteria is out of its valid range
         */
        public SearchFilter build() {
            validate();
            return new SearchFilter(this);
        }

        /**
         * Function to validate the assembled criteria, checking value ranges and that mutually
         * dependent criteria (e.g. {@code openTime} requiring {@code openDay}) are set together.
         *
         * @throws IllegalArgumentException if any criteria is out of its valid range
         */
        private void validate() {
            if (minRating != null && (minRating < 0 || minRating > 5)) {
                throw new IllegalArgumentException("minRating must be between 0 and 5, got " + minRating);
            }
            if (radiusKm != null && radiusKm <= 0) {
                throw new IllegalArgumentException("radiusKm must be positive, got " + radiusKm);
            }
            if ((latRef != null || lonRef != null) && !(latRef != null && lonRef != null && radiusKm != null)) {
                throw new IllegalArgumentException("distance(latRef, lonRef, radiusKm) requires latitude, longitude, and radiusKm");
            }
            if (addressRef != null && addressRef.trim().isEmpty()) {
                throw new IllegalArgumentException("addressRef cannot be blank");
            }
            if (addressRef != null && radiusKm == null) {
                throw new IllegalArgumentException("distanceFromAddress(address, radiusKm) requires a radiusKm");
            }
            if (addressRef != null && (latRef != null || lonRef != null)) {
                throw new IllegalArgumentException("Use either coordinate distance or address distance, not both");
            }
            if (maxCapacity != null && maxCapacity <= 0) {
                throw new IllegalArgumentException("maxCapacity must be positive, got " + maxCapacity);
            }
            if (maxPriceRange != null && maxPriceRange < 0) {
                throw new IllegalArgumentException("maxPriceRange cannot be negative, got " + maxPriceRange);
            }
            if (openTime != null && openDay == null) {
                throw new IllegalArgumentException("openTime requires openDay to be set");
            }
            if (openTime != null) {
                try {
                    LocalTime.parse(openTime.trim());
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("openTime must be formatted as HH:mm, got " + openTime, e);
                }
            }
            if (limit != null && limit <= 0) {
                throw new IllegalArgumentException("limit must be positive, got " + limit);
            }
            if (offset != null && offset < 0) {
                throw new IllegalArgumentException("offset cannot be negative, got " + offset);
            }
        }
    }
}
