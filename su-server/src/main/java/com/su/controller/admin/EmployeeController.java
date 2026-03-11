package com.su.controller.admin;

import com.su.dto.EmployeeDTO;
import com.su.dto.EmployeeLoginDTO;
import com.su.dto.EmployeePageQueryDTO;
import com.su.entity.Employee;
import com.su.properties.JwtProperties;
import com.su.result.PageResult;
import com.su.result.Result;
import com.su.service.EmployeeService;
import com.su.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Tag(name = "员工管理")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody @Parameter(description = "员工登录信息") EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        EmployeeLoginVO employeeLoginVO = employeeService.login(employeeLoginDTO);
        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "员工退出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     */

    @PostMapping
    @Operation(summary = "新增员工")
    public Result<EmployeeDTO> save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工，员工数据：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }


    /**
     * 员工分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.page(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "启用禁用员工账号")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("启用禁用员工账号：{}", id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据id获取员工详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询员工详细信息")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("查询员工详细信息：{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     */
    @PutMapping
    @Operation(summary = "编辑员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

}
