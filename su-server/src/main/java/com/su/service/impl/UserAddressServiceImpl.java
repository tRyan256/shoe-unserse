package com.su.service.impl;

import com.su.dto.AddressBookDTO;
import com.su.entity.AddressBook;
import com.su.exception.ForbiddenException;
import com.su.exception.ServiceException;
import com.su.mapper.AddressBookMapper;
import com.su.service.UserAddressService;
import com.su.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Address Service Implementation
 */
@Service
@Slf4j
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> listAddresses(Long userId) {
        log.info("Listing addresses for user: {}", userId);
        
        AddressBook query = AddressBook.builder()
                .userId(userId)
                .build();
        
        return addressBookMapper.list(query);
    }

    @Override
    public void addAddress(Long userId, AddressBookDTO dto) {
        log.info("Adding address for user: {}, data: {}", userId, dto);

        // Validate required fields
        validateAddressData(dto);

        // Create address entity
        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(dto, addressBook);
        addressBook.setUserId(userId);
        
        // If this is set as default, ensure no other address is default
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            // Set all other addresses to non-default
            AddressBook updateDefault = AddressBook.builder()
                    .userId(userId)
                    .isDefault(0)
                    .build();
            addressBookMapper.updateIsDefaultByUserId(updateDefault);
        } else {
            // If not specified, default to non-default
            addressBook.setIsDefault(0);
        }

        addressBookMapper.insert(addressBook);
        
        log.info("Address added successfully for user: {}", userId);
    }

    @Override
    public void updateAddress(Long userId, Long addressId, AddressBookDTO dto) {
        log.info("Updating address {} for user: {}, data: {}", addressId, userId, dto);

        // Validate required fields
        validateAddressData(dto);

        // Check if address exists and belongs to user
        AddressBook existingAddress = addressBookMapper.getById(addressId);
        if (existingAddress == null) {
            throw new ServiceException("地址不存在");
        }
        
        if (!existingAddress.getUserId().equals(userId)) {
            throw new ForbiddenException("无权修改此地址");
        }

        // Update address entity
        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(dto, addressBook);
        addressBook.setId(addressId);
        addressBook.setUserId(userId);
        
        // If this is set as default, ensure no other address is default
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            // Set all other addresses to non-default
            AddressBook updateDefault = AddressBook.builder()
                    .userId(userId)
                    .isDefault(0)
                    .build();
            addressBookMapper.updateIsDefaultByUserId(updateDefault);
        }

        addressBookMapper.update(addressBook);
        
        log.info("Address updated successfully for user: {}", userId);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);

        // Check if address exists and belongs to user
        AddressBook existingAddress = addressBookMapper.getById(addressId);
        if (existingAddress == null) {
            throw new ServiceException("地址不存在");
        }
        
        if (!existingAddress.getUserId().equals(userId)) {
            throw new ForbiddenException("无权删除此地址");
        }

        addressBookMapper.deleteById(addressId);
        
        log.info("Address deleted successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        log.info("Setting default address {} for user: {}", addressId, userId);

        // Check if address exists and belongs to user
        AddressBook existingAddress = addressBookMapper.getById(addressId);
        if (existingAddress == null) {
            throw new ServiceException("地址不存在");
        }
        
        if (!existingAddress.getUserId().equals(userId)) {
            throw new ForbiddenException("无权修改此地址");
        }

        // Set all addresses to non-default first
        AddressBook updateDefault = AddressBook.builder()
                .userId(userId)
                .isDefault(0)
                .build();
        addressBookMapper.updateIsDefaultByUserId(updateDefault);

        // Set the specified address as default
        AddressBook addressBook = new AddressBook();
        addressBook.setId(addressId);
        addressBook.setUserId(userId);
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
        
        log.info("Default address set successfully for user: {}", userId);
    }

    /**
     * Validate address data
     * @param dto Address data
     */
    private void validateAddressData(AddressBookDTO dto) {
        // Validate required fields
        if (dto.getConsignee() == null || dto.getConsignee().trim().isEmpty()) {
            throw new ServiceException("收货人不能为空");
        }

        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            throw new ServiceException("手机号不能为空");
        }

        // Validate phone format
        if (RegexUtils.isPhoneInvalid(dto.getPhone())) {
            throw new ServiceException("手机号格式错误");
        }

        if (dto.getProvinceCode() == null || dto.getProvinceCode().trim().isEmpty()) {
            throw new ServiceException("省份不能为空");
        }

        if (dto.getCityCode() == null || dto.getCityCode().trim().isEmpty()) {
            throw new ServiceException("城市不能为空");
        }

        if (dto.getDistrictCode() == null || dto.getDistrictCode().trim().isEmpty()) {
            throw new ServiceException("区县不能为空");
        }

        if (dto.getDetail() == null || dto.getDetail().trim().isEmpty()) {
            throw new ServiceException("详细地址不能为空");
        }
    }
}
