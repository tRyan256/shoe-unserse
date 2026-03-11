package com.su.mapper;

import com.su.entity.DrawRecord;
import com.su.vo.DrawRecordVO;
import com.su.vo.DrawWinnerPublicVO;
import com.su.vo.DrawWinnerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DrawRecordMapper {
    void insert(DrawRecord record);

    void insertIgnore(DrawRecord record);

    void insertBatchIgnore(@Param("records") List<DrawRecord> records);

    Long maxId();

    int countByDrawId(Long drawId);

    List<DrawRecord> listByDrawId(Long drawId);

    List<DrawRecord> listPendingByDrawId(Long drawId);

    List<DrawRecord> listByUserId(Long userId);

    List<Long> listUserIdsByDrawId(Long drawId);

    List<DrawRecord> listUserIdRecordsByDrawIdPaged(@Param("drawId") Long drawId, @Param("lastId") Long lastId, @Param("limit") Integer limit);

    DrawRecord getByDrawIdAndUserId(Long drawId, Long userId);

    List<DrawRecord> pickWinners(Long drawId, Integer winnerCount);

    int updateStatusAndOrderNo(DrawRecord record);

    int updateOrderNoAndShoeSize(@Param("id") Long id, @Param("orderNo") String orderNo, @Param("shoeSize") String shoeSize);

    void markLosers(@Param("drawId") Long drawId, @Param("winnerIds") List<Long> winnerIds);

    List<DrawWinnerVO> listWinners(Long drawId);

    List<DrawWinnerPublicVO> listWinnersPublic(Long drawId);

    /**
     * List draw records with joined draw details for user profile display
     * @param userId User ID
     * @return List of DrawRecordVO with complete draw information, sorted by create_time desc
     */
    List<DrawRecordVO> listByUserIdWithDetails(Long userId);

    /**
     * Clear order_no by order number (for draw order cancellation)
     * @param orderNo The order number to clear
     * @return Number of rows affected
     */
    int clearOrderNoByOrderNo(@Param("orderNo") String orderNo);

    /**
     * Get draw record by order number
     * @param orderNo The order number
     * @return DrawRecord with drawId and userId
     */
    DrawRecord getByOrderNo(@Param("orderNo") String orderNo);
}
