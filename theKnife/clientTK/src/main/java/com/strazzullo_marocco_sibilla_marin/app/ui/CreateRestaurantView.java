package com.strazzullo_marocco_sibilla_marin.app.ui;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;

import java.io.File;

public class CreateRestaurantView extends VBox {

    public CreateRestaurantView(AppShell shell) {

        HBox navbar = new HBox(20);
        navbar.setPadding(new Insets(10, 20, 10, 20));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setStyle("-fx-background-color: white; -fx-border-color: #000000; -fx-border-width: 0 0 2 0;");

        Label title = new Label("TheKnife");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        title.setOnMouseClicked(e -> shell.showHome());
        Label description = new Label("Area Ristoratori");
        description.setStyle("-fx-border-color: #e67e22; -fx-border-radius: 15px; -fx-padding: 5 10; -fx-text-fill: #e67e22;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button add = new Button("Pubblica");
        add.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-background-radius: 15px; -fx-padding: 5 20; -fx-cursor: hand;");

        navbar.getChildren().addAll(title, description, spacer, add);
        navbar.setMaxWidth(Double.MAX_VALUE);

        VBox contentArea = new VBox(20);
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setPadding(new Insets(20));
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        contentArea.getChildren().addAll(createInfoCard(), locationCard());

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        getChildren().addAll(navbar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private HBox addPhoto() {
        HBox photoContainer = new HBox(15);
        photoContainer.setPadding(new Insets(20));
        photoContainer.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        photoContainer.setMaxWidth(500);
        photoContainer.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Aggiungi foto");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        imageView.setPreserveRatio(true);

        Button select = new Button("Scegli file...");

        select.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleziona un'immagine");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg")
            );

            File file = fileChooser.showOpenDialog(select.getScene().getWindow());
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
            }
        });

        photoContainer.getChildren().addAll(title, select, imageView);
        return photoContainer;
    }

    private VBox createInfoCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMaxWidth(500);

        Label title = new Label("Informazioni principali");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField nameField = new TextField();
        nameField.setPromptText("Nome del ristorante");

        ComboBox<String> categorySelect = new ComboBox<>();
        categorySelect.getItems().addAll("Italiano", "Giapponese", "Messicano", "Salutare", "Cinese", "Indiano", "Tailandese");
        categorySelect.setPromptText("Seleziona categoria");
        categorySelect.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(title, nameField, categorySelect, addPhoto());
        return card;
    }

    private VBox locationCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMaxWidth(500);

        Label title = new Label("Indirizzi");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox locationsContainer = new VBox(10);
        locationsContainer.getChildren().add(createNewLocationRow());
        Button btnAdd = new Button("+ Aggiungi nuova sede");
        btnAdd.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> locationsContainer.getChildren().add(createNewLocationRow()));

        card.getChildren().addAll(title, locationsContainer, btnAdd);
        return card;
    }

    private VBox createNewLocationRow() {
        HBox row1 = new HBox(10);
        TextField name = new TextField();
        name.setPromptText("Nome della sede");
        HBox.setHgrow(name, Priority.ALWAYS);

        TextField address = new TextField();
        address.setPromptText("Indirizzo");
        HBox.setHgrow(address, Priority.ALWAYS);

        row1.getChildren().addAll(name, address);

        HBox row2 = new HBox(10);
        TextField city = new TextField();
        city.setPromptText("Città");
        city.setPrefWidth(200);

        TextField nation = new TextField();
        nation.setPromptText("Nazione");
        nation.setPrefWidth(100);

        row2.getChildren().addAll(city, nation);

        VBox sedeBlock = new VBox(10, row1, row2);
        sedeBlock.setPadding(new Insets(10));
        sedeBlock.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");

        return sedeBlock;
    }
}