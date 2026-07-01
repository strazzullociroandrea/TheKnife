package com.strazzullo_marocco_sibilla_marin.app.ui;

import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
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
import sibilla.Cuisine;
import sibilla.Location;
import sibilla.Restaurant;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

/**
 * View page where restaurant managers can create a new restaurant entity and define its physical
 * locations.
 * After submitting the data, the view communicates with the {@code RestaurantService} and
 * {@code PhotoService} via RMI to persist the new restaurant entity and its associated image
 * to the system database.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class CreateRestaurantView extends VBox {

    /**
     * Restaurant name input field
     */
    private TextField nameField;

    /**
     * Restaurant category selection dropdown
     */
    private ComboBox<String> categorySelect;

    /**
     * Selected image file for the restaurant's photo
     */
    private File selectedImageFile;

    /**
     * List of locations added for the restaurant
     */
    private final List<Location> locations = new ArrayList<>();

    /**
     * Feedback label, hidden until a restaurant creation attempt completes
     */
    private final Label showMessage;

    /**
     * Create Restaurant View Constructor
     *
     * @param shell the app shell used for navigation and to store the authenticated session
     */
    public CreateRestaurantView(AppShell shell) {

        HBox navbar = new HBox(20);
        navbar.setPadding(new Insets(10, 20, 10, 20));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setStyle("-fx-background-color: white; -fx-border-color: #000000; -fx-border-width: 0 0 2 0;");

        showMessage = new Label();
        showMessage.setMaxWidth(Double.MAX_VALUE);
        showMessage.setVisible(false);

        Label title = new Label("TheKnife");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        title.setOnMouseClicked(e -> shell.showHome());
        Label description = new Label("Area Ristoratori");
        description.setStyle("-fx-border-color: #e67e22; -fx-border-radius: 15px; -fx-padding: 5 10; -fx-text-fill: #e67e22;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button add = new Button("Pubblica");
        add.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-background-radius: 15px; -fx-padding: 5 20; -fx-cursor: hand;");
        add.setOnAction(e -> {
            handlerCreateRestaurant(shell);
            shell.showHome();
        });

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

        getChildren().addAll(showMessage, navbar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    /**
     * Handler private function to add a photo to the restaurant creation form.
     * It creates a file chooser dialog for the user to select an image file, and displays the selected image in an ImageView.
     *
     * @return a HBox component
     */
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

            selectedImageFile = fileChooser.showOpenDialog(select.getScene().getWindow());
            if (selectedImageFile != null) {
                Image image = new Image(selectedImageFile.toURI().toString());
                imageView.setImage(image);
            }
        });

        photoContainer.getChildren().addAll(title, select, imageView);
        return photoContainer;
    }

    /**
     * Creates the information card for the restaurant creation form, which includes fields for the restaurant name and category selection.
     *
     * @return a VBox component containing the information card
     */
    private VBox createInfoCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMaxWidth(500);

        Label title = new Label("Informazioni principali");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        nameField = new TextField();
        nameField.setPromptText("Nome del ristorante");

        categorySelect = new ComboBox<>();
        categorySelect.getItems().addAll("Italiano", "Giapponese", "Messicano", "Salutare", "Cinese", "Indiano", "Tailandese");
        categorySelect.setPromptText("Seleziona categoria");
        categorySelect.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(title, nameField, categorySelect, addPhoto());
        return card;
    }

    /**
     * Creates the location card for the restaurant creation form, which allows users to add multiple physical locations for the restaurant.
     *
     * @return a VBox component containing the location card
     */
    private VBox locationCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMaxWidth(500);

        Label title = new Label("Indirizzi");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox locationsContainer = new VBox(10);
        locationsContainer.getChildren().add(createNewLocationRow(locationsContainer));
        Button btnAdd = new Button("+ Aggiungi nuova sede");
        btnAdd.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> locationsContainer.getChildren().add(createNewLocationRow(locationsContainer)));

        card.getChildren().addAll(title, locationsContainer, btnAdd);
        return card;
    }

    /**
     * Creates a new row for entering a location's details, including name, address, city, and country.
     *
     * @param parent the parent VBox to which the new location row will be added
     * @return a VBox component containing the new location row
     */
    private VBox createNewLocationRow(VBox parent) {
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
        city.setPrefWidth(150);

        TextField nation = new TextField();
        nation.setPromptText("Nazione");
        nation.setPrefWidth(100);

        Button btnRemove = new Button("Rimuovi");
        btnRemove.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-cursor: hand;");

        Button btnAdd = new Button("Aggiungi");
        btnAdd.setStyle("-fx-background-color: #00FF00; -fx-text-fill: white; -fx-cursor: hand;");


        row2.getChildren().addAll(city, nation, btnRemove, btnAdd);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox sedeBlock = new VBox(10, row1, row2);
        sedeBlock.setPadding(new Insets(10));
        sedeBlock.setStyle("-fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        btnRemove.setOnAction(e -> parent.getChildren().remove(sedeBlock));
        btnAdd.setOnAction(e -> {
            //Da aggiungere i componenti mancanti

            Location newLocation = new Location();
            newLocation.setName(name.getText());
            newLocation.setAddress(address.getText());
            newLocation.setCity(city.getText());
            newLocation.setCountry(nation.getText());
            locations.add(newLocation);

            btnAdd.setText("Aggiunto");
            btnAdd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        });

        sedeBlock.setUserData(new TextField[]{name, address, city, nation});

        return sedeBlock;
    }

    /**
     * Converts a string representation of a cuisine category to its corresponding Cuisine enum value.
     *
     * @param category the string representation of the cuisine category
     * @return the corresponding Cuisine enum value, or null if the category is not recognized
     */
    private Cuisine stringToCousine(String category) {
        return switch (category) {
            case "Italiano" -> Cuisine.italian;
            case "Giapponese" -> Cuisine.japanese;
            case "Messicano" -> Cuisine.mexican;
            case "Salutare" -> Cuisine.healthy;
            case "Cinese" -> Cuisine.chinese;
            case "Indiano" -> Cuisine.indian;
            case "Tailandese" -> Cuisine.thai;
            default -> null;
        };
    }

    /**
     * Handler private function to create a new restaurant entity based on the input data from the form.
     *
     * @param shell the app shell used for navigation and to store the authenticated session
     */
    private void handlerCreateRestaurant(AppShell shell) {

        try {
            //Da aggiungere i dati mancanti
            String nameRestaurant = nameField.getText();
            String category = categorySelect.getValue();
            byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());

            if (stringToCousine(category) == null
                    || category.isEmpty() || !selectedImageFile.exists()) {
                showMessage.setText("Attenzione. Controlla i campi inseriti.");
                showMessage.setStyle("-fx-text-fill: red;");
                showMessage.setVisible(true);

            } else if (locations.isEmpty()) {
                showMessage.setText("Attenzione. Inserisci almeno una sede.");
                showMessage.setStyle("-fx-text-fill: red;");
                showMessage.setVisible(true);
            } else {
                Restaurant r = new Restaurant();
                r.setName(nameRestaurant);

                r.setCuisine(stringToCousine(category));
                r.setLocations(locations);


                ServiceLocator.getInstance().getRestaurantService().createRestaurant(shell.getCurrentUserId(), r);

                ServiceLocator.getInstance().getPhotoService().uploadPhoto(
                        shell.getCurrentUserId(),
                        r.getId(),
                        selectedImageFile.getName(),
                        imageBytes
                );

            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            showMessage.setText("Attenzione. Non è stato possibile proseguire con la creazione del ristorante.");
            showMessage.setStyle("-fx-text-fill: red;");
            showMessage.setVisible(true);
        }
    }
}