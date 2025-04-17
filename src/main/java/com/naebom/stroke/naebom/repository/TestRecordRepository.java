package com.naebom.stroke.naebom.repository;

import com.naebom.stroke.naebom.entity.TestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TestRecordRepository extends JpaRepository<TestRecord, Long> {

    // 모든 검사 기록 조회 (최신순)
    List<TestRecord> findByMemberIdOrderByTestDateDesc(Long memberId);

    // 최근 2개의 검사 기록 조회
    @Query(value = "SELECT * FROM test_record WHERE member_id = :memberId ORDER BY test_date DESC LIMIT 2", nativeQuery = true)
    List<TestRecord> findRecentTestRecords(@Param("memberId") Long memberId);

    //특정 사용자의 총 검사 횟수 반환 (새로운 메서드 추가)
    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.member.id = :memberId")
    int countByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT MAX(tr.testCount) FROM TestRecord tr WHERE tr.member.id = :memberId")
    Optional<Integer> findMaxTestCountByMemberId(@Param("memberId") Long memberId);

    Optional<TestRecord> findByIdAndMember_Id(Long id, Long memberId);
    Optional<TestRecord> findByMember_IdAndTestCount(Long memberId, Integer testCount);

    List<TestRecord> findTop2ByMemberIdOrderByIdDesc(Long memberId);

    @Query("SELECT COUNT(r) FROM TestRecord r WHERE r.member.id = :memberId AND r.testDate >= :startDate")
    long countByMemberIdInLast30Days(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate);

}
