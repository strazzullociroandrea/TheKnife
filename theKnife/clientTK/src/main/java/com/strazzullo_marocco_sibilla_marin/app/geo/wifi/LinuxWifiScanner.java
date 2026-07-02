package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link WifiScanner} for Linux, using NetworkManager's {@code nmcli} CLI, the scanning tool
 * present by default on most desktop Linux distributions. Terse output is requested so each
 * access point is a single, machine-parsable line.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class LinuxWifiScanner implements WifiScanner {

    /**
     * Function to list the Wi-Fi access points currently visible to this machine, via
     * {@code nmcli}'s terse output mode.
     *
     * @return the observed access points; never null, but may be empty
     * @throws WifiScanException if the scan could not be run or its output could not be parsed
     */
    @Override
    public List<WifiNetwork> scan() throws WifiScanException {
        String output = ScanCommand.run("nmcli", "-t", "-f", "BSSID,SIGNAL,CHAN", "dev", "wifi", "list");
        List<WifiNetwork> networks = new ArrayList<>();
        for (String line : output.lines().toList()) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = splitTerseFields(line);
            if (fields.length < 3 || fields[0].isBlank()) {
                continue;
            }
            String bssid = fields[0].toLowerCase(java.util.Locale.ROOT);
            if (MaskedBssid.isMasked(bssid)) {
                continue;
            }
            networks.add(new WifiNetwork(bssid, toDbm(parseIntOrNull(fields[1])), parseIntOrNull(fields[2])));
        }
        return networks;
    }

    /**
     * Function to split one line of {@code nmcli -t} output into its fields, honouring the
     * backslash-escaped colons nmcli uses both as the field separator and, unescaped, inside
     * a BSSID's own MAC address.
     *
     * @param line one line of terse nmcli output
     * @return the line's fields, with escape sequences resolved
     */
    private String[] splitTerseFields(String line) {
        String[] rawFields = line.split("(?<!\\\\):");
        String[] fields = new String[rawFields.length];
        for (int i = 0; i < rawFields.length; i++) {
            fields[i] = rawFields[i].replace("\\:", ":");
        }
        return fields;
    }

    /**
     * Function to parse a trimmed integer, swallowing any format error.
     *
     * @param value the string to parse
     * @return the parsed integer, or null if it wasn't a valid integer
     */
    private Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Function to convert nmcli's 0-100 signal quality percentage into the approximate dBm
     * figure Google's Geolocation API expects.
     *
     * @param quality the signal quality percentage, or null if unknown
     * @return the approximate signal strength in dBm, or null if quality was unknown
     */
    private Integer toDbm(Integer quality) {
        return quality == null ? null : (quality / 2) - 100;
    }
}
