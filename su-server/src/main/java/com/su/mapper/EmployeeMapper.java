package com.su.mapper;

import com.github.pagehelper.Page;
import com.su.annotation.AutoFill;
import com.su.dto.EmployeePageQueryDTO;
import com.su.entity.Employee;
import com.su.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */
    @AutoFill(OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询员工数据
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    Employee getById(Long id);
}
