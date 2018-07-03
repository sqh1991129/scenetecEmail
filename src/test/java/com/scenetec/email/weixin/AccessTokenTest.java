package com.scenetec.email.weixin;

import com.scenetec.email.po.weixin.AccessToken;
import com.scenetec.email.service.AccessTokenManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccessTokenTest {

    static String corpId = "ww5b2fdaa3d4bb41b0";
    static String addressListSecret = "TA1ZEy-E93IEbBTk_pNbmhOwfQ5ORVe0diBkTYCDwMs";

    @Autowired
    AccessTokenManager accessTokenManager;

    @Test
    public void testAccessToken(){
        AccessToken token = new AccessToken(corpId, addressListSecret);
        assertNotNull(token.getToken());
    }

    @Test
    public void testAccessTokenManager() {
        assertNotNull(accessTokenManager.getToken(addressListSecret));
        assertEquals(accessTokenManager.getToken(addressListSecret), accessTokenManager.getToken(addressListSecret));
    }

}
