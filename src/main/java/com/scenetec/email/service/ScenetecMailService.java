package com.scenetec.email.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.scenetec.email.bean.ParamBean;
import com.scenetec.email.po.EmailInfo;
import com.scenetec.email.po.Person;
import com.scenetec.email.util.HttpClientUtil;
import com.scenetec.email.util.LDAPAuthentication;

@Component
public class ScenetecMailService {

	@Autowired
	private ParamBean paramBean;
	public List<EmailInfo> getLadpData(){
		List<EmailInfo> info = new ArrayList<EmailInfo>();
		LDAPAuthentication ldap = new LDAPAuthentication();
		try {
			ldap.search();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info;
	}
	//同步机构
	public String scenetecMailDepartment() {
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
				/*param.put("name", name);
				param.put("parentid", Long.valueOf(parentid));*/
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
	//同步成员信息
	public String scenetecMailPerson(EmailInfo emailInfo) {
		//获取token
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
				String createUserUrl = paramBean.getUserCreate()+"?access_token="+token;
				JSONObject param = new JSONObject();
				Person person = emailInfo.getPerson();
				param.put("userid", person.getUserId());
				param.put("name", person.getName());
				param.put("department", person.getDepartment());
				param.put("position", person.getPosition());
				param.put("mobile", person.getMobile());
				param.put("tel", person.getTel());
				param.put("extid", person.getExtid());
				param.put("gender", person.getGender());
				param.put("slaves", person.getSlaves());
				param.put("password", person.getPassword());
				param.put("cpwd_login", person.getCpwdLogin());
				String createUserRes =  HttpClientUtil.sendPost(param.toJSONString(), createUserUrl);
				if(!StringUtils.isEmpty(createUserRes)) {
					return createUserRes;
				}
			}else {
				System.out.println("errorCode:"+errCode+",errorMsg:"+errmsg);
				return getTokenRes;
			}
	}
		return null;}
}
