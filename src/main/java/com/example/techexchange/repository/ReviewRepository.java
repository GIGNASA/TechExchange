package com.example.techexchange.repository;

import com.example.techexchange.entity.ExchangeRequest;
import com.example.techexchange.entity.Review;
import com.example.techexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByToUserOrderByCreatedAtDesc(User toUser);

    boolean existsByExchangeRequestAndFromUser(ExchangeRequest exchangeRequest, User fromUser);

    long countByToUser(User toUser);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.toUser = :user")
    double averageRatingFor(@Param("user") User user);
}
