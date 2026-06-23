package io.nebula.storage.mysql.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommaSeparatedLongListTypeHandler implements TypeHandler<List<Long>> {

    @Override
    public void setParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, convertListToString(parameter));
    }

    @Override
    public List<Long> getResult(ResultSet rs, String columnName) throws SQLException {
        return convertStringToList(rs.getString(columnName));
    }

    @Override
    public List<Long> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertStringToList(rs.getString(columnIndex));
    }

    @Override
    public List<Long> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertStringToList(cs.getString(columnIndex));
    }

    private String convertListToString(List<Long> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Long> convertStringToList(String value) {
        if (value == null || value.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::valueOf)
            .collect(Collectors.toList());
    }
}
