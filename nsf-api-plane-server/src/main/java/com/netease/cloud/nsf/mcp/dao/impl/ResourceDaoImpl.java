package com.netease.cloud.nsf.mcp.dao.impl;

import com.netease.cloud.nsf.mcp.dao.ResourceDao;
import com.netease.cloud.nsf.mcp.dao.meta.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/22
 **/
public class ResourceDaoImpl implements ResourceDao {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDaoImpl.class);

    private NamedParameterJdbcTemplate namedTemplate;

    public ResourceDaoImpl(NamedParameterJdbcTemplate namedTemplate) {
        this.namedTemplate = namedTemplate;
    }

    @Override
    public boolean contains(String collection, String name) {
        String sql = "select count(*) from resource where collection=:collection and name=:name";
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("collection", collection)
                .addValue("name", name);
        return namedTemplate.queryForObject(sql, ps, (resultSet, i) -> resultSet.getInt(1) > 0);
    }

    @Override
    public void add(Resource resource) {
        String sql = "insert into resource(collection, name, config) values(:collection, :name, :config)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(resource);
        namedTemplate.update(sql, ps);
    }

    @Override
    public void delete(String collection, String name) {
        String sql = "delete from resource where collection=:collection and name=:name";
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("collection", collection)
                .addValue("name", name);
        namedTemplate.update(sql, ps);
    }

    @Override
    public void update(Resource resource) {
        String sql = "update resource set config=:config where collection=:collection and name=:name";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(resource);
        namedTemplate.update(sql, ps);
    }

    @Override
    public Resource get(String collection, String name) {
        try {
            String sql = "select * from resource where collection=:collection and  name=:name";
            SqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("collection", collection)
                    .addValue("name", name);
            return namedTemplate.queryForObject(sql, ps, new ResourceRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Resource> list() {
        String sql = "select * from resource";
        return namedTemplate.query(sql, new ResourceRowMapper());
    }

    @Override
    public List<Resource> list(String collection) {
        String sql = "select * from resource where collection=:collection";
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("collection", collection);
        return namedTemplate.query(sql, ps, new ResourceRowMapper());
    }

    @Override
    public List<Resource> list(String collection, String namespace) {
        String sql = "select * from resource where collection=:collection and name like :namespace/%";
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("collection", collection);
        return namedTemplate.query(sql, ps, new ResourceRowMapper());
    }


    static final class ResourceRowMapper implements RowMapper<Resource> {

        @Override
        public Resource mapRow(ResultSet resultSet, int i) throws SQLException {
            Resource resource = new Resource();
            resource.setCollection(resultSet.getString("collection"));
            resource.setName(resultSet.getString("name"));
            resource.setConfig(resultSet.getString("config"));
            return resource;
        }
    }
}
