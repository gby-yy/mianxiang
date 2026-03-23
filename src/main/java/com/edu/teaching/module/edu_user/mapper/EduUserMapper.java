package com.edu.teaching.module.edu_user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.teaching.module.edu_user.entity.EduUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuming
 * @description: 系统用户Mapper接口
 */
@Mapper
public interface EduUserMapper extends BaseMapper<EduUser> {

    /**
     * 分页查询
     */
    List<EduUser> pageQuery(@Param("entity") EduUser entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") EduUser entity);

    /**
     * 列表查询（不分页）
     */
    List<EduUser> listQuery(@Param("entity") EduUser entity);

    /**
     * 根据ID查询
     */
    EduUser getById(@Param("id") Long id);

}
