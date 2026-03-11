package com.su.service.impl;

import com.su.dto.admin.AirdropSaveDTO;
import com.su.dto.admin.BundleSaveDTO;
import com.su.dto.admin.DrawSaveDTO;
import com.su.dto.admin.ShoeSaveDTO;
import com.su.entity.Bundle;
import com.su.entity.BundleShoe;
import com.su.entity.Category;
import com.su.entity.Draw;
import com.su.entity.Airdrop;
import com.su.entity.ShoeSpu;
import com.su.entity.ShoeSpuCategory;
import com.su.exception.OrderBusinessException;
import com.su.mapper.AirdropMapper;
import com.su.mapper.BundleMapper;
import com.su.mapper.BundleShoeMapper;
import com.su.mapper.CategoryMapper;
import com.su.mapper.DrawMapper;
import com.su.mapper.DrawRecordMapper;
import com.su.mapper.ShoeSpuCategoryMapper;
import com.su.mapper.ShoeSpuMapper;
import com.su.service.AdminProductService;
import com.su.vo.DrawWinnerVO;
import com.su.vo.ShoeSpuAdminVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员商品管理服务实现类
 * 提供商品（鞋款、组合包、抽签、空投）的管理功能
 * 管理员端直接查询数据库，不使用缓存
 */
@Slf4j
@Service
public class AdminProductServiceImpl implements AdminProductService {

    private final ShoeSpuMapper shoeSpuMapper;
    private final ShoeSpuCategoryMapper shoeSpuCategoryMapper;
    private final CategoryMapper categoryMapper;
    private final BundleMapper bundleMapper;
    private final BundleShoeMapper bundleShoeMapper;
    private final DrawMapper drawMapper;
    private final DrawRecordMapper drawRecordMapper;
    private final AirdropMapper airdropMapper;

    public AdminProductServiceImpl(
            ShoeSpuMapper shoeSpuMapper,
            ShoeSpuCategoryMapper shoeSpuCategoryMapper,
            CategoryMapper categoryMapper,
            BundleMapper bundleMapper,
            BundleShoeMapper bundleShoeMapper,
            DrawMapper drawMapper,
            DrawRecordMapper drawRecordMapper,
            AirdropMapper airdropMapper
    ) {
        this.shoeSpuMapper = shoeSpuMapper;
        this.shoeSpuCategoryMapper = shoeSpuCategoryMapper;
        this.categoryMapper = categoryMapper;
        this.bundleMapper = bundleMapper;
        this.bundleShoeMapper = bundleShoeMapper;
        this.drawMapper = drawMapper;
        this.drawRecordMapper = drawRecordMapper;
        this.airdropMapper = airdropMapper;
    }

