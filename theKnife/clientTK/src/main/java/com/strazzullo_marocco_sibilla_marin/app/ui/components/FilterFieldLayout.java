package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Layout helpers shared by {@link FilterPanel}'s sections: grouping a section's content under a
 * title, laying two stretched controls side by side, and labeling a single control above itself.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
final class FilterFieldLayout {

    private FilterFieldLayout() {
    }

    /**
     * Function to group one section's content under a title, with consistent spacing and
     * background so the panel reads as a small number of clear sections rather than a flat list
     * of identical fields.
     *
     * @param title the section title
     * @param content the section content
     * @return the assembled card
     */
    static VBox sectionCard(String title, Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TEXT_CAPTION);
        VBox card = new VBox(14, titleLabel, content);
        card.getStyleClass().add("tk-filter-card");
        card.setPadding(new Insets(16, 18, 18, 18));
        return card;
    }

    /**
     * Function to lay two fields side by side in equal-width columns. Only used for controls that
     * are meant to be stretched (spinners, combo boxes) — unlike {@code ToggleSwitch}, which keeps
     * its own label glued to its own switch and looks disconnected when stretched this way.
     *
     * @param left the left column content
     * @param right the right column content
     * @return the assembled row
     */
    static HBox twoColumns(Node left, Node right) {
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        if (left instanceof Control control) {
            control.setMaxWidth(Double.MAX_VALUE);
        }
        if (right instanceof Control control) {
            control.setMaxWidth(Double.MAX_VALUE);
        }
        return new HBox(16, left, right);
    }

    /**
     * Function to lay out a labeled control as a full-width, label-above-control row, so long
     * Italian labels never get truncated regardless of the panel width.
     *
     * @param label the field label
     * @param control the field control
     * @return the assembled row
     */
    static VBox field(String label, Control control) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add(Styles.TEXT_BOLD);
        control.setMaxWidth(Double.MAX_VALUE);
        return new VBox(8, labelNode, control);
    }
}
