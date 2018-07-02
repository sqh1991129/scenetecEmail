package com.scenetec.email.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.scenetec.email.bean.ParamBean;
import com.scenetec.email.util.HttpClientUtil;

@RestController
@RequestMapping(value="/scenetecMail/department")
public class EmailDepartmentController {
	
	@Autowired
	private ParamBean paramBean;
	
	@RequestMapping(value="/createDepartment")
	public String createDepartment(@RequestParam(name="name") String name,@RequestParam(name="parentid")String parentid){
	    //获取token
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("corpid", paramBean.getCorpid());
		parameter.put("corpsecret", paramBean.getCorpsecret());
		String getTokenRes = HttpClientUtil.sendGet(parameter, paramBean.getGetTokenUrl());
		if(!StringUtils.isEmpty(getTokenRes)) {
			JSONObject getTokenResJson = JSONObject.parseObject(getTokenRes);
			String errCode = String.valueOf(getTokenResJson.get("errcode"));
			String errmsg = String.valueOf(getTokenResJson.get("errmsg"));
			if("0".equals(errCode)) {
				String token = String.valueOf(getTokenResJson.get("access_token"));
				//调用创建部门服务
				String createDepartmentUrl = paramBean.getDepartmentCreate()+"?access_token="+token;
				JSONObject param = new JSONObject();
				param.put("name", name);
				param.put("parentid", Long.valueOf(parentid));
				String createDepRes =  HttpClientUtil.sendPost(param.toJSONString(), createDepartmentUrl);
				if(!StringUtils.isEmpty(createDepRes)) {
					/*JSONObject createDepResJson = JSONObject.parseObject(createDepRes);	
					String createDep = String.valueOf(createDepResJson.get("errcode"));
					if("0".equals(createDep)) {
						
					}*/
					return createDepRes;
				}
			}else {
				System.out.println("errorCode:"+errCode+",errorMsg:"+errmsg);
				return getTokenRes;
			}
		}
		return null;
	}

}