    /**
     * 保存鞋款（支持多分类）
     * 
     * @param dto 鞋款保存DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShoeSpu(ShoeSaveDTO dto) {
        log.info("saveShoeSpu: saving shoe spu, name={}, categoryIds={}", dto.getName(), dto.getCategoryIds());

        // 1. 构建鞋款SPU实体
        ShoeSpu shoeSpu = ShoeSpu.builder()
                .name(dto.getName())
                .brand(dto.getBrandId() != null ? dto.getBrandId().toString() : null)
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        // 2. 插入鞋款SPU到数据库
        shoeSpuMapper.insert(shoeSpu);
        log.info("saveShoeSpu: inserted shoe spu, id={}", shoeSpu.getId());

        // 3. 批量插入分类关联
        List<ShoeSpuCategory> categoryList = dto.getCategoryIds().stream()
                .map(categoryId -> ShoeSpuCategory.builder()
                        .spuId(shoeSpu.getId())
                        .categoryId(categoryId)
                        .createTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        
        shoeSpuCategoryMapper.insertBatch(categoryList);
        log.info("saveShoeSpu: inserted {} category associations", categoryList.size());
    }

    /**
     * 更新鞋款（支持多分类）
     * 先删除旧的分类关联，再插入新的分类关联
     * 
     * @param id 鞋款ID
     * @param dto 鞋款保存DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShoeSpu(Long id, ShoeSaveDTO dto) {
        log.info("updateShoeSpu: updating shoe spu, id={}, name={}, categoryIds={}", id, dto.getName(), dto.getCategoryIds());
        ensureSpuEditable(id);

        // 1. 构建鞋款SPU实体
        ShoeSpu shoeSpu = ShoeSpu.builder()
                .id(id)
                .name(dto.getName())
                .brand(dto.getBrandId() != null ? dto.getBrandId().toString() : null)
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        // 2. 更新鞋款SPU
        shoeSpuMapper.update(shoeSpu);
        log.info("updateShoeSpu: updated shoe spu, id={}", id);

        // 3. 删除旧的分类关联
        shoeSpuCategoryMapper.deleteBySpuId(id);
        log.info("updateShoeSpu: deleted old category associations for spuId={}", id);

        // 4. 批量插入新的分类关联
        List<ShoeSpuCategory> categoryList = dto.getCategoryIds().stream()
                .map(categoryId -> ShoeSpuCategory.builder()
                        .spuId(id)
                        .categoryId(categoryId)
                        .createTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        
        shoeSpuCategoryMapper.insertBatch(categoryList);
        log.info("updateShoeSpu: inserted {} new category associations", categoryList.size());
    }

    /**
     * 查询鞋款详情（包含所有分类）
     * 
     * @param id 鞋款ID
     * @return 鞋款详情VO
     */
    @Override
    public ShoeSpuAdminVO getShoeSpu(Long id) {
        log.info("getShoeSpu: querying shoe spu, id={}", id);

        // 1. 查询鞋款SPU基本信息
        ShoeSpu shoeSpu = shoeSpuMapper.getById(id);
        if (shoeSpu == null) {
            log.warn("getShoeSpu: shoe spu not found, id={}", id);
            return null;
        }

        // 2. 查询该鞋款的所有分类ID
        List<Long> categoryIds = shoeSpuCategoryMapper.getCategoryIdsBySpuId(id);
        log.info("getShoeSpu: found {} categories for spuId={}", categoryIds.size(), id);

        // 3. 查询分类详情（名称等）
        List<ShoeSpuAdminVO.CategoryVO> categories = null;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categoryList = categoryMapper.getByIds(categoryIds);
            categories = categoryList.stream()
                    .map(category -> ShoeSpuAdminVO.CategoryVO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        // 4. 构建返回VO
        ShoeSpuAdminVO vo = ShoeSpuAdminVO.builder()
                .id(shoeSpu.getId())
                .name(shoeSpu.getName())
                .brandId(shoeSpu.getBrand() != null ? Long.parseLong(shoeSpu.getBrand()) : null)
                .brandName(shoeSpu.getBrand())
                .categories(categories)
                .description(shoeSpu.getDescription())
                .status(shoeSpu.getStatus())
                .createTime(shoeSpu.getCreateTime())
                .updateTime(shoeSpu.getUpdateTime())
                .build();

        log.info("getShoeSpu: successfully queried shoe spu, id={}, categoryCount={}", id, 
                categories != null ? categories.size() : 0);
        return vo;
    }

    /**
     * 更新组合包
     * 更新组合包信息和包含的商品项
     * 
     * @param id 组合包ID
     * @param dto 组合包保存DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBundle(Long id, BundleSaveDTO dto) {
        log.info("updateBundle: updating bundle, id={}, name={}", id, dto.getName());
        ensureBundleEditable(id);

        // 1. 构建组合包实体
        Bundle bundle = Bundle.builder()
                .id(id)
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .image(dto.getImage())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        // 2. 更新组合包基本信息
        bundleMapper.update(bundle);
        log.info("updateBundle: updated bundle basic info, id={}", id);

        // 3. 删除旧的商品项关联
        bundleShoeMapper.deleteByBundleId(Collections.singletonList(id));
        log.info("updateBundle: deleted old bundle items for bundleId={}", id);

        // 4. 批量插入新的商品项（如果有）
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<BundleShoe> bundleShoes = dto.getItems().stream()
                    .map(item -> BundleShoe.builder()
                            .bundleId(id)
                            .skuId(item.getSkuId())
                            .name(item.getName())
                            .price(item.getPrice())
                            .copies(item.getCopies())
                            .build())
                    .collect(Collectors.toList());
            
            bundleShoeMapper.insertBatch(bundleShoes);
            log.info("updateBundle: inserted {} new bundle items", bundleShoes.size());
        }
    }

    /**
     * 查询组合包列表
     * 从数据库查询所有组合包
     * 
     * @return 组合包列表
     */
    @Override
    public List<Bundle> getBundleList() {
        log.info("getBundleList: querying all bundles");

        // 查询所有组合包（传入null或空对象查询全部）
        List<Bundle> bundles = bundleMapper.list(null);
        
        log.info("getBundleList: found {} bundles", bundles != null ? bundles.size() : 0);
        return bundles;
    }

    /**
     * 切换组合包启用/停用状态
     * 
     * @param id 组合包ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleBundleStatus(Long id) {
        log.info("toggleBundleStatus: toggling bundle status, id={}", id);
        ensureBundleEditable(id);

        // 1. 查询当前组合包状态
        Bundle bundle = bundleMapper.getInfoById(id);
        if (bundle == null) {
            log.warn("toggleBundleStatus: bundle not found, id={}", id);
            throw new RuntimeException("组合包不存在: " + id);
        }

        // 2. 切换状态 (0 -> 1 或 1 -> 0)
        Integer newStatus = (bundle.getStatus() == null || bundle.getStatus() == 0) ? 1 : 0;
        bundleMapper.startOrStop(newStatus, id);
        log.info("toggleBundleStatus: toggled bundle status from {} to {}, id={}", 
                bundle.getStatus(), newStatus, id);
    }

    /**
     * 更新抽签活动
     * 
     * @param id 抽签活动ID
     * @param dto 抽签保存DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDraw(Long id, DrawSaveDTO dto) {
        log.info("updateDraw: updating draw, id={}, title={}", id, dto.getTitle());
        throw new OrderBusinessException("抽签活动创建后不支持编辑，请取消后重新创建");
    }

    /**
     * 查询抽签列表
     * 从数据库查询所有抽签活动
     * 
     * @return 抽签活动列表
     */
    @Override
    public List<Draw> getDrawList() {
        log.info("getDrawList: querying all draws");

        // 查询所有活跃的抽签活动
        List<Draw> draws = drawMapper.listActive();
        
        log.info("getDrawList: found {} draws", draws != null ? draws.size() : 0);
        return draws;
    }

    /**
     * 查询中奖名单
     * 查询指定抽签活动的所有中奖用户信息
     * 
     * @param drawId 抽签活动ID
     * @return 中奖用户列表
     */
    @Override
    public List<DrawWinnerVO> getDrawWinners(Long drawId) {
        log.info("getDrawWinners: querying winners for drawId={}", drawId);

        if (drawId == null) {
            log.warn("getDrawWinners: drawId is null");
            return Collections.emptyList();
        }

        // 查询中奖名单
        List<DrawWinnerVO> winners = drawRecordMapper.listWinners(drawId);
        
        log.info("getDrawWinners: found {} winners for drawId={}", 
                winners != null ? winners.size() : 0, drawId);
        return winners != null ? winners : Collections.emptyList();
    }

    /**
     * 更新空投活动
     * 
     * @param id 空投活动ID
     * @param dto 空投保存DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAirdrop(Long id, AirdropSaveDTO dto) {
        log.info("updateAirdrop: updating airdrop, id={}, title={}, couponId={}", 
                id, dto.getTitle(), dto.getCouponId());
        throw new OrderBusinessException("空投活动创建后不支持编辑，请取消后重新创建");
    }

    /**
     * 查询空投列表
     * 
     * @return 空投活动列表
     */
    @Override
    public List<Airdrop> getAirdropList() {
        log.info("getAirdropList: querying all airdrops");

        // 查询所有正在运行的空投活动
        List<Airdrop> airdrops = airdropMapper.listRunning();
        
        log.info("getAirdropList: found {} airdrops", airdrops != null ? airdrops.size() : 0);
        return airdrops != null ? airdrops : Collections.emptyList();
    }

    /**
     * 取消空投活动
     * 更新空投状态为已取消（status=3）
     * 
     * @param id 空投活动ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAirdrop(Long id) {
        log.info("cancelAirdrop: cancelling airdrop, id={}", id);

        // 1. 查询当前空投活动
        Airdrop airdrop = airdropMapper.getById(id);
        if (airdrop == null) {
            log.warn("cancelAirdrop: airdrop not found, id={}", id);
            throw new RuntimeException("空投活动不存在: " + id);
        }

        // 2. 更新状态为已取消（status=3）
        Airdrop updateAirdrop = Airdrop.builder()
                .id(id)
                .status(3)
                .build();
        airdropMapper.update(updateAirdrop);
        log.info("cancelAirdrop: updated airdrop status to cancelled (3), id={}", id);
    }

    private void ensureSpuEditable(Long spuId) {
        if (spuId == null) {
            return;
        }
        int linked = drawMapper.countWarmupBySpuId(spuId);
        if (linked > 0) {
            throw new OrderBusinessException("该商品关联抽签活动，活动缓存未结束，请在缓存结束后再修改");
        }
    }

    private void ensureBundleEditable(Long bundleId) {
        if (bundleId == null) {
            return;
        }
        int linked = drawMapper.countWarmupByBundleId(bundleId);
        if (linked > 0) {
            throw new OrderBusinessException("该商品关联抽签活动，活动缓存未结束，请在缓存结束后再修改");
        }
    }
}


