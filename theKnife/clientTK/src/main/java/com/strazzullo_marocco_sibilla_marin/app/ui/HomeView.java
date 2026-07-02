package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.AuthPromptCard;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.CuisineLabels;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.HomeSectionPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.HomeToolbar;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.NextBookingBanner;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import marocco.SearchFilter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Cuisine;
import sibilla.LocationSearchResult;
import strazzullo.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Home screen: {@link HomeToolbar} (search, and, for logged-in customers, round "Prenotazioni"/
 * "Preferiti" buttons), a location-aware greeting, a {@link NextBookingBanner} for logged-in
 * customers, Glovo-style cuisine category chips, and paginated location sections built with
 * {@link HomeSectionPanel} — "I tuoi preferiti", "Consigliati per te", and "Vicini a te" for
 * customers, or a single cuisine-filterable recommended section (gated behind an auth prompt) for
 * guests and managers. Favourite state for every card on this screen is loaded once per visit via
 * {@code FavouriteService} and persisted the same way when toggled.
 *
 * @version 4.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class HomeView extends StackPane {

    private final ModalPane centeredModalPane = new ModalPane();
    private final VBox scrollContent = new VBox(24);
    private final AppShell shell;
    private final HomeToolbar toolbar;
    private Set<String> favouriteIds = new HashSet<>();
    private Cuisine selectedCuisine;

    /**
     * HomeView constructor.
     *
     * @param shell the app shell to navigate through
     */
    public HomeView(AppShell shell) {
        this.shell = shell;
        getStyleClass().add(Styles.BG_SUBTLE);

        boolean loggedIn = shell.isLoggedIn();
        boolean showCustomerButtons = loggedIn && shell.isCustomer();
        toolbar = new HomeToolbar(this::onSearchFromToolbar, shell::showAccountOrLogin,
                loggedIn, showCustomerButtons, shell::showBookings, shell::showFavourites);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add(Styles.BG_SUBTLE);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scrollContent.setPadding(new Insets(24, 32, 32, 32));
        scrollContent.setAlignment(Pos.TOP_LEFT);
        scrollPane.setContent(scrollContent);

        VBox layout = new VBox(0, toolbar, scrollPane);

        centeredModalPane.setAlignment(Pos.CENTER);
        getChildren().addAll(layout, centeredModalPane);

        Label loadingLabel = new Label("Caricamento consigliati in corso...", new FontIcon(Feather.LOADER));
        loadingLabel.getStyleClass().add(Styles.TEXT_MUTED);
        scrollContent.getChildren().add(loadingLabel);

        loadData();
    }

    /**
     * Function to navigate to the search results screen with the toolbar's currently typed query.
     */
    private void onSearchFromToolbar() {
        shell.showSearch(toolbar.getQuery());
    }

    /**
     * Function to fetch every location and, for a logged-in customer, their favourite location ids,
     * in the background, keeping the UI responsive while the RMI calls are in flight.
     */
    private void loadData() {
        Task<List<LocationSearchResult>> loadTask = new Task<>() {
            private Set<String> loadedFavouriteIds = Set.of();

            @Override
            protected List<LocationSearchResult> call() throws Exception {
                List<LocationSearchResult> allLocations = ServiceLocator.getInstance().getCustomerService()
                        .searchLocations(new SearchFilter.Builder().build());
                if (shell.isCustomer()) {
                    loadedFavouriteIds = ServiceLocator.getInstance().getFavouriteService()
                            .listFavouriteLocationIds(shell.getCurrentUserId());
                }
                return allLocations;
            }

            @Override
            protected void succeeded() {
                favouriteIds = new HashSet<>(loadedFavouriteIds);
                Platform.runLater(() -> populateUI(getValue()));
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
     * Function to build and populate the actual home screen content once every location has
     * loaded: the greeting, the next-booking banner (customers only), the cuisine category chips,
     * and the location sections.
     *
     * @param allLocations every location currently in the system
     */
    private void populateUI(List<LocationSearchResult> allLocations) {
        scrollContent.getChildren().clear();
        scrollContent.getChildren().add(buildGreetingBox());

        if (shell.isCustomer()) {
            scrollContent.getChildren().add(new NextBookingBanner(shell.getCurrentUserId(), centeredModalPane, ids -> { }));
        }

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

        selectedCuisine = null;
        ToggleGroup cuisineGroup = new ToggleGroup();
        Map<Cuisine, ToggleButton> cuisineChips = new LinkedHashMap<>();
        for (Cuisine cuisine : Cuisine.values()) {
            ToggleButton chip = buildChip(cuisine, cuisineGroup);
            categoriesRow.getChildren().add(chip);
            cuisineChips.put(cuisine, chip);
        }
        cuisineGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            selectedCuisine = cuisineChips.entrySet().stream()
                    .filter(entry -> entry.getValue() == newVal)
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);
            renderSections(recommendationsSection, allLocations);
        });

        renderSections(recommendationsSection, allLocations);
    }

    /**
     * Function to rebuild the location sections for the currently selected cuisine (or "any" if
     * none is selected): "I tuoi preferiti", "Consigliati per te", and "Vicini a te" for
     * logged-in customers, or a single recommended section for guests/managers.
     *
     * @param recommendationsSection the container to fill with sections
     * @param allLocations every location currently in the system
     */
    private void renderSections(VBox recommendationsSection, List<LocationSearchResult> allLocations) {
        recommendationsSection.getChildren().clear();

        if (shell.isLoggedIn() && shell.isCustomer()) {
            User user = shell.getCurrentUser();

            List<LocationSearchResult> favouritesList = allLocations.stream()
                    .filter(result -> favouriteIds.contains(result.location().getId()))
                    .toList();
            recommendationsSection.getChildren().add(new HomeSectionPanel(shell, "I tuoi preferiti",
                    new ArrayList<>(favouritesList), "Non hai ancora aggiunto alcun ristorante ai tuoi preferiti.",
                    favouriteIds, () -> showAuthError("I preferiti sono disponibili solo per i clienti."),
                    this::toggleFavourite));

            List<LocationSearchResult> recommendedList = new ArrayList<>(allLocations);
            if (selectedCuisine != null) {
                recommendedList.removeIf(res -> !res.restaurantCuisine().equalsIgnoreCase(selectedCuisine.name()));
            } else {
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
            recommendationsSection.getChildren().add(new HomeSectionPanel(shell, "Consigliati per te",
                    recommendedList, "Nessun ristorante consigliato.",
                    favouriteIds, () -> showAuthError("I preferiti sono disponibili solo per i clienti."),
                    this::toggleFavourite));

            List<LocationSearchResult> nearList = new ArrayList<>(allLocations);
            String userCity = user.getDomicile();
            if (userCity != null && !userCity.isBlank()) {
                nearList.removeIf(res -> !res.location().getCity().toLowerCase().contains(userCity.toLowerCase()));
            }
            recommendationsSection.getChildren().add(new HomeSectionPanel(shell, "Vicini a te",
                    nearList, "Nessun ristorante trovato nella tua zona.",
                    favouriteIds, () -> showAuthError("I preferiti sono disponibili solo per i clienti."),
                    this::toggleFavourite));
        } else {
            List<LocationSearchResult> recommendedList = new ArrayList<>(allLocations);
            String sectionTitle = "Consigliati per te";
            if (selectedCuisine != null) {
                recommendedList.removeIf(res -> !res.restaurantCuisine().equalsIgnoreCase(selectedCuisine.name()));
                sectionTitle = "Consigliati per categoria: " + CuisineLabels.of(selectedCuisine);
            } else {
                recommendedList.sort((a, b) -> Double.compare(b.averageRating() == null ? 0.0 : b.averageRating(),
                        a.averageRating() == null ? 0.0 : a.averageRating()));
            }
            recommendationsSection.getChildren().add(new HomeSectionPanel(shell, sectionTitle,
                    recommendedList, "Nessun ristorante consigliato per questa categoria.",
                    favouriteIds, this::showAuthPrompt, (result, nowFavourite) -> showAuthPrompt()));
        }
    }

    /**
     * Function to persist a favourite toggle fired from any section's card, on a background
     * thread, keeping the local {@link #favouriteIds} set in sync so a re-render (e.g. switching
     * cuisine chips) reflects it without a fresh RMI round trip.
     *
     * @param result the location whose favourite state changed
     * @param nowFavourite the heart's new state
     */
    private void toggleFavourite(LocationSearchResult result, boolean nowFavourite) {
        String userId = shell.getCurrentUserId();
        String locationId = result.location().getId();
        if (nowFavourite) {
            favouriteIds.add(locationId);
        } else {
            favouriteIds.remove(locationId);
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (nowFavourite) {
                    ServiceLocator.getInstance().getFavouriteService().addFavourite(userId, locationId);
                } else {
                    ServiceLocator.getInstance().getFavouriteService().removeFavourite(userId, locationId);
                }
                return null;
            }
        };
        Thread thread = new Thread(task, "home-favourite-toggle-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to build the greeting row: "Ciao {name}, cosa mangiamo oggi?" plus, for logged-in
     * users, the domicile line with its "cambia" link.
     *
     * @return the greeting box
     */
    private VBox buildGreetingBox() {
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

            String domicileText = user.getDomicile() != null && !user.getDomicile().isBlank()
                    ? user.getDomicile() : "Imposta indirizzo";
            Label domicileLabel = new Label(domicileText);
            domicileLabel.getStyleClass().add(Styles.TEXT_MUTED);

            Hyperlink cambiaLink = new Hyperlink("cambia");
            cambiaLink.setStyle("-fx-text-fill: -color-accent-fg; -fx-padding: 0;");
            cambiaLink.setOnAction(e -> promptChangeDomicile(user));

            locationRow.getChildren().addAll(pinIcon, domicileLabel, cambiaLink);
            greetingBox.getChildren().addAll(greetingLabel, locationRow);
        } else {
            greetingLabel.setText("Ciao, cosa mangiamo oggi?");
            greetingBox.getChildren().add(greetingLabel);
        }
        return greetingBox;
    }

    /**
     * Function to prompt for a new domicile and, once confirmed, update it and refresh the home
     * screen so the "Vicini a te"/"Consigliati per te" sections re-match against it.
     *
     * @param user the logged-in user whose domicile to change
     */
    private void promptChangeDomicile(User user) {
        TextInputDialog dialog = new TextInputDialog(user.getDomicile());
        dialog.setTitle("Cambia Domicilio");
        dialog.setHeaderText("Modifica il tuo domicilio");
        dialog.setContentText("Domicilio:");
        dialog.showAndWait().ifPresent(newDomicile -> {
            user.setDomicile(newDomicile);
            shell.showHome();
        });
    }

    /**
     * Function to show a modal auth prompt for guests, offering to log in or register.
     */
    private void showAuthPrompt() {
        centeredModalPane.show(new AuthPromptCard(
                "Per aggiungere ai preferiti, accedi o registrati.",
                () -> centeredModalPane.hide(true),
                shell::showLogin,
                shell::showRegistrationView));
    }

    /**
     * Function to show a modal error for a logged-in manager, who can't have favourites.
     *
     * @param message the error message to show
     */
    private void showAuthError(String message) {
        centeredModalPane.show(new AuthPromptCard(message, () -> centeredModalPane.hide(true)));
    }

    /**
     * Function to build one cuisine's quick-filter chip: an icon badge above a text label, with a
     * hover-lift animation.
     *
     * @param cuisine the cuisine this chip filters by
     * @param group the toggle group enforcing single-selection among all chips
     * @return the chip
     */
    private ToggleButton buildChip(Cuisine cuisine, ToggleGroup group) {
        ToggleButton chip = new ToggleButton(CuisineLabels.of(cuisine));
        chip.setToggleGroup(group);
        chip.getStyleClass().add("tk-cuisine-toggle");
        chip.setContentDisplay(ContentDisplay.TOP);

        StackPane circle = new StackPane();
        circle.getStyleClass().add("tk-cuisine-circle");
        circle.setPrefSize(56, 56);
        circle.setMinSize(56, 56);
        circle.setMaxSize(56, 56);

        var stream = getClass().getResourceAsStream("/icons/cuisine/" + cuisine.name() + ".png");
        ImageView view = new ImageView(new Image(stream));
        view.setFitWidth(32);
        view.setFitHeight(32);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        circle.getChildren().add(view);

        chip.setGraphic(circle);
        installHoverLift(chip);
        return chip;
    }

    /**
     * Function to install a small scale-up/scale-down animation on hover, giving the cuisine
     * chips a bit of tactile feedback.
     *
     * @param node the node to animate
     */
    private void installHoverLift(Node node) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(140), node);
        grow.setToX(1.08);
        grow.setToY(1.08);
        grow.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition shrink = new ScaleTransition(Duration.millis(140), node);
        shrink.setToX(1.0);
        shrink.setToY(1.0);
        shrink.setInterpolator(Interpolator.EASE_OUT);

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
