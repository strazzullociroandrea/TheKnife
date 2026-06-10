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
     * @param globalStars
     * @param priceStars
     * @param hospitalityStars
     * @param serviceStars
     */
    public Review(int globalStars, int priceStars, int hospitalityStars, int serviceStars){
        reviewId = UUID.randomUUID().toString();
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
     * @param globalStars
     * @param priceStars
     * @param hospitalityStars
     * @param serviceStars
     * @param text
     */
    public Review(int globalStars, int priceStars, int hospitalityStars, int serviceStars, String text){
        reviewId = UUID.randomUUID().toString();
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
        return "Review [reviewId: " + reviewId + "\tglobalStars: " + globalStars + "\tpriceStars: " + priceStars + "\thospitalityStars: " + hospitalityStars + "\tserviceStars: " + serviceStars + "\ntext: " + text + "\nreviewDate: " + reviewDate + "\treviewLikes: " + reviewLikes + "\nreply: " + reply + "]";
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