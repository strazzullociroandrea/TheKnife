package com.strazzullo_marocco_sibilla_marin.app.geo.wifi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Shared process-running helper for the OS-specific {@link WifiScanner} implementations, all of
 * which work by shelling out to a platform scanning tool and parsing its stdout.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class ScanCommand {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private ScanCommand() {
    }

    /**
     * Function to run a scanning command to completion and capture its standard output.
     *
     * @param command the command and its arguments
     * @return the process's standard output
     * @throws WifiScanException if the command could not be started, timed out, or exited
     *         with a non-zero status
     */
    static String run(String... command) throws WifiScanException {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new WifiScanException("Wi-Fi scan command timed out: " + command[0], null);
            }
            if (process.exitValue() != 0) {
                throw new WifiScanException("Wi-Fi scan command failed: " + command[0], null);
            }
            return output;
        } catch (IOException e) {
            throw new WifiScanException("Wi-Fi scan command unavailable: " + command[0], e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WifiScanException("Wi-Fi scan command interrupted: " + command[0], e);
        }
    }
}
