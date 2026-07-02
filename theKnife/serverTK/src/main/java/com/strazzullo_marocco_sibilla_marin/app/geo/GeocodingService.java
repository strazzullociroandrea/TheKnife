package com.strazzullo_marocco_sibilla_marin.app.geo;

/**
 * Abstraction over an address geocoding backend, used to resolve a free-text
 * address typed by a customer into coordinates usable for distance search.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface GeocodingService {

    /**
     * Function to resolve an address into a geographic coordinate pair.
     *
     * @param address the free-text address to resolve
     * @return the resolved coordinates
     * @throws GeocodingException if the address cannot be resolved
     */
    GeoPoint geocode(String address) throws GeocodingException;
}
