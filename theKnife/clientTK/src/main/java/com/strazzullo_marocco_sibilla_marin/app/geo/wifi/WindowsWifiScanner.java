package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link WifiScanner} for Windows, using the built-in {@code netsh wlan show networks} tool.
 * Its field labels ("Signal", "Channel", ...) are localized, so rather than matching on label
 * text this parses by shape: a BSSID line, followed within a few lines by a "NN%" signal
 * quality figure and, where present, a bare channel number — both look the same in any
 * Windows locale.
 *
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
final class WindowsWifiScanner implements WifiScanner {

    private static final Pattern BSSID_PATTERN = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");
    private static final Pattern SIGNAL_PATTERN = Pattern.compile("(\\d+)\\s*%");
    private static final Pattern CHANNEL_PATTERN = Pattern.compile("^\\s*(\\d{1,3})\\s*$");
    private static final int LOOKAHEAD_LINES = 5;

    @Override
    public List<WifiNetwork> scan() throws WifiScanException {
        List<String> lines = ScanCommand.run("netsh", "wlan", "show", "networks", "mode=bssid").lines().toList();
        List<WifiNetwork> networks = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Matcher bssidMatcher = BSSID_PATTERN.matcher(lines.get(i));
            if (!bssidMatcher.find()) {
                continue;
            }
            String bssid = bssidMatcher.group().toLowerCase(Locale.ROOT);
            if (MaskedBssid.isMasked(bssid)) {
                continue;
            }
            Integer signalQuality = null;
            Integer channel = null;
            for (int j = i + 1; j < Math.min(lines.size(), i + 1 + LOOKAHEAD_LINES); j++) {
                String line = lines.get(j);
                if (BSSID_PATTERN.matcher(line).find()) {
                    break;
                }
                Matcher signalMatcher = SIGNAL_PATTERN.matcher(line);
                if (signalQuality == null && signalMatcher.find()) {
                    signalQuality = Integer.parseInt(signalMatcher.group(1));
                    continue;
                }
                Matcher channelMatcher = CHANNEL_PATTERN.matcher(line);
                if (channel == null && channelMatcher.matches()) {
                    channel = Integer.parseInt(channelMatcher.group(1));
                }
            }
            networks.add(new WifiNetwork(bssid, toDbm(signalQuality), channel));
        }
        return networks;
    }

    /**
     * Function to convert netsh's 0-100 signal quality percentage into the approximate dBm
     * figure Google's Geolocation API expects, using the same linear mapping Windows itself
     * uses internally (0% = -100 dBm, 100% = -50 dBm).
     *
     * @param quality the signal quality percentage, or null if unknown
     * @return the approximate signal strength in dBm, or null if quality was unknown
     */
    private Integer toDbm(Integer quality) {
        return quality == null ? null : (quality / 2) - 100;
    }
}
