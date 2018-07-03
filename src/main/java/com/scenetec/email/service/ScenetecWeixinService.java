package com.scenetec.email.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scenetec.email.exception.WeixinRequestException;
import com.scenetec.email.po.weixin.WeixinDepartment;
import com.scenetec.email.po.weixin.WeixinMemberAdd;
import com.scenetec.email.po.weixin.WeixinMemberSearchResult;
import com.scenetec.email.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScenetecWeixinService {

    @Value("${workWeixin.user.create.url}")
    private String createUserURLTemp; // 创建用户

    @Value("${workWeixin.user.delete.url}")
    private String deleteUserURLTemp;

    @Value("${workWeixin.user.search.url}")
    private String searchUserURLTemp;

    @Value("${workWeixin.user.update.url}")
    private String updateUserURLTemp;

    @Value("${workWeixin.department.search.url}")
    private String searchDepartmentURLTemp; // 查询部门

    @Value("${workWeixin.department.delete.url}")
    private String deleteDepartmentURLTemp;

    @Value("${workWeixin.department.create.url}")
    private String createDepartmentURLTemp;

    @Value("${workWeixin.addresslist.secret}")
    private String addresslistSecret;

    @Resource
    private AccessTokenManager accessTokenManager;

    public void createUser(WeixinMemberAdd weixinMember) {

        String jsonParam = JSON.toJSONString(weixinMember);

        String resultJson = HttpClientUtil.sendPost(jsonParam, getURL(createUserURLTemp, accessTokenManager.getToken(addresslistSecret).getToken()));

        checkException(resultJson);
    }

    public void deleteUser(String userId) {
        String url = deleteDepartmentURLTemp.replace("USERID", userId);
        String resultJson = HttpClientUtil.sendGet(null, getURL(url, accessTokenManager.getToken(addresslistSecret).getToken()));
        checkException(resultJson);
    }

    public List<WeixinMemberSearchResult> searchUser(Integer rootDepartmentId) {
        String url = searchUserURLTemp.replace("DEPARTMENT_ID", rootDepartmentId+"").replace("FETCH_CHILD", "1");
        String resultJson = HttpClientUtil.sendGet(null, getURL(url, accessTokenManager.getToken(addresslistSecret).getToken()));
        checkException(resultJson);

        if (JSONObject.parseObject(resultJson).get("userlist") != null) {
            List<JSONObject> jsonObjects =  ( List<JSONObject>) JSONObject.parseObject(resultJson).get("userlist");

            List<WeixinMemberSearchResult> weixinMemberAdds = new ArrayList<>();

            for (JSONObject jsonObject: jsonObjects) {
                WeixinMemberSearchResult sr = new WeixinMemberSearchResult();
                sr.setName(jsonObject.getString("name"));
                sr.setUserid(jsonObject.getString("userid"));
                JSONArray jsonArray = (JSONArray) jsonObject.get("department");
                List<Integer> departments = new ArrayList<>();
                for (int i=0; i < jsonArray.size(); i++) {
                    departments.add(jsonArray.getInteger(i));
                }
                sr.setDepartment(departments);
                weixinMemberAdds.add(sr);
            }

            return weixinMemberAdds;
        }
        return null;
    }

    public void updateUser(WeixinMemberSearchResult weixinMemberSearchResult) {

        String url = updateUserURLTemp;
        String resultJson = HttpClientUtil.sendPost(JSON.toJSONString(weixinMemberSearchResult), getURL(url, accessTokenManager.getToken(addresslistSecret).getToken()));
        checkException(resultJson);
    }

    public List<WeixinDepartment> getDepartments(String id) {

        String url = searchDepartmentURLTemp;
        if (StringUtils.isEmpty(id)) {
            url = searchDepartmentURLTemp.replace("&id=ID", "");
        }
        String resultJson = HttpClientUtil.sendGet(null, getURL(url, accessTokenManager.getToken(addresslistSecret).getToken()));
        checkException(resultJson);
        if (JSONObject.parseObject(resultJson).get("department") != null) {
            List<JSONObject> jsonObjects =  ( List<JSONObject>) JSONObject.parseObject(resultJson).get("department");

            List<WeixinDepartment> weixinDepartments = new ArrayList<>();

            for (JSONObject jsonObject: jsonObjects) {
                WeixinDepartment weixinDepartment = new WeixinDepartment();
                weixinDepartment.setName(jsonObject.getString("name"));
                weixinDepartment.setParentid(jsonObject.getInteger("parentid"));
                weixinDepartment.setId(jsonObject.getInteger("id"));
                weixinDepartment.setOrder(jsonObject.getInteger("order"));
                weixinDepartments.add(weixinDepartment);
            }

            return weixinDepartments;
        }
        return null;
    }

    public void createDepartment(WeixinDepartment weixinDepartment) {
        if (weixinDepartment == null) {
            return;
        }
        String url = createDepartmentURLTemp;

        String resultJson = HttpClientUtil.sendPost(JSON.toJSONString(weixinDepartment), getURL(url, accessTokenManager.getToken(addresslistSecret).getToken()));

        checkException(resultJson);
    }

    private void checkException (String resultJson) {
        if (StringUtils.isEmpty(resultJson)) {
            throw new WeixinRequestException("无返回");
        }

        String errcode = JSONObject.parseObject(resultJson).getString("errcode");
        String errmsg = JSONObject.parseObject(resultJson).getString("errmsg");
        if (!errcode.equals("0")) {
            throw new WeixinRequestException(errmsg);
        }

    }

    private String getURL(String urlTemp, String accessToken) {
        return urlTemp.replace("ACCESS_TOKEN", accessToken);
    }
}
