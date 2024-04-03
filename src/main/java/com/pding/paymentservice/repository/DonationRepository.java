package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public interface DonationRepository extends JpaRepository<Donation, String> {
    List<Donation> findByDonorUserId(String donorUserId);

    @Query(value = "SELECT u.email AS donor_email, d.donated_trees, d.last_update_date " +
            "FROM donation d " +
            "JOIN users u ON d.donor_user_id = u.id " +
            "WHERE d.pd_user_id = ?1 ORDER BY d.last_update_date ASC", countQuery = "SELECT count(d.id) FROM Donation d WHERE d.pd_user_id = ?1", nativeQuery = true)
    Page<Object[]> findByPdUserId(String pdUserId, Pageable pageable);

    @Query("SELECT d.donorUserId, SUM(d.donatedTrees) as totalDonatedTrees " +
            "FROM Donation d " +
            "GROUP BY d.donorUserId " +
            "ORDER BY totalDonatedTrees DESC")
    List<Object[]> findTopDonors(@Param("limit") Long limit);

    default List<Object[]> findTopDonorUserIds(Long limit) {
        return findTopDonors(limit);

//        return topDonors.stream()
//                .map(row -> (String) row[0])
//                .collect(Collectors.toList());
    }


    @Query("SELECT d.donorUserId, SUM(d.donatedTrees) as totalDonatedTrees " +
            "FROM Donation d " +
            "WHERE d.pdUserId = :pdUserId " +
            "GROUP BY d.donorUserId " +
            "ORDER BY totalDonatedTrees DESC " +
            "LIMIT :limit")
    List<Object[]> findTopDonorUserAndDonatedTreesByPdUserID(@Param("pdUserId") String pdUserId, @Param("limit") Long limit);

    @Query(value = "SELECT COALESCE(SUM(d.donatedTrees), 0) FROM Donation d WHERE d.donorUserId = :userId")
    BigDecimal getTotalDonatedTreesByDonorUserId(@Param("userId") String userId);

}
