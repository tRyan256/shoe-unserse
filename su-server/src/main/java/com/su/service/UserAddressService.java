package com.su.service;

import com.su.dto.AddressBookDTO;
import com.su.entity.AddressBook;

import java.util.List;

/**
 * User Address Service
 */
public interface UserAddressService {

    /**
     * List all addresses for a user
     * @param userId User ID
     * @return List of addresses
     */
    List<AddressBook> listAddresses(Long userId);

    /**
     * Add a new address for a user
     * @param userId User ID
     * @param dto Address data
     */
    void addAddress(Long userId, AddressBookDTO dto);

    /**
     * Update an existing address
     * @param userId User ID
     * @param addressId Address ID
     * @param dto Address data
     */
    void updateAddress(Long userId, Long addressId, AddressBookDTO dto);

    /**
     * Delete an address
     * @param userId User ID
     * @param addressId Address ID
     */
    void deleteAddress(Long userId, Long addressId);

    /**
     * Set an address as default
     * @param userId User ID
     * @param addressId Address ID
     */
    void setDefaultAddress(Long userId, Long addressId);
}
