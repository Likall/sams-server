package pers.zjc.sams.dao;

import org.apache.ibatis.annotations.Param;
import pers.zjc.sams.po.Teacher;

public interface TeacherMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Teacher record);

    int insertSelective(Teacher record);

    Teacher selectByPrimaryKey(@Param("id") Integer id);

    int updateByPrimaryKeySelective(Teacher record);

    int updateByPrimaryKey(Teacher record);

    Teacher selectMaxIdTeac();
}