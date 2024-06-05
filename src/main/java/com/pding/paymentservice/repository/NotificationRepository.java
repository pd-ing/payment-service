package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.VideoPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<VideoPurchase, String> {
    @Query(value = "SELECT email FROM users WHERE id = :userId", nativeQuery = true)
    String findEmailByUserId(@Param("userId") String userId);

    @Query(value = "SELECT title FROM videos WHERE video_id = :videoId", nativeQuery = true)
    String findTitleByVideoId(@Param("videoId") String videoId);


}
