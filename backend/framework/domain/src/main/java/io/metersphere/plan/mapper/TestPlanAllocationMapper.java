package io.metersphere.plan.mapper;

import io.metersphere.plan.domain.TestPlanAllocation;
import io.metersphere.plan.domain.TestPlanAllocationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestPlanAllocationMapper {
    long countByExample(TestPlanAllocationExample example);

    int deleteByExample(TestPlanAllocationExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestPlanAllocation record);

    int insertSelective(TestPlanAllocation record);

    List<TestPlanAllocation> selectByExampleWithBLOBs(TestPlanAllocationExample example);

    List<TestPlanAllocation> selectByExample(TestPlanAllocationExample example);

    TestPlanAllocation selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestPlanAllocation record, @Param("example") TestPlanAllocationExample example);

    int updateByExampleWithBLOBs(@Param("record") TestPlanAllocation record, @Param("example") TestPlanAllocationExample example);

    int updateByExample(@Param("record") TestPlanAllocation record, @Param("example") TestPlanAllocationExample example);

    int updateByPrimaryKeySelective(TestPlanAllocation record);

    int updateByPrimaryKeyWithBLOBs(TestPlanAllocation record);

    int updateByPrimaryKey(TestPlanAllocation record);

    int batchInsert(@Param("list") List<TestPlanAllocation> list);

    int batchInsertSelective(@Param("list") List<TestPlanAllocation> list, @Param("selective") TestPlanAllocation.Column ... selective);
}