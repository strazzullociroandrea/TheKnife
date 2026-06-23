package marin;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Consents to reply to a generic review
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class ReviewReply {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Identifier for the reply. Always randomly generated. */
    private String replyId;
    /** Identifier for the review associated with the reply.*/
    private String reviewId;
    /** Identifier for the manager who wrote the reply.*/
    private String managerId;
    /** Reply text content. */
    private String text;
    /** Date and time the reply was submitted. */
    private LocalDateTime replyDate;

    /** Reply empty constructor. */
    public ReviewReply(){
        replyId = UUID.randomUUID().toString();
        reviewId = "";
        managerId = "";
        text = "";
        replyDate = null;
    }

    /** Reply constructor to create a review with text
     *
     * @param text
     */
    public ReviewReply(String text){
        replyId = UUID.randomUUID().toString();
        this.reviewId = reviewId;
        this.managerId = managerId;
        this.text = text;
        replyDate = LocalDateTime.now();
    }

    /**
     * Returns the reply id
     *
     * @return the reply id
     */
    public String getReplyId(){
        return replyId;
    }

    /**
     * Sets the reply id
     *
     * @param replyId
     */
    public void setReplyId(String replyId){
        this.replyId = replyId;
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
     * Returns the manager id
     *
     * @return the manager id
     */
    public String getManagerId(){
        return managerId;
    }

    /**
     * Sets the manager id
     *
     * @param managerId
     */
    public void setManagerId(String managerId){
        this.managerId = managerId;
    }

    /**
     * Returns the reply text
     *
     * @return the reply text
     */
    public String getText(){
        return text;
    }

    /**
     * Sets the reply text
     *
     * @param text
     */
    public void setText(String text){
        this.text = text;
    }

    /**
     * Returns the reply date
     *
     * @return the reply date
     */
    public LocalDateTime getReplyDate(){
        return replyDate;
    }

    /**
     * Sets the reply date
     *
     * @param replyDate
     */
    public void setReplyDate(LocalDateTime replyDate){
        this.replyDate = replyDate;
    }

    /**
     * Shows reply details.
     *
     * @return reply details
     */
    @Override
    public String toString(){
        return "Reply [replyId: " + replyId + "\treviewId: " + reviewId + "\tmanagerId: " + managerId + "\ntext: " + text + "\nreplyDate: " + replyDate + "]";
    }
}