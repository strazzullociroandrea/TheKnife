package com.strazzullo_marocco_sibilla_marin.app.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Class that represent the user role card in register form
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class RoleCard extends VBox {
    /**
     * Card status (selected=true or not selected=false)
     */
    private boolean isSelected;

    /**
     * The radio button
     */
    private final Circle radioButton = new Circle(8, Color.TRANSPARENT);

    /**
     * Constructor to generate a role card view
     *
     * @param title    the role card title
     * @param subtitle the role card subtitle
     * @param iconName the icon (FontIcon library)
     */
    public RoleCard(String title, String subtitle, String iconName) {
        super(10);
        isSelected = false;

        this.setPadding(new Insets(15));
        this.setPrefWidth(300);


        HBox head = new HBox();
        head.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon(iconName);
        icon.setIconSize(20);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        radioButton.setStroke(Color.GRAY);
        radioButton.setStrokeWidth(2);
        head.getChildren().addAll(icon, spacer, radioButton);

        Label tit = new Label(title);
        tit.getStyleClass().add(Styles.TEXT_NORMAL);
        tit.setStyle("-fx-font-weight: bold;");
        Label subtit = new Label(subtitle);
        subtit.getStyleClass().add(Styles.TEXT_SMALL);
        subtit.setStyle("-fx-text-fill: grey;");

        this.getChildren().addAll(head, tit, subtit);

        status();


    }

    /**
     * Private function to check the status (selected or not) and display card with the correct view.
     */
    private void status() {
        if (isSelected) {
            this.setStyle("-fx-border-color: #4CAF50; -fx-background-color: #f1f8e9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 2; -fx-cursor: hand;");
            radioButton.setStroke(Color.valueOf("#4CAF50"));
            radioButton.setFill(Color.valueOf("#4CAF50"));
        } else {
            this.setStyle("-fx-background-color: white; -fx-border-color: grey ; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1; -fx-cursor: hand;");
            radioButton.setStroke(Color.GRAY);
            radioButton.setFill(Color.TRANSPARENT);
        }
    }

    /**
     * Function to change the card status
     *
     * @param isSelected true=selected, false=not selected
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        status();
    }

    /**
     * Function to get the card status
     *
     * @return true=selected, false=not selected
     */
    public boolean getIsSelected() {
        return this.isSelected;
    }
}