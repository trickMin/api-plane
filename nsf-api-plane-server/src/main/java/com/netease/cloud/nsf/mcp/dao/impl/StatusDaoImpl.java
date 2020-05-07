package com.netease.cloud.nsf.mcp.dao.impl;

import com.netease.cloud.nsf.mcp.dao.StatusDao;
import com.netease.cloud.nsf.mcp.dao.meta.Status;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class StatusDaoImpl implements StatusDao {
    private NamedParameterJdbcTemplate namedTemplate;

    public StatusDaoImpl(NamedParameterJdbcTemplate namedTemplate) {
        this.namedTemplate = namedTemplate;
    }

    @Override
    public String get(String name) {
        try {
            String sql = "select * from status where name=:name";
            SqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("name", name);
            return namedTemplate.queryForObject(sql, ps, (resultSet, i) -> resultSet.getString("value"));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void update(Status status) {
        String sql = "update status set value=:value where name=:name";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(status);
        namedTemplate.update(sql, ps);
    }

    @Override
    public List<Status> list() {
        String sql = "select * from status";
        return namedTemplate.query(sql, (resultSet, i) -> new Status(resultSet.getString("name"), resultSet.getString("value")));
    }
}
