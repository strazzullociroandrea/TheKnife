package marocco;

import sibilla.Cuisine;
import sibilla.Day;

import java.io.Serial;
import java.io.Serializable;

/**
 * Filter parameters for searching restaurant locations.
 * Includes criteria for every attribute of the location (sede) and the restaurant name.
 * Uses the Builder design pattern to allow flexible instantiation of search criteria.
 * This class is immutable and serializable to ensure safe remote communication via RMI.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA - author of this file
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchFilter implements Serializable {
    /**
     * Unique identifier for serialization to ensure that a loaded class corresponds
     * exactly to the serialized object.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The name of the restaurant.
     */
    private final String restaurantName;

    /**
     * The type of cuisine.
     */
    private final Cuisine cuisineType;

    /**
     * The country of the location.
     */
    private final String country;

    /**
     * The city of the location.
     */
    private final String city;

    /**
     * The address of the location.
     */
    private final String address;

    /**
     * The price range indicator of the location.
     */
    private final Integer priceRange;

    /**
     * Whether delivery service is available.
     */
    private final Boolean delivery;

    /**
     * Whether takeaway service is available.
     */
    private final Boolean takeaway;

    /**
     * The maximum capacity of the location.
     */
    private final Integer maxCapacity;

    /**
     * Whether a vegetarian menu is available.
     */
    private final Boolean vegetarianMenu;

    /**
     * Whether a vegan menu is available.
     */
    private final Boolean veganMenu;

    /**
     * Whether a gluten-free menu is available.
     */
    private final Boolean glutenFreeMenu;

    /**
     * The operating day filter to check if open.
     */
    private final Day openDay;

    /**
     * The reference latitude coordinate for distance search.
     */
    private final Double latRef;

    /**
     * The reference longitude coordinate for distance search.
     */
    private final Double lonRef;

    /**
     * The search radius in kilometers.
     */
    private final Double radiusKm;

    /**
     * The minimum rating filter score.
     */
    private final Double minRating;

    /**
     * SearchFilter constructor to create filters using the builder.
     *
     * @param builder the builder helper
     */
    private SearchFilter(Builder builder) {
        this.restaurantName = builder.restaurantName;
        this.cuisineType = builder.cuisineType;
        this.country = builder.country;
        this.city = builder.city;
        this.address = builder.address;
        this.priceRange = builder.priceRange;
        this.delivery = builder.delivery;
        this.takeaway = builder.takeaway;
        this.maxCapacity = builder.maxCapacity;
        this.vegetarianMenu = builder.vegetarianMenu;
        this.veganMenu = builder.veganMenu;
        this.glutenFreeMenu = builder.glutenFreeMenu;
        this.openDay = builder.openDay;
        this.latRef = builder.latRef;
        this.lonRef = builder.lonRef;
        this.radiusKm = builder.radiusKm;
        this.minRating = builder.minRating;
    }

    /**
     * Function to return the restaurant name.
     *
     * @return the restaurant name
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * Function to return the cuisine type.
     *
     * @return the cuisine type
     */
    public Cuisine getCuisineType() {
        return cuisineType;
    }

    /**
     * Function to return the country.
     *
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Function to return the city.
     *
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Function to return the address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Function to return the price range.
     *
     * @return the price range
     */
    public Integer getPriceRange() {
        return priceRange;
    }

    /**
     * Function to return whether delivery is available.
     *
     * @return true if delivery is available, false otherwise
     */
    public Boolean getDelivery() {
        return delivery;
    }

    /**
     * Function to return whether takeaway is available.
     *
     * @return true if takeaway is available, false otherwise
     */
    public Boolean getTakeaway() {
        return takeaway;
    }

    /**
     * Function to return the maximum capacity.
     *
     * @return the maximum capacity
     */
    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Function to return whether vegetarian menu is available.
     *
     * @return true if vegetarian menu is available, false otherwise
     */
    public Boolean getVegetarianMenu() {
        return vegetarianMenu;
    }

    /**
     * Function to return whether vegan menu is available.
     *
     * @return true if vegan menu is available, false otherwise
     */
    public Boolean getVeganMenu() {
        return veganMenu;
    }

    /**
     * Function to return whether gluten-free menu is available.
     *
     * @return true if gluten-free menu is available, false otherwise
     */
    public Boolean getGlutenFreeMenu() {
        return glutenFreeMenu;
    }

    /**
     * Function to return the operating day filter.
     *
     * @return the operating day
     */
    public Day getOpenDay() {
        return openDay;
    }

    /**
     * Function to return the reference latitude.
     *
     * @return the reference latitude
     */
    public Double getLatRef() {
        return latRef;
    }

    /**
     * Function to return the reference longitude.
     *
     * @return the reference longitude
     */
    public Double getLonRef() {
        return lonRef;
    }

    /**
     * Function to return the search radius in kilometers.
     *
     * @return the radius in km
     */
    public Double getRadiusKm() {
        return radiusKm;
    }

    /**
     * Function to return the minimum rating.
     *
     * @return the minimum rating
     */
    public Double getMinRating() {
        return minRating;
    }

    /**
     * Function to check if distance-based filters are active.
     *
     * @return true if latitude, longitude, and radius are all specified
     */
    public boolean hasDistanceFilter() {
        return latRef != null && lonRef != null && radiusKm != null;
    }

    /**
     * Function to return a string representation of the SearchFilter.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "SearchFilter{" +
                "restaurantName='" + restaurantName + '\'' +
                ", cuisineType=" + cuisineType +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", priceRange=" + priceRange +
                ", delivery=" + delivery +
                ", takeaway=" + takeaway +
                ", maxCapacity=" + maxCapacity +
                ", vegetarianMenu=" + vegetarianMenu +
                ", veganMenu=" + veganMenu +
                ", glutenFreeMenu=" + glutenFreeMenu +
                ", openDay=" + openDay +
                ", latRef=" + latRef +
                ", lonRef=" + lonRef +
                ", radiusKm=" + radiusKm +
                ", minRating=" + minRating +
                '}';
    }

    /**
     * Builder class for {@link SearchFilter}.
     * Provides functions to set criteria parameters fluently.
     */
    public static class Builder {
        /**
         * The name of the restaurant.
         */
        private String restaurantName;

        /**
         * The type of cuisine.
         */
        private Cuisine cuisineType;

        /**
         * The country of the location.
         */
        private String country;

        /**
         * The city of the location.
         */
        private String city;

        /**
         * The address of the location.
         */
        private String address;

        /**
         * The price range indicator.
         */
        private Integer priceRange;

        /**
         * Whether delivery is available.
         */
        private Boolean delivery;

        /**
         * Whether takeaway is available.
         */
        private Boolean takeaway;

        /**
         * The maximum capacity.
         */
        private Integer maxCapacity;

        /**
         * Whether a vegetarian menu is available.
         */
        private Boolean vegetarianMenu;

        /**
         * Whether a vegan menu is available.
         */
        private Boolean veganMenu;

        /**
         * Whether a gluten-free menu is available.
         */
        private Boolean glutenFreeMenu;

        /**
         * The day when the location must be open.
         */
        private Day openDay;

        /**
         * The reference latitude coordinate.
         */
        private Double latRef;

        /**
         * The reference longitude coordinate.
         */
        private Double lonRef;

        /**
         * The search radius.
         */
        private Double radiusKm;

        /**
         * The minimum rating.
         */
        private Double minRating;

        /**
         * Builder constructor to create a new builder.
         */
        public Builder() {}

        /**
         * Function to set the restaurant name filter.
         *
         * @param restaurantName the restaurant name
         * @return this builder instance
         */
        public Builder restaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
            return this;
        }

        /**
         * Function to set the cuisine type filter.
         *
         * @param cuisineType the cuisine type
         * @return this builder instance
         */
        public Builder cuisineType(Cuisine cuisineType) {
            this.cuisineType = cuisineType;
            return this;
        }

        /**
         * Function to set the country filter.
         *
         * @param country the country
         * @return this builder instance
         */
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        /**
         * Function to set the city filter.
         *
         * @param city the city
         * @return this builder instance
         */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /**
         * Function to set the address filter.
         *
         * @param address the address
         * @return this builder instance
         */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * Function to set the price range filter.
         *
         * @param priceRange the price range
         * @return this builder instance
         */
        public Builder priceRange(Integer priceRange) {
            this.priceRange = priceRange;
            return this;
        }

        /**
         * Function to set whether delivery is required.
         *
         * @param delivery whether delivery is required
         * @return this builder instance
         */
        public Builder delivery(Boolean delivery) {
            this.delivery = delivery;
            return this;
        }

        /**
         * Function to set whether takeaway is required.
         *
         * @param takeaway whether takeaway is required
         * @return this builder instance
         */
        public Builder takeaway(Boolean takeaway) {
            this.takeaway = takeaway;
            return this;
        }

        /**
         * Function to set the minimum capacity required.
         *
         * @param maxCapacity the minimum capacity
         * @return this builder instance
         */
        public Builder maxCapacity(Integer maxCapacity) {
            this.maxCapacity = maxCapacity;
            return this;
        }

        /**
         * Function to set whether vegetarian menu is required.
         *
         * @param vegetarianMenu whether vegetarian menu is required
         * @return this builder instance
         */
        public Builder vegetarianMenu(Boolean vegetarianMenu) {
            this.vegetarianMenu = vegetarianMenu;
            return this;
        }

        /**
         * Function to set whether vegan menu is required.
         *
         * @param veganMenu whether vegan menu is required
         * @return this builder instance
         */
        public Builder veganMenu(Boolean veganMenu) {
            this.veganMenu = veganMenu;
            return this;
        }

        /**
         * Function to set whether gluten-free menu is required.
         *
         * @param glutenFreeMenu whether gluten-free menu is required
         * @return this builder instance
         */
        public Builder glutenFreeMenu(Boolean glutenFreeMenu) {
            this.glutenFreeMenu = glutenFreeMenu;
            return this;
        }

        /**
         * Function to set the open day filter.
         *
         * @param openDay the day to check if open
         * @return this builder instance
         */
        public Builder openDay(Day openDay) {
            this.openDay = openDay;
            return this;
        }

        /**
         * Function to set reference coordinates and radius for distance search.
         *
         * @param latRef the reference latitude
         * @param lonRef the reference longitude
         * @param radiusKm the radius in km
         * @return this builder instance
         */
        public Builder distance(Double latRef, Double lonRef, Double radiusKm) {
            this.latRef = latRef;
            this.lonRef = lonRef;
            this.radiusKm = radiusKm;
            return this;
        }

        /**
         * Function to set the minimum average rating required.
         *
         * @param minRating the minimum rating
         * @return this builder instance
         */
        public Builder minRating(Double minRating) {
            this.minRating = minRating;
            return this;
        }

        /**
         * Function to build the {@link SearchFilter} instance.
         *
         * @return the SearchFilter instance
         */
        public SearchFilter build() {
            return new SearchFilter(this);
        }
    }
}
