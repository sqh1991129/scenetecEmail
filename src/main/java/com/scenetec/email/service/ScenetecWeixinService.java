package com.scenetec.email.service;

import com.alibaba.fastjson.JSON;
import com.scenetec.email.po.weixin.WeixinMemberAdd;
import com.scenetec.email.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Component
public class ScenetecWeixinService {

    @Value("${workWeixin.user.create.url}")
    private String createUserURLTemp;

    @Value("${workWeixin.department.search.url}")
    private String searchDepartmentURLTemp;

    @Value("${workWeixin.department.delete.url}")
    private String deleteDepartmentURLTemp;

    @Value("${workWeixin.addresslist.secret}")
    private String addresslistSecret;

    @Resource
    private AccessTokenManager accessTokenManager;

    public void createUser(WeixinMemberAdd weixinMember) {

        String jsonParam = JSON.toJSONString(weixinMember);

        String resultJson = HttpClientUtil.sendPost(jsonParam, getURL(createUserURLTemp, accessTokenManager.getToken(addresslistSecret).getToken()));

        System.out.printf(resultJson);
    }

    public void deleteUser(String userId) {
        String url = deleteDepartmentURLTemp.replace("USERID", userId);
        HttpClientUtil.sendGet(null, getURL(url, accessTokenManager.getToken(addresslistSecret).getToken()));
    }

    public void getDepartments(String id) {

        String url = searchDepartmentURLTemp;
        if (StringUtils.isEmpty(id)) {
            url = searchDepartmentURLTemp.replace("&id=ID", "");
        }
        String resultJson = HttpClientUtil.sendGet(null, getURL(url, accessTokenManager.getToken(addresslistSecret).getToken()));

    }

    private String getURL(String urlTemp, String accessToken) {
        return urlTemp.replace("ACCESS_TOKEN", accessToken);
    }
}
