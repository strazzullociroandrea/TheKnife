package marin;

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
    /** Reply text content. */
    private Stirng text;
    /** Date and time the reply was submitted. */
    private LocalDateTime replyDate;

    /** Reply empty constructor. */
    public Reply(){
        text = "";
        replyDate = null;
    }

    /** Reply constructor to create a review with text
     *
     * @param text
     */
    public Reply(String text){
        this.text = text;
        replyDate = LocalDateTime.now();
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
        return "Reply [text: " + text + "\nreplyDate: " + replyDate + "]";
    }
}