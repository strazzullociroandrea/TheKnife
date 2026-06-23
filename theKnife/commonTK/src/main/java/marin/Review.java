package marin;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Implemets all the functionality for managing restaurant review.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class Review implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Identifier for the review. Always randomly generated. */
    private String reviewId;
    /** Identifier for the user who wrote the review.*/
    private String userId;
    /** Identifier for the location associated with the reviw.*/
    private String locationId;
    /** Star rating of the overall esperience (0-5). */
    private int globalStars;
    /** Star rating for resturant meal price (0-5). */
    private int priceStars;
    /** Star rating for resturant hospitality (0-5). */
    private int hospitalityStars;
    /** Star rating of restaurant service (0-5). */
    private int serviceStars;
    /** Review text content. */
    private String text;
    /** Date and time the review was submitted. */
    private LocalDateTime reviewDate;
    /** Number of likes recived by the review. */
    private int reviewLikes;
    /** Reply to a revew. */
    private ReviewReply reply;

    /** Review empty constructor. */
    public Review(){
        reviewId = UUID.randomUUID().toString();
        userId = "";
        locationId = "";
        globalStars = 0;
        priceStars = 0;
        hospitalityStars = 0;
        serviceStars = 0;
        text = "";
        reviewDate = LocalDateTime.now();
        reviewLikes = 0;
        reply = null;
    }

    /**
     * Review constructor to create a review with all the required valutation but no text.
     *
     * @param userId
     * @param locationId
     * @param globalStars
     * @param priceStars
     * @param hospitalityStars
     * @param serviceStars
     */
    public Review(String userId, String locationId, int globalStars, int priceStars, int hospitalityStars, int serviceStars){
        reviewId = UUID.randomUUID().toString();
        this.userId = userId;
        this.locationId = locationId;
        this.globalStars = globalStars;
        this.priceStars = priceStars;
        this.hospitalityStars = hospitalityStars;
        this.serviceStars = serviceStars;
        text = "";
        reviewDate = LocalDateTime.now();
        reviewLikes = 0;
        reply = null;
    }

    /**
     * Review constructor to create a review with all the required valutation and a text.
     *
     * @param userId
     * @param locationId
     * @param globalStars
     * @param priceStars
     * @param hospitalityStars
     * @param serviceStars
     * @param text
     */
    public Review(String userId, String locationId, int globalStars, int priceStars, int hospitalityStars, int serviceStars, String text){
        reviewId = UUID.randomUUID().toString();
        this.userId = userId;
        this.locationId = locationId;
        this.globalStars = globalStars;
        this.priceStars = priceStars;
        this.hospitalityStars = hospitalityStars;
        this.serviceStars = serviceStars;
        this.text = text;
        reviewDate = LocalDateTime.now();
        reviewLikes = 0;
        reply = null;
    }

    /**
     * Validates a global star rating value.
     *
     * @param stars the global star rating to validate
     * @throws IllegalArgumentException if the global star rating is not between 1 and 5
     */
    private void validateGlobalStars(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Global stars must be between 1 and 5.");
        }
    }

    /**
     * Validates all the others star rating value.
     *
     * @param stars the star rating to validate
     * @throws IllegalArgumentException if the rating is not between 0 and 5
     */
    private void validateStars(int stars) {
        if (stars < 0 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 0 and 5.");
        }
    }

    /**
     * Returns the review id
     *
     * @return the review id
     */
    public String getReviewId(){
        return reviewId;
    }

    /**
     * Sets the review id
     *
     * @param reviewId
     */
    public void setReviewId(String reviewId){
        this.reviewId = reviewId;
    }

    /**
     * Returns the user id
     *
     * @return the user id
     */
    public String getUserId(){
        return userId;
    }

    /**
     * Sets the user id
     *
     * @param userId
     */
    public void setUserId(String userId){
        this.userId = userId;
    }

    /**
     * Returns the location id
     *
     * @return the location id
     */
    public String getLocationId(){
        return locationId;
    }

    /**
     * Sets the location id
     *
     * @param locationId
     */
    public void setLocationId(String locationId){
        this.locationId = locationId;
    }

    /**
     * Returns the review global stars
     *
     * @return the review global stars
     */
    public int getGlobalStars(){
        return globalStars;
    }

    /**
     * Sets the review global stars
     *
     * @param globalStars
     */
    public void setGlobalStars(int globalStars){
        validateGlobalStars(globalStars);
        this.globalStars = globalStars;
    }

    /**
     * Returns the review price stars
     *
     * @return the review price stars
     */
    public int getPriceStars(){
        return priceStars;
    }

    /**
     * Sets the review price stars
     *
     * @param priceStars
     */
    public void setPriceStars(int priceStars){
        validateStars(priceStars);
        this.priceStars = priceStars;
    }

    /**
     * Returns the review hospitality stars
     *
     * @return the review hospitality stars
     */
    public int getHospitalityStars(){
        return hospitalityStars;
    }

    /**
     * Sets the review hospitality stars
     *
     * @param hospitalityStars
     */
    public void setHospitalityStars(int hospitalityStars){
        validateStars(hospitalityStars);
        this.hospitalityStars = hospitalityStars;
    }

    /**
     * Returns the review service stars
     *
     * @return the review service stars
     */
    public int getServiceStars(){
        return serviceStars;
    }

    /**
     * Sets the review service stars
     *
     * @param serviceStars
     */
    public void setServiceStars(int serviceStars){
        validateStars(serviceStars);
        this.serviceStars = serviceStars;
    }

    /**
     * Returns the review text
     *
     * @return the review text
     */
    public String getText(){
        return text;
    }

    /**
     * Sets the review text
     *
     * @param text
     */
    public void setText(String text){
        this.text = text;
    }

    /**
     * Returns the review date
     *
     * @return the review date
     */
    public LocalDateTime getReviewDate(){
        return reviewDate;
    }

    /**
     * Sets the review date
     *
     * @param reviewDate
     */
    public void setReviewDate(LocalDateTime reviewDate){
        this.reviewDate = reviewDate;
    }

    /**
     * Returns the review likes
     *
     * @return the review likes
     */
    public int getReviewLikes(){
        return reviewLikes;
    }

    /**
     * Sets the review likes
     *
     * @param reviewLikes
     */
    public void setReviewLikes(int reviewLikes){
        this.reviewLikes = reviewLikes;
    }

    /**
     * Returns the review reply
     *
     * @return the review reply
     */
    public ReviewReply getReply(){
        return reply;
    }

    /**
     * Sets the review reply
     *
     * @param reply
     */
    public void setReply(ReviewReply reply){
        this.reply = reply;
    }

    /**
     * Shows review details.
     *
     * @return review details
     */
    @Override
    public String toString(){
        return "Review [reviewId: " + reviewId + "userId: " + userId + "locationId: " + locationId + "\nglobalStars: " + globalStars + "\tpriceStars: " + priceStars + "\thospitalityStars: " + hospitalityStars + "\tserviceStars: " + serviceStars + "\ntext: " + text + "\nreviewDate: " + reviewDate + "\treviewLikes: " + reviewLikes + "\nreply: " + reply + "]";
    }

    /**
     * Adds a reply to a review.
     *
     * @param reply that has to be added
     * @throws IllegalStateException if the review has already a reply
     */
    public void addReply(ReviewReply reply){
        if(this.reply != null)
            throw new IllegalStateException("This review has already a reply.");
        this.reply = reply;
    }

    /**
     * Increments the review likes counter
     */
    public void incrementLikes(){
        reviewLikes++;
    }
}