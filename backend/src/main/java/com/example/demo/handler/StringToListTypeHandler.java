package com.example.demo.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringToListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setString(i, "");
        } else {
            String tagsString = String.join(",", parameter);
            ps.setString(i, tagsString);
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String tagsString = rs.getString(columnName);
        return convertStringToList(tagsString);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String tagsString = rs.getString(columnIndex);
        return convertStringToList(tagsString);
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String tagsString = cs.getString(columnIndex);
        return convertStringToList(tagsString);
    }

    private List<String> convertStringToList(String tagsString) {
        if (tagsString == null || tagsString.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
