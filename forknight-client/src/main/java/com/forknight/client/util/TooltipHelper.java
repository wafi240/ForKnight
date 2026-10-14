package com.forknight.client.util;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Centralized tooltip factory for ForKn8.
 *
 * All tooltips in the application must be created via this class so that
 * styling, delay, and behaviour are 100 % consistent across every screen.
 *
 * Usage:
 *   TooltipHelper.install(someButton, "Create a new repository");
 *   TooltipHelper.install(iconLabel,  "Open Git palette");
 */
public final class TooltipHelper {

    // ── Design tokens ────────────────────────────────────────────────────────
    private static final String TOOLTIP_STYLE =
            // Background matches the ForKn8 primary surface
            "-fx-background-color: #11131F;"
            // Subtle border matching the design system
            + "-fx-border-color: #2A2E43;"
            + "-fx-border-width: 1;"
            + "-fx-border-radius: 7;"
            + "-fx-background-radius: 7;"
            // Clean, comfortable text
            + "-fx-text-fill: #F1F3FA;"
            + "-fx-font-family: 'Segoe UI', 'Inter', system-ui, SansSerif;"
            + "-fx-font-size: 12px;"
            // Generous but compact padding
            + "-fx-padding: 6 12 6 12;"
            // Elevated shadow matching card depth
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0, 0, 4);";

    // ── Timing (ms) ─────────────────────────────────────────────────────────
    /** Delay before the tooltip appears (400 ms — comfortable, not slow). */
    private static final double SHOW_DELAY_MS   = 400;
    /** How long the tooltip stays visible without moving the mouse. */
    private static final double SHOW_DURATION_MS = 6_000;
    /** How quickly it disappears once the mouse leaves. */
    private static final double HIDE_DELAY_MS   = 100;

    private TooltipHelper() { /* utility class – no instances */ }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Attaches a styled, themed tooltip to {@code node}.
     *
     * @param node    the interactive element to receive the tooltip
     * @param message the action-oriented description that will appear
     */
    public static void install(Node node, String message) {
        if (node == null || message == null || message.isBlank()) return;

        Tooltip tip = new Tooltip(message);
        tip.setStyle(TOOLTIP_STYLE);
        tip.setShowDelay(Duration.millis(SHOW_DELAY_MS));
        tip.setShowDuration(Duration.millis(SHOW_DURATION_MS));
        tip.setHideDelay(Duration.millis(HIDE_DELAY_MS));
        tip.setWrapText(true);
        tip.setMaxWidth(300);

        Tooltip.install(node, tip);
    }
}
