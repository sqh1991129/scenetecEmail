package com.scenetec.email.weixin;

import com.scenetec.email.service.SyncLdapToWeixinService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SyncLdapToWeixinServiceTest {

    @Autowired
    private SyncLdapToWeixinService syncLdapToWeixinService;
    @Test
    public void testSyncDepartment() {
        syncLdapToWeixinService.syncDepartment();
    }

}
