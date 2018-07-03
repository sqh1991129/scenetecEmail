package com.scenetec.email.po.weixin;

import com.alibaba.fastjson.JSONObject;
import com.scenetec.email.exception.AccessTokenException;
import com.scenetec.email.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

public class AccessToken {

    private String token;

    private String errCode;

    private String errMsg;

    private int expiresIn;

    private String corpId;

    private String secret;

    private long lastGetTokenTimestamp; // 上次获取token时间

    private static String URL_ID = "ID";

    private static String URL_SECRET = "SECRET";

    private final static String URL_TEMPLATE = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRET";

    public AccessToken(String corpId, String secret) {
        this.corpId = corpId;
        this.secret = secret;
    }

    /***
     * @return token是否已经失效
     */
    public boolean tokenExpired() {
        return System.currentTimeMillis() - lastGetTokenTimestamp > 7200*1000;
    }

    public AccessToken refresh() {

        String returnJson = HttpClientUtil.sendGet(null, getAccessTokenURL(this.corpId, this.secret));
        if (StringUtils.isEmpty(returnJson)) {
            throw new AccessTokenException("work weixin result is null");
        }
        setAccessToken(parseTokenJSON(returnJson));

        return this;
    }

    public String getToken() {
        if (tokenExpired()) {
            refresh();
        }
        return this.token;
    }

    private void setAccessToken(JSONObject jsonObject) {
        this.token = jsonObject.getString("access_token");
        this.errCode = jsonObject.getString("errcode");
        this.errMsg = jsonObject.getString("errmsg");
        this.expiresIn = jsonObject.getInteger("expires_in");
        this.lastGetTokenTimestamp = System.currentTimeMillis();
    }

    private static String getAccessTokenURL (String corpId, String secret) {
        return URL_TEMPLATE.replace(URL_ID, corpId).replace(URL_SECRET, secret);
    }

    private JSONObject parseTokenJSON (String jsonToken) {
        JSONObject jsonObject = JSONObject.parseObject(jsonToken);

        return jsonObject;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getLastGetTokenTimestamp() {
        return lastGetTokenTimestamp;
    }

    public void setLastGetTokenTimestamp(long lastGetTokenTimestamp) {
        this.lastGetTokenTimestamp = lastGetTokenTimestamp;
    }
}
