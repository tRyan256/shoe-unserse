package com.su.controller.user;

import com.su.context.BaseContext;
import com.su.dto.AddressBookDTO;
import com.su.entity.AddressBook;
import com.su.result.Result;
import com.su.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Address Controller
 * Handles address CRUD operations
 */
@RestController
@RequestMapping("/user/profile/addresses")
@Slf4j
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    /**
     * Get all addresses for current user
     *
     * @return List of addresses
     */
    @GetMapping
    public Result<List<AddressBook>> listAddresses() {
        Long userId = BaseContext.getCurrentId();
        log.info("Getting addresses for user: {}", userId);

        List<AddressBook> addresses = userAddressService.listAddresses(userId);
        return Result.success(addresses);
    }

    /**
     * Create new address
     *
     * @param dto Address data
     * @return Success result
     */
    @PostMapping
    public Result<Void> addAddress(@RequestBody AddressBookDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("Adding address for user: {}, data: {}", userId, dto);

        userAddressService.addAddress(userId, dto);
        return Result.success();
    }

    /**
     * Update address
     *
     * @param id Address ID
     * @param dto Address data
     * @return Success result
     */
    @PutMapping("/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody AddressBookDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("Updating address {} for user: {}, data: {}", id, userId, dto);

        userAddressService.updateAddress(userId, id, dto);
        return Result.success();
    }

    /**
     * Delete address
     *
     * @param id Address ID
     * @return Success result
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("Deleting address {} for user: {}", id, userId);

        userAddressService.deleteAddress(userId, id);
        return Result.success();
    }

    /**
     * Set address as default
     *
     * @param id Address ID
     * @return Success result
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("Setting default address {} for user: {}", id, userId);

        userAddressService.setDefaultAddress(userId, id);
        return Result.success();
    }
}
