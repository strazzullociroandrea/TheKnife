package com.strazzullo_marocco_sibilla_marin.app.ui;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.strazzullo_marocco_sibilla_marin.app.geo.AddressGeocoder;
import com.strazzullo_marocco_sibilla_marin.app.rmi.ServiceLocator;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.CategoryRow;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.CuisineFilterRow;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.FavouritesPreviewSection;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.LocationPromptPanel;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.NextBookingBanner;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.Pager;
import com.strazzullo_marocco_sibilla_marin.app.ui.components.RestaurantTileCard;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import marocco.SearchFilter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import sibilla.Cuisine;
import sibilla.LocationSearchResult;
import strazzullo.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The home screen's body for logged-in customers: a location-aware greeting with a "cambia"
 * auto-locate link, the cuisine quick-filter row, a {@link NextBookingBanner}, a {@link
 * FavouritesPreviewSection}, and a paginated "Consigliati per te" grid that boosts locations the
 * customer has already booked from (reported by the banner). If "Consigliati per te" comes back
 * empty, it falls back to the same {@link CategoryRow} browsing rows {@link HomeGuestBody} shows,
 * so the section is never blank.
 * The banner and favourites preview are fully self-contained (they run their own background
 * fetches); this class only owns the recommended-grid's own favourite-id lookup and load, since
 * those cards need it and nothing else on the screen does.
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class HomeCustomerBody extends VBox {

    private static final int RECOMMENDED_PAGE_SIZE = 8;
    private static final int CATEGORY_ROW_SIZE = 6;
    private static final double RECOMMENDATION_RADIUS_KM = 20.0;

    private final AppShell shell;
    private final Label locationLabel = new Label();
    private final TilePane recommendedGrid = new TilePane();
    private final Pager recommendedPager = new Pager(this::goToPrevRecommendedPage, this::goToNextRecommendedPage);
    private final VBox recommendedEmptyBox = new VBox(20);

    private String resolvedAddress;
    private int recommendedPage = 0;
    private boolean fallbackCategoriesBuilt = false;
    private Set<String> favouriteIds = new HashSet<>();
    private Set<String> bookedLocationIds = new HashSet<>();

    /**
     * HomeCustomerBody constructor. Builds the dashboard layout; the banner, favourites preview,
     * and recommended grid each kick off their own background load.
     *
     * @param shell the app shell, used for navigation and to read the current user
     * @param modalPane the host screen's modal pane, used for the "cambia" location prompt and the
     *                  banner's booking-details dialog
     */
    public HomeCustomerBody(AppShell shell, ModalPane modalPane) {
        this.shell = shell;
        String userId = shell.getCurrentUserId();

        User user = shell.getCurrentUser();
        resolvedAddress = user.getDomicile();

        VBox greetingBox = buildGreetingBox(user, modalPane);

        CuisineFilterRow cuisineRow = new CuisineFilterRow(
                cuisine -> shell.showSearch(resolvedAddress, "", cuisine),
                () -> shell.showSearch(resolvedAddress, ""));

        NextBookingBanner banner = new NextBookingBanner(userId, modalPane, ids -> bookedLocationIds = ids);
        FavouritesPreviewSection favouritesPreview = new FavouritesPreviewSection(
                userId, shell::showLocationDetail, shell::showFavourites);
        VBox recommendedSection = buildRecommendedSection();

        setSpacing(20);
        getChildren().addAll(greetingBox, banner, cuisineRow, favouritesPreview, recommendedSection);
        setPadding(new Insets(8, 32, 32, 32));

        loadFavouriteIds();
    }

    /**
     * Function to build the greeting row: "Ciao {name}, cosa mangiamo oggi?" plus the location
     * line with its "cambia" auto-locate link.
     *
     * @param user the logged-in customer
     * @param modalPane the host screen's modal pane, used for the "cambia" location prompt
     * @return the greeting box
     */
    private VBox buildGreetingBox(User user, ModalPane modalPane) {
        Label greeting = new Label("Ciao " + user.getName() + ", cosa mangiamo oggi?");
        greeting.getStyleClass().add(Styles.TITLE_1);

        locationLabel.getStyleClass().add(Styles.TEXT_MUTED);
        updateLocationLabelText();

        Label changeLink = new Label("cambia");
        changeLink.getStyleClass().add("tk-link");
        changeLink.setCursor(Cursor.HAND);
        changeLink.setOnMouseClicked(e -> openChangeLocation(modalPane));

        HBox locationRow = new HBox(6, new FontIcon(Feather.MAP_PIN), locationLabel, changeLink);
        locationRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(6, greeting, locationRow);
    }

    /**
     * Function to build the "Consigliati per te" section: a title, the paginated grid and pager,
     * and the (initially hidden) empty-state fallback box.
     *
     * @return the section
     */
    private VBox buildRecommendedSection() {
        Label recommendedTitle = new Label("Consigliati per te", new FontIcon(Feather.EYE));
        recommendedTitle.getStyleClass().add(Styles.TITLE_3);
        recommendedGrid.setHgap(20);
        recommendedGrid.setVgap(20);

        Label recommendedEmptyLabel = new Label(
                "Non abbiamo ancora consigli personalizzati per te. Dai un'occhiata a queste categorie:");
        recommendedEmptyLabel.getStyleClass().add(Styles.TEXT_MUTED);
        recommendedEmptyLabel.setWrapText(true);
        recommendedEmptyBox.getChildren().setAll(recommendedEmptyLabel);
        recommendedEmptyBox.setVisible(false);
        recommendedEmptyBox.setManaged(false);

        return new VBox(16, recommendedTitle, recommendedGrid, recommendedPager, recommendedEmptyBox);
    }

    /**
     * Function to refresh the "cambia" location row's text to the currently resolved address, or
     * a placeholder if none is set.
     */
    private void updateLocationLabelText() {
        locationLabel.setText(resolvedAddress == null || resolvedAddress.isBlank()
                ? "Posizione non impostata" : resolvedAddress);
    }

    /**
     * Function to open the "your position" prompt as an in-app centered modal card, resolving
     * this dashboard's location context (used for the "Consigliati per te" nearby search) either
     * automatically (preferring a Wi-Fi scan, falling back to the user's IP) or by dismissing.
     * Never persisted server-side.
     *
     * @param modalPane the host screen's modal pane to show the prompt in
     */
    private void openChangeLocation(ModalPane modalPane) {
        LocationPromptPanel panel = new LocationPromptPanel(new AddressGeocoder(), address -> {
            resolvedAddress = address;
            updateLocationLabelText();
            modalPane.hide(true);
            recommendedPage = 0;
            loadRecommendedPage();
        }, () -> modalPane.hide(true));
        modalPane.show(panel);
    }

    /**
     * Function to load the current customer's favourite location ids in the background, then
     * kick off the recommended-page load, so every recommended card already knows the correct
     * favourite/heart state when it first renders.
     */
    private void loadFavouriteIds() {
        String userId = shell.getCurrentUserId();
        Task<Set<String>> task = new Task<>() {
            @Override
            protected Set<String> call() throws Exception {
                return ServiceLocator.getInstance().getFavouriteService().listFavouriteLocationIds(userId);
            }
        };
        task.setOnSucceeded(e -> {
            favouriteIds = new HashSet<>(task.getValue());
            loadRecommendedPage();
        });
        Thread thread = new Thread(task, "home-recommended-favourite-ids-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to persist a favourite toggle fired from a recommended (or fallback category) tile
     * card, updating the local id cache immediately.
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
        Thread thread = new Thread(task, "home-recommended-favourite-toggle-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to go back one recommended page, if not already on the first, and reload.
     */
    private void goToPrevRecommendedPage() {
        if (recommendedPage > 0) {
            recommendedPage--;
            loadRecommendedPage();
        }
    }

    /**
     * Function to advance to the next recommended page and reload.
     */
    private void goToNextRecommendedPage() {
        recommendedPage++;
        loadRecommendedPage();
    }

    /**
     * Function to fetch a page of nearby (or, absent a resolved location, top-rated) locations in
     * the background, boost the ones the user has already booked from to the front of the page,
     * and render them.
     */
    private void loadRecommendedPage() {
        SearchFilter.Builder builder = new SearchFilter.Builder().page(recommendedPage, RECOMMENDED_PAGE_SIZE + 1);
        if (resolvedAddress != null && !resolvedAddress.isBlank()) {
            builder.distanceFromAddress(resolvedAddress, RECOMMENDATION_RADIUS_KM);
        }
        SearchFilter filter = builder.build();

        Task<List<LocationSearchResult>> task = new Task<>() {
            @Override
            protected List<LocationSearchResult> call() throws Exception {
                return ServiceLocator.getInstance().getCustomerService().searchLocations(filter);
            }
        };
        task.setOnSucceeded(e -> {
            List<LocationSearchResult> raw = task.getValue();
            boolean hasNext = raw.size() > RECOMMENDED_PAGE_SIZE;
            List<LocationSearchResult> page = new ArrayList<>(hasNext ? raw.subList(0, RECOMMENDED_PAGE_SIZE) : raw);
            if (!bookedLocationIds.isEmpty()) {
                page.sort(Comparator.comparing(r -> bookedLocationIds.contains(r.location().getId()) ? 0 : 1));
            }

            recommendedGrid.getChildren().clear();
            for (LocationSearchResult result : page) {
                recommendedGrid.getChildren().add(new RestaurantTileCard(result, true,
                        favouriteIds.contains(result.location().getId()),
                        () -> shell.showLocationDetail(result), nowFavourite -> toggleFavourite(result, nowFavourite)));
            }
            recommendedPager.setState(recommendedPage, recommendedPage > 0, hasNext);

            boolean isEmpty = page.isEmpty() && recommendedPage == 0;
            recommendedGrid.setVisible(!isEmpty);
            recommendedGrid.setManaged(!isEmpty);
            recommendedPager.setVisible(!isEmpty);
            recommendedPager.setManaged(!isEmpty);
            recommendedEmptyBox.setVisible(isEmpty);
            recommendedEmptyBox.setManaged(isEmpty);
            if (isEmpty) {
                ensureFallbackCategoriesBuilt();
            }
        });
        Thread thread = new Thread(task, "home-recommended-worker");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Function to lazily build the same Glovo-style {@link CategoryRow}s the guest home shows, as
     * a fallback appended to {@link #recommendedEmptyBox} the first (and only the first) time
     * "Consigliati per te" comes back with nothing, so a customer with no bookings/location
     * context yet never sees a blank recommendations section.
     */
    private void ensureFallbackCategoriesBuilt() {
        if (fallbackCategoriesBuilt) {
            return;
        }
        fallbackCategoriesBuilt = true;
        for (Cuisine cuisine : Cuisine.values()) {
            recommendedEmptyBox.getChildren().add(new CategoryRow(cuisine, CATEGORY_ROW_SIZE,
                    shell::showLocationDetail, () -> shell.showSearch(resolvedAddress, "", cuisine),
                    shell::isCustomer, () -> favouriteIds, this::toggleFavourite, () -> { }));
        }
    }
}
