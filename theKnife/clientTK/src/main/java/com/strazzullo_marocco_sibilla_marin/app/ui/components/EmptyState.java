package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A proper empty-state placeholder — a large accent-tinted icon circle, a bold title, and a
 * muted subtitle, optionally followed by a call-to-action button — used anywhere a list can
 * legitimately have nothing in it (no favourites, no bookings, ...). Replaces the plain muted
 * text label these placeholders used to be, which read as an afterthought rather than a
 * deliberate part of the screen.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class EmptyState extends VBox {

    /**
     * EmptyState constructor without a call-to-action.
     *
     * @param icon the icon shown in the accent circle
     * @param title the bold headline
     * @param subtitle the muted supporting line
     */
    public EmptyState(Feather icon, String title, String subtitle) {
        this(icon, title, subtitle, null, null);
    }

    /**
     * EmptyState constructor with a call-to-action button.
     *
     * @param icon the icon shown in the accent circle
     * @param title the bold headline
     * @param subtitle the muted supporting line
     * @param ctaText the call-to-action button's label, or null for none
     * @param onCta callback invoked when the call-to-action is pressed, or null for none
     */
    public EmptyState(Feather icon, String title, String subtitle, String ctaText, Runnable onCta) {
        setAlignment(Pos.CENTER);
        setSpacing(12);
        setPadding(new Insets(48, 24, 48, 24));

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(28);
        StackPane iconCircle = new StackPane(fontIcon);
        iconCircle.getStyleClass().add("tk-empty-state-icon");
        iconCircle.setPrefSize(72, 72);
        iconCircle.setMinSize(72, 72);
        iconCircle.setMaxSize(72, 72);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_3);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add(Styles.TEXT_MUTED);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        subtitleLabel.setMaxWidth(360);

        getChildren().addAll(iconCircle, titleLabel, subtitleLabel);

        if (ctaText != null && onCta != null) {
            Button cta = new Button(ctaText);
            cta.getStyleClass().add(Styles.ACCENT);
            cta.setOnAction(e -> onCta.run());
            getChildren().add(cta);
        }
    }
}
