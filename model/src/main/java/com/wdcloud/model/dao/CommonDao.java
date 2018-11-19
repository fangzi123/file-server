package com.wdcloud.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 通用dao
 *
 * @param <BeanType> 实体类型
 * @param <IdType> 实体ID类型
 */
public abstract class CommonDao<BeanType, IdType> {
    @Autowired
    protected Mapper<BeanType> mapper;

    public int save(BeanType record) {
        assert record != null;
        return mapper.insertSelective(record);
    }

    public int insert(BeanType record) {
        return mapper.insert(record);
    }

    /**
     * 按id更新分空字段
     *
     * @param record
     * @return
     */
    public int update(BeanType record) {
        assert record != null;
        return mapper.updateByPrimaryKeySelective(record);
    }

    public int updateIncludeNull(BeanType record) {
        assert record != null;
        return mapper.updateByPrimaryKey(record);
    }

    /**
     * 批量更新
     * <pre>
     *    批量更新实体主键名必须是"id"
     * </pre>
     *
     * @param record
     * @param ids    更新记录id列表
     * @return
     */
    public int updateByExample(BeanType record, Collection<IdType> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Example example = getExample();
        Example.Criteria criteria = example.createCriteria().andIn("id", ids);
        return mapper.updateByExampleSelective(record, example);
    }

    /**
     * 按id删除
     *
     * @param id
     * @return
     */
    public int delete(IdType id) {
        assert id != null;
        return mapper.deleteByPrimaryKey(id);
    }

    /**
     * 批量删除
     * <pre>
     *     批量删除实体主键名必须是"id"
     * </pre>
     *
     * @param ids 删除记录id列表
     * @return
     */
    public int deletes(Collection<IdType> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Example example = getExample();
        example.createCriteria()
                .andIn("id", ids);
        return mapper.deleteByExample(example);
    }

    public int delete(Example example) {
        return mapper.deleteByExample(example);
    }

    /**
     * 按condition中非空字段进行删除
     *
     * @param condition 删除条件，非空字段作为相等条件进行匹配
     * @return
     */
    public int deleteByField(BeanType condition) {
        assert condition != null;
        return mapper.delete(condition);
    }

    /**
     * 按id检索
     *
     * @param id
     * @return
     */
    public BeanType get(IdType id) {
        assert id != null;
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * 按ids检索
     *
     * @param ids
     * @return
     */
    public List<BeanType> gets(Collection<IdType> ids) {
        assert ids != null;
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        Example example = getExample();
        example.createCriteria()
                .andIn("id", ids);
        return mapper.selectByExample(example);
    }

    /**
     * 按condition对象中非空字段进行检索，并且只返回第一个结果
     *
     * @param condition 检索条件，非空字段作为相等条件进行匹配
     * @return
     */
    public BeanType findOne(BeanType condition) {
        assert condition != null;
        List<BeanType> list = find(condition);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 按condition对象中非空字段进行检索
     *
     * @param condition 检索条件，非空字段作为相等条件进行匹配
     * @return
     */
    public List<BeanType> find(BeanType condition) {
        assert condition != null;
        return mapper.select(condition);
    }

    public List<BeanType> find(Example example) {
        return mapper.selectByExample(example);
    }

    public BeanType findOne(Example example) {
        List<BeanType> res = find(example);
        return res.isEmpty() ? null : res.get(0);
    }

    public int count(Example example) {
        return mapper.selectCountByExample(example);
    }

    public int count(BeanType condition) {
        return mapper.selectCount(condition);
    }

    public Example getExample() {
        return new Example(getBeanClass());
    }

    abstract protected Class<BeanType> getBeanClass();
}
