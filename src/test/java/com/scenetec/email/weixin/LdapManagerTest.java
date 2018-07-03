package com.scenetec.email.weixin;

import com.scenetec.email.service.LdapManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.NamingException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LdapManagerTest {

    @Autowired
    private LdapManager ldapManager;

    @Test
    public void testSearch() {
        try {
            ldapManager.search();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

}
