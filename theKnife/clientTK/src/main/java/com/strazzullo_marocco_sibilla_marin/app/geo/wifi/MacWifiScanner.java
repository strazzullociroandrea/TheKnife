package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link WifiScanner} for macOS, using Apple's bundled (if unadvertised) {@code airport} tool,
 * since macOS has no public command-line Wi-Fi scan utility. macOS only reports real BSSIDs to a
 * process that has been granted Location Services permission; otherwise every entry comes back
 * masked as {@code 00:00:00:00:00:00}, which {@link #scan()} detects and rejects rather than
 * silently returning useless access points.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class MacWifiScanner implements WifiScanner {

    private static final String AIRPORT_PATH =
            "/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport";
    private static final Pattern BSSID_PATTERN = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");

    /**
     * Function to list the Wi-Fi access points currently visible to this machine, via the
     * bundled {@code airport} tool. Throws if every observed BSSID came back masked, since that
     * means Location Services permission is missing rather than there being no networks nearby.
     *
     * @return the observed access points; never null, but may be empty
     * @throws WifiScanException if the scan could not be run, its output could not be parsed, or
     *                           every BSSID came back masked
     */
    @Override
    public List<WifiNetwork> scan() throws WifiScanException {
        String output = ScanCommand.run(AIRPORT_PATH, "-s");
        List<WifiNetwork> networks = new ArrayList<>();
        int masked = 0;
        for (String line : output.lines().toList()) {
            Matcher matcher = BSSID_PATTERN.matcher(line);
            if (!matcher.find()) {
                continue;
            }
            String bssid = matcher.group().toLowerCase(Locale.ROOT);
            if (MaskedBssid.isMasked(bssid)) {
                masked++;
                continue;
            }
            String[] rest = line.substring(matcher.end()).trim().split("\\s+");
            Integer rssi = rest.length > 0 ? parseIntOrNull(rest[0]) : null;
            Integer channel = rest.length > 1 ? parseChannel(rest[1]) : null;
            networks.add(new WifiNetwork(bssid, rssi, channel));
        }
        if (networks.isEmpty() && masked > 0) {
            throw new WifiScanException(
                    "Wi-Fi BSSIDs are masked; grant Location Services permission to this app in "
                            + "System Settings > Privacy & Security > Location Services", null);
        }
        return networks;
    }

    /**
     * Function to parse an integer, swallowing any format error.
     *
     * @param value the string to parse
     * @return the parsed integer, or null if it wasn't a valid integer
     */
    private Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Function to parse {@code airport}'s channel column, which sometimes appends a comma and a
     * band suffix (e.g. {@code "36,1"}) that must be stripped before parsing.
     *
     * @param value the channel column's raw text
     * @return the parsed channel number, or null if it wasn't a valid integer
     */
    private Integer parseChannel(String value) {
        int comma = value.indexOf(',');
        return parseIntOrNull(comma >= 0 ? value.substring(0, comma) : value);
    }
}
