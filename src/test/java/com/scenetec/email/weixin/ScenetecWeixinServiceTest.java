package com.scenetec.email.weixin;

import com.scenetec.email.po.weixin.WeixinMemberAdd;
import com.scenetec.email.service.ScenetecWeixinService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ScenetecWeixinServiceTest {

    @Resource
    private ScenetecWeixinService scenetecWeixinService;

    @Test
    public void createUser() {
        List<Integer> departmentList = new ArrayList<>();
        departmentList.add(1340016318);
        WeixinMemberAdd member = new WeixinMemberAdd("zhangsan02", "张三02", "18610023804", "zhangsan_001@scenetec.com", departmentList);
        scenetecWeixinService.createUser(member);
    }

    @Test
    public void searchDepartment() {
        scenetecWeixinService.getDepartments(null);
    }

}
