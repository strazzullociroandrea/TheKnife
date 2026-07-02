package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

import java.util.Locale;

/**
 * Resolves the {@link WifiScanner} implementation for the host OS.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public final class WifiScanners {

    private WifiScanners() {
    }

    /**
     * Function to resolve the Wi-Fi scanner for the OS this JVM is running on.
     *
     * @return the matching scanner
     * @throws WifiScanException if the host OS has no supported scanning tool
     */
    public static WifiScanner forCurrentPlatform() throws WifiScanException {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("mac") || osName.contains("darwin")) {
            return new MacWifiScanner();
        }
        if (osName.contains("win")) {
            return new WindowsWifiScanner();
        }
        if (osName.contains("nux") || osName.contains("nix")) {
            return new LinuxWifiScanner();
        }
        throw new WifiScanException("No Wi-Fi scanner available for OS: " + osName, null);
    }
}
