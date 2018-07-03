package com.scenetec.email.service;

import com.scenetec.email.po.weixin.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccessTokenManager {

    @Value("${workWeixin.corpId}")
    private String corpId;

    private Map<String, AccessToken> tokenCache = new HashMap<>();

    public AccessToken getToken(String secret) {
        AccessToken token = tokenCache.get(secret);
        if (token == null) {
            token = new AccessToken(corpId, secret);
            token.refresh();
            tokenCache.put(secret, token);
        }

        return token;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }
}
