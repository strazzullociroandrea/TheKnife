package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.SearchToolbar;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.VerticalLocationCard;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.HomeSectionPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.AuthPromptCard;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Cuisine;
import sibilla.LocationSearchResult;
import strazzullo.Client;
import strazzullo.User;
import marocco.Booking;
import marocco.BookingStatus;
import marocco.SearchFilter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Home screen: displays search filter top bar, greeting, next booking card, category chips,
 * and paginated location sections (Favorites, Recommended, Near You) for both logged-in and guest users.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class HomeView extends StackPane {

    private final ModalPane centeredModalPane = new ModalPane();
    private final AppShell shell;

    /**
     * HomeView constructor.
     *
     * @param shell the app shell to navigate through
     */
    public HomeView(AppShell shell) {
        this.shell = shell;
        getStyleClass().add(Styles.BG_SUBTLE);

        VBox layout = new VBox(0);

        /** Search filter top bar */
        final SearchToolbar[] toolbarHolder = new SearchToolbar[1];
        SearchToolbar toolbar = new SearchToolbar("", "", shell::showHome,
                () -> shell.showSearch(toolbarHolder[0].getCity(), toolbarHolder[0].getQuery()), shell::showAccountOrLogin);
        toolbarHolder[0] = toolbar;
        layout.getChildren().add(toolbar);

        /** Scrollable content container */
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add(Styles.BG_SUBTLE);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox scrollContent = new VBox(24);
        scrollContent.setPadding(new Insets(24, 32, 32, 32));
        scrollContent.setAlignment(Pos.TOP_LEFT);
        scrollPane.setContent(scrollContent);

        layout.getChildren().add(scrollPane);

        centeredModalPane.setAlignment(Pos.CENTER);
        getChildren().addAll(layout, centeredModalPane);

        /** Show a loading indicator while data is loading from RMI */
        Label loadingLabel = new Label("Caricamento consigliati in corso...", new FontIcon(Feather.LOADER));
        loadingLabel.getStyleClass().add(Styles.TEXT_MUTED);
        scrollContent.getChildren().add(loadingLabel);

        loadData(shell, scrollContent);
    }

    /**
     * Loads location and booking data asynchronously to keep UI responsive.
     */
    private void loadData(AppShell shell, VBox scrollContent) {
        Task<Void> loadTask = new Task<>() {
            private List<LocationSearchResult> allLocations;
            private Booking nextBooking;
            private LocationSearchResult nextBookingLocation;

            @Override
            protected Void call() throws Exception {
                /** Fetch all locations */
                allLocations = ServiceLocator.getInstance().getCustomerService().searchLocations(new SearchFilter.Builder().build());

                /** Fetch upcoming booking if logged in */
                if (shell.isLoggedIn()) {
                    String userId = shell.getCurrentUserId();
                    List<Booking> bookings = ServiceLocator.getInstance().getBookingService().listBookingsByUser(userId);
                    LocalDate today = LocalDate.now();
                    for (Booking b : bookings) {
                        if (b.getStatus() == BookingStatus.confirmed || b.getStatus() == BookingStatus.waiting) {
                            if (b.getBookingDate().isAfter(today) || b.getBookingDate().isEqual(today)) {
                                if (nextBooking == null || b.getBookingDate().isBefore(nextBooking.getBookingDate())) {
                                    nextBooking = b;
                                }
                            }
                        }
                    }

                    if (nextBooking != null) {
                        for (LocationSearchResult res : allLocations) {
                            if (res.location().getId().equals(nextBooking.getLocationId())) {
                                nextBookingLocation = res;
                                break;
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> populateUI(shell, scrollContent, allLocations, nextBooking, nextBookingLocation));
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                Platform.runLater(() -> {
                    scrollContent.getChildren().clear();
                    Label errorLabel = new Label("Impossibile caricare i dati: " + exception.getMessage());
                    errorLabel.getStyleClass().add(Styles.DANGER);
                    scrollContent.getChildren().add(errorLabel);
                });
            }
        };

        Thread thread = new Thread(loadTask, "home-data-loader");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Builds and populates the actual home screen content.
     */
    private void populateUI(AppShell shell, VBox scrollContent, List<LocationSearchResult> allLocations,
                            Booking nextBooking, LocationSearchResult nextBookingLocation) {
        scrollContent.getChildren().clear();

        /** Welcome greeting & location */
        VBox greetingBox = new VBox(6);
        Label greetingLabel = new Label();
        greetingLabel.getStyleClass().add(Styles.TITLE_1);
        greetingLabel.setStyle("-fx-font-weight: bold;");

        if (shell.isLoggedIn()) {
            User user = shell.getCurrentUser();
            greetingLabel.setText("Ciao " + user.getName() + ", cosa mangiamo oggi?");

            HBox locationRow = new HBox(6);
            locationRow.setAlignment(Pos.CENTER_LEFT);
            FontIcon pinIcon = new FontIcon(Feather.MAP_PIN);
            pinIcon.getStyleClass().add(Styles.TEXT_MUTED);

            String domicileText = (user.getDomicile() != null && !user.getDomicile().isBlank()) ? user.getDomicile() : "Imposta indirizzo";
            Label domicileLabel = new Label(domicileText);
            domicileLabel.getStyleClass().add(Styles.TEXT_MUTED);

            Hyperlink cambiaLink = new Hyperlink("cambia");
            cambiaLink.setStyle("-fx-text-fill: -color-accent-fg; -fx-padding: 0;");
            cambiaLink.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog(user.getDomicile());
                dialog.setTitle("Cambia Domicilio");
                dialog.setHeaderText("Modifica il tuo domicilio");
                dialog.setContentText("Domicilio:");
                dialog.showAndWait().ifPresent(newDomicile -> {
                    user.setDomicile(newDomicile);
                    shell.showHome();
                });
            });

            locationRow.getChildren().addAll(pinIcon, domicileLabel, cambiaLink);
            greetingBox.getChildren().addAll(greetingLabel, locationRow);
        } else {
            greetingLabel.setText("Ciao, cosa mangiamo oggi?");
            greetingBox.getChildren().add(greetingLabel);
        }
        scrollContent.getChildren().add(greetingBox);

        /** Next Booking card (upcoming reservation) */
        if (nextBooking != null && nextBookingLocation != null) {
            VBox bookingCard = new VBox(8);
            bookingCard.getStyleClass().add("tk-booking-ticket");
            bookingCard.setPadding(new Insets(16));

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);
            FontIcon calendarIcon = new FontIcon(Feather.CALENDAR);
            calendarIcon.setStyle("-fx-icon-color: -color-accent-fg;");
            Label cardTitle = new Label("PROSSIMA PRENOTAZIONE");
            cardTitle.setStyle("-fx-text-fill: -color-accent-fg; -fx-font-weight: bold; -fx-font-size: 11px;");
            header.getChildren().addAll(calendarIcon, cardTitle);

            HBox contentRow = new HBox(12);
            contentRow.setAlignment(Pos.CENTER_LEFT);

            String restName = nextBookingLocation.location().getName() != null && !nextBookingLocation.location().getName().isBlank()
                    ? nextBookingLocation.location().getName() : nextBookingLocation.restaurantName();
            Label bookingDetails = new Label(String.format("%s · %s, %s · %d coperti",
                    restName,
                    nextBooking.getBookingDate().toString(),
                    nextBooking.getTimeSlot().toString(),
                    nextBooking.getSeats()));
            bookingDetails.getStyleClass().add(Styles.TEXT_BOLD);
            HBox.setHgrow(bookingDetails, Priority.ALWAYS);

            Button detailsButton = new Button("Dettagli");
            detailsButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
            detailsButton.setOnAction(e -> shell.showLocationDetail(nextBookingLocation));

            contentRow.getChildren().addAll(bookingDetails, detailsButton);
            bookingCard.getChildren().addAll(header, contentRow);
            scrollContent.getChildren().add(bookingCard);
        }

        /** Category chips bar */
        VBox categoriesSection = new VBox(10);
        Label catTitle = new Label("Sfoglia per categoria");
        catTitle.getStyleClass().add(Styles.TITLE_3);
        catTitle.setStyle("-fx-font-weight: bold;");

        HBox categoriesRow = new HBox(8);
        categoriesRow.setPadding(new Insets(4, 0, 8, 0));
        categoriesRow.setAlignment(Pos.CENTER_LEFT);

        categoriesSection.getChildren().addAll(catTitle, categoriesRow);
        scrollContent.getChildren().add(categoriesSection);

        VBox recommendationsSection = new VBox(24);
        scrollContent.getChildren().add(recommendationsSection);

        ToggleGroup cuisineGroup = new ToggleGroup();

        Map<Cuisine, ToggleButton> cuisineChips = new LinkedHashMap<>();
        for (Cuisine cuisine : Cuisine.values()) {
            ToggleButton chip = buildChip(cuisine, label(cuisine), cuisineGroup);
            categoriesRow.getChildren().add(chip);
            cuisineChips.put(cuisine, chip);
        }

        /** Update recommendations list based on selected category chip */
        final Runnable[] updateViewHolder = new Runnable[1];
        Runnable updateView = () -> {
            ToggleButton selected = (ToggleButton) cuisineGroup.getSelectedToggle();
            Cuisine selectedCuisine = null;
            for (Map.Entry<Cuisine, ToggleButton> entry : cuisineChips.entrySet()) {
                if (entry.getValue() == selected) {
                    selectedCuisine = entry.getKey();
                    break;
                }
            }

            recommendationsSection.getChildren().clear();

            if (shell.isLoggedIn()) {
                /** Registered view: Favorites, Recommended for you, Near you */
                User user = shell.getCurrentUser();

                /** Section 1: I tuoi preferiti */
                List<LocationSearchResult> favoritesList = new ArrayList<>();
                if (user instanceof Client) {
                    Client client = (Client) user;
                    for (sibilla.Restaurant favRest : client.getFavoriteRestaurants()) {
                        for (LocationSearchResult res : allLocations) {
                            if (res.restaurantName().equalsIgnoreCase(favRest.getName())) {
                                favoritesList.add(res);
                            }
                        }
                    }
                }
                recommendationsSection.getChildren().add(new HomeSectionPanel(shell, "I tuoi preferiti", favoritesList,
                        "Non hai ancora aggiunto alcun ristorante ai tuoi preferiti.",
                        () -> showAuthError("I preferiti sono disponibili solo per i clienti."),
                        updateViewHolder[0]));

                /** Section 2: Consigliati per te */
                List<LocationSearchResult> recommendedList = new ArrayList<>(allLocations);
                if (selectedCuisine != null) {
                    final Cuisine finalCuisine = selectedCuisine;
                    recommendedList.removeIf(res -> !res.restaurantCuisine().equalsIgnoreCase(finalCuisine.name()));
                } else {
                    /** Match user city, sort by rating */
                    String userCity = user.getDomicile();
                    if (userCity != null && !userCity.isBlank()) {
                        List<LocationSearchResult> inCity = new ArrayList<>(allLocations);
                        inCity.removeIf(res -> !res.location().getCity().toLowerCase().contains(userCity.toLowerCase()));
                        if (!inCity.isEmpty()) {
                            recommendedList = inCity;
                        }
                    }
                    recommendedList.sort((a, b) -> Double.compare(b.averageRating() == null ? 0.0 : b.averageRating(),
                            a.averageRating() == null ? 0.0 : a.averageRating()));
                }
                recommendationsSection.getChildren().add(new HomeSectionPanel(shell, "Consigliati per te", recommendedList,
                        "Nessun ristorante consigliato.",
                        () -> showAuthError("I preferiti sono disponibili solo per i clienti."),
                        updateViewHolder[0]));

                /** Section 3: Vicini a te */
                List<LocationSearchResult> nearList = new ArrayList<>(allLocations);
                String userCity = user.getDomicile();
                if (userCity != null && !userCity.isBlank()) {
                    nearList.removeIf(res -> !res.location().getCity().toLowerCase().contains(userCity.toLowerCase()));
                }
                recommendationsSection.getChildren().add(new HomeSectionPanel(shell, "Vicini a te", nearList,
                        "Nessun ristorante trovato nella tua zona.",
                        () -> showAuthError("I preferiti sono disponibili solo per i clienti."),
                        updateViewHolder[0]));

            } else {
                /** Unregistered view: Recommends based on categories */
                List<LocationSearchResult> recommendedList = new ArrayList<>(allLocations);
                String sectionTitle = "Consigliati per te";
                if (selectedCuisine != null) {
                    final Cuisine finalCuisine = selectedCuisine;
                    recommendedList.removeIf(res -> !res.restaurantCuisine().equalsIgnoreCase(finalCuisine.name()));
                    sectionTitle = "Consigliati per categoria: " + label(selectedCuisine);
                } else {
                    recommendedList.sort((a, b) -> Double.compare(b.averageRating() == null ? 0.0 : b.averageRating(),
                            a.averageRating() == null ? 0.0 : a.averageRating()));
                }
                recommendationsSection.getChildren().add(new HomeSectionPanel(shell, sectionTitle, recommendedList,
                        "Nessun ristorante consigliato per questa categoria.",
                        () -> showAuthPrompt(),
                        updateViewHolder[0]));
            }
        };
        updateViewHolder[0] = updateView;

        cuisineGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateView.run();
        });

        updateView.run();
    }

    /**
     * Shows modal auth prompt for unregistered guests.
     */
    private void showAuthPrompt() {
        centeredModalPane.show(new AuthPromptCard(
                "Per aggiungere ai preferiti, accedi o registrati.",
                () -> centeredModalPane.hide(true),
                shell::showLogin,
                shell::showRegistrationView));
    }

    /**
     * Shows modal error/restriction info for logged in managers.
     */
    private void showAuthError(String message) {
        centeredModalPane.show(new AuthPromptCard(
                message,
                () -> centeredModalPane.hide(true)));
    }

    private String label(Cuisine cuisine) {
        return switch (cuisine) {
            case italian -> "Italiana";
            case chinese -> "Cinese";
            case thai -> "Tailandese";
            case mexican -> "Messicana";
            case indian -> "Indiana";
            case healthy -> "Sana";
            case japanese -> "Giapponese";
        };
    }

    private ToggleButton buildChip(Cuisine cuisine, String text, ToggleGroup group) {
        ToggleButton chip = new ToggleButton(text);
        chip.setToggleGroup(group);
        chip.getStyleClass().add("tk-cuisine-toggle");
        chip.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);

        javafx.scene.layout.StackPane circle = new javafx.scene.layout.StackPane();
        circle.getStyleClass().add("tk-cuisine-circle");
        circle.setPrefSize(56, 56);
        circle.setMinSize(56, 56);
        circle.setMaxSize(56, 56);

        javafx.scene.image.Image image = new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/cuisine/" + cuisine.name() + ".png"));
        javafx.scene.image.ImageView view = new javafx.scene.image.ImageView(image);
        view.setFitWidth(32);
        view.setFitHeight(32);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        circle.getChildren().add(view);

        chip.setGraphic(circle);
        installHoverLift(chip);

        return chip;
    }

    private void installHoverLift(javafx.scene.Node node) {
        javafx.animation.ScaleTransition grow = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(140), node);
        grow.setToX(1.08);
        grow.setToY(1.08);
        grow.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        javafx.animation.ScaleTransition shrink = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(140), node);
        shrink.setToX(1.0);
        shrink.setToY(1.0);
        shrink.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> {
            shrink.stop();
            grow.playFromStart();
        });
        node.setOnMouseExited(e -> {
            grow.stop();
            shrink.playFromStart();
        });
    }
}
