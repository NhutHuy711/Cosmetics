package com.cosmetics.review;

import java.util.Date;

import javax.transaction.Transactional;

import com.cosmetics.productVariant.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cosmetics.common.entity.Customer;
import com.cosmetics.common.entity.Review;
import com.cosmetics.common.entity.order.OrderStatus;
import com.cosmetics.common.exception.ReviewNotFoundException;
import com.cosmetics.common.entity.product.ProductVariant;
import com.cosmetics.order.OrderDetailRepository;
import com.cosmetics.product.ProductRepository;

@Service
@Transactional
public class ReviewService {
    public static final int REVIEWS_PER_PAGE = 5;

    @Autowired
    private ReviewRepository reviewRepo;

    @Autowired
    private OrderDetailRepository orderDetailRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private ProductVariantRepository productVariantRepo;

    public Page<Review> listByCustomerByPage(Customer customer, String keyword, int pageNum,
                                             String sortField, String sortDir) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNum - 1, REVIEWS_PER_PAGE, sort);

        if (keyword != null) {
            return reviewRepo.findByCustomer(customer.getId(), keyword, pageable);
        }

        return reviewRepo.findByCustomer(customer.getId(), pageable);
    }

    public Review getByCustomerAndId(Customer customer, Integer reviewId) throws ReviewNotFoundException {
        Review review = reviewRepo.findByCustomerAndId(customer.getId(), reviewId);
        if (review == null)
            throw new ReviewNotFoundException("Customer doesn not have any reviews with ID " + reviewId);

        return review;
    }

    public Page<Review> list3MostVotedReviewsByProduct(ProductVariant productVariant) {
        Sort sort = Sort.by("votes").descending();
        Pageable pageable = PageRequest.of(0, 3, sort);

        return reviewRepo.findByProduct(productVariant, pageable);
    }

    public Page<Review> listByProduct(ProductVariant productVariant, int pageNum, String sortField, String sortDir) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNum - 1, REVIEWS_PER_PAGE, sort);

        return reviewRepo.findByProduct(productVariant, pageable);
    }

    public boolean didCustomerReviewProduct(Customer customer, Integer productId) {
        Long count = reviewRepo.countByCustomerAndProduct(customer.getId(), productId);
        return count > 0;
    }

    public boolean canCustomerReviewProduct(Customer customer, Integer productId) {
        Long count = orderDetailRepo.countByVariantAndCustomerAndOrderStatus(productId, customer.getId(), OrderStatus.DELIVERED);
        return count > 0;
    }

    public Review save(Review review) {
        review.setReviewTime(new Date());

        return reviewRepo.save(review);
    }
}
