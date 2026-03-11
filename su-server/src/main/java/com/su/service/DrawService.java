package com.su.service;

import com.su.dto.DrawDTO;
import com.su.dto.DrawPageQueryDTO;
import com.su.dto.DrawWinConfirmDTO;
import com.su.entity.Draw;
import com.su.entity.DrawRecord;
import com.su.result.PageResult;
import com.su.vo.DrawDetailVO;
import com.su.vo.DrawWinOptionsVO;
import com.su.vo.DrawWinnerPublicVO;
import com.su.vo.DrawWinnerVO;

import java.util.List;

public interface DrawService {

    void save(DrawDTO dto);

    PageResult page(DrawPageQueryDTO dto);

    Draw getById(Long id);

    Draw getDrawByIdWithCache(Long id);

    void update(DrawDTO dto);

    void deleteById(Long id);

    List<Draw> listActive();

    void join(Long drawId);

    List<DrawRecord> myRecords();

    DrawRecord myResult(Long drawId);

    DrawDetailVO detail(Long drawId);

    DrawWinOptionsVO winOptions(Long drawId);

    String winConfirm(Long drawId, DrawWinConfirmDTO dto);

    void giveUp(Long drawId);

    void manualDraw(Long drawId);

    void cancel(Long drawId);

    List<DrawWinnerVO> winners(Long drawId);

    List<DrawWinnerPublicVO> winnersPublic(Long drawId);

    void warmupDrawCache(Long drawId);
}