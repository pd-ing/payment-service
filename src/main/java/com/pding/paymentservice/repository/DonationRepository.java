package com.pding.paymentservice.repository;

import com.pding.paymentservice.models.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Collectors;

public interface DonationRepository extends JpaRepository<Donation, String> {
    List<Donation> findByDonorUserId(String donorUserId);

    List<Donation> findByPdUserId(String pdUserId);

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

}
