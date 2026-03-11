package com.cosmetics.common.entity;

import com.cosmetics.common.entity.product.Product;
import com.cosmetics.common.entity.product.ProductVariant;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Review extends IdBasedEntity {

    @Column(length = 128, nullable = false)
    private String headline;

    @Column(length = 300, nullable = false)
    private String comment;

    private int rating;

    private int votes;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewVote> reviewVotes;

    @Column(nullable = false, updatable = false)
    private Date reviewTime;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Transient
    private boolean upvotedByCurrentCustomer;

    @Transient
    private boolean downvotedByCurrentCustomer;

    public Review() {
    }

    public Review(Integer id) {
        this.id = id;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<ReviewVote> getReviewVotes() {
        return reviewVotes;
    }

    public void setReviewVotes(List<ReviewVote> reviewVotes) {
        this.reviewVotes = reviewVotes;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public boolean isUpvotedByCurrentCustomer() {
        return upvotedByCurrentCustomer;
    }

    public void setUpvotedByCurrentCustomer(boolean upvotedByCurrentCustomer) {
        this.upvotedByCurrentCustomer = upvotedByCurrentCustomer;
    }

    public boolean isDownvotedByCurrentCustomer() {
        return downvotedByCurrentCustomer;
    }

    public void setDownvotedByCurrentCustomer(boolean downvotedByCurrentCustomer) {
        this.downvotedByCurrentCustomer = downvotedByCurrentCustomer;
    }

    @Override
    public String toString() {
        Product product = productVariant != null ? productVariant.getProduct() : null;
        return "Review [headline=" + headline + ", rating=" + rating + ", reviewTime=" + reviewTime +
                ", product=" + (product != null ? product.getShortName() : "") +
                ", customer=" + (customer != null ? customer.getFullName() : "") + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Review other = (Review) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
