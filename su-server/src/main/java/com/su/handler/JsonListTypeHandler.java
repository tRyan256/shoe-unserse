package com.su.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON数组类型处理器 - 将数据库中的JSON字符串转换为List<String>
 */
public class JsonListTypeHandler extends BaseTypeHandler<List<String>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (Exception e) {
            ps.setString(i, "[]");
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJsonList(rs.getString(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJsonList(rs.getString(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJsonList(cs.getString(columnIndex));
    }

    private List<String> parseJsonList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 如果解析失败，尝试按逗号分隔（兼容旧数据）
            if (value.contains(",")) {
                String[] arr = value.split(",");
                List<String> list = new ArrayList<>();
                for (String s : arr) {
                    list.add(s.trim());
                }
                return list;
            }
            return new ArrayList<>();
        }
    }
}
