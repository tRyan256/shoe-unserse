package com.su.service;

import com.su.dto.EmployeeDTO;
import com.su.dto.EmployeeLoginDTO;
import com.su.dto.EmployeePageQueryDTO;
import com.su.entity.Employee;
import com.su.result.PageResult;
import com.su.vo.EmployeeLoginVO;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */

    void save(EmployeeDTO employeeDTO);

    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);

    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);

}
