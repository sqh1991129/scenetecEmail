package com.scenetec.email.bean;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ParamBean {
	
	@Value("${email.corpid}")
	private String corpid;
	@Value("${email.corpsecret}")
	private String corpsecret;
	@Value("${email.gettoken}")
	private String getTokenUrl;
	@Value("${email.department.create}")
	private String departmentCreate;
	@Value("${email.user.create}")
	private String userCreate;
	@Value("${email.department.search}")
	private String departmentSearch;
	@Value("${email.department.list}")
	private String departmentList;
	@Value("${email.department.delete}")
	private String departmentDelete;
	@Value("${email.user.get}")
	private String userGet;//获取用户信息
	@Value("${email.user.update}")
	private String userUpdate;//更新用户信息
	@Value("${email.user.delete}")
	private String userDelete;//删除用户信息
	@Value("${email.user.simplelist}")
	private String userSimpleList;
	
	@Value("${email.system.email}")
	private String systemEmail;
	@Value("${email.default.pwd}")
	private String defaultPwd;//邮箱默认密码
	public String getCorpid() {
		return corpid;
	}
	public void setCorpid(String corpid) {
		this.corpid = corpid;
	}
	public String getCorpsecret() {
		return corpsecret;
	}
	public void setCorpsecret(String corpsecret) {
		this.corpsecret = corpsecret;
	}
	public String getGetTokenUrl() {
		return getTokenUrl;
	}
	public void setGetTokenUrl(String getTokenUrl) {
		this.getTokenUrl = getTokenUrl;
	}
	public String getDepartmentCreate() {
		return departmentCreate;
	}
	public void setDepartmentCreate(String departmentCreate) {
		this.departmentCreate = departmentCreate;
	}
	public String getUserCreate() {
		return userCreate;
	}
	public void setUserCreate(String userCreate) {
		this.userCreate = userCreate;
	}
	public String getDepartmentSearch() {
		return departmentSearch;
	}
	public void setDepartmentSearch(String departmentSearch) {
		this.departmentSearch = departmentSearch;
	}
	public String getUserGet() {
		return userGet;
	}
	public void setUserGet(String userGet) {
		this.userGet = userGet;
	}
	public String getUserUpdate() {
		return userUpdate;
	}
	public void setUserUpdate(String userUpdate) {
		this.userUpdate = userUpdate;
	}
	public String getUserDelete() {
		return userDelete;
	}
	public void setUserDelete(String userDelete) {
		this.userDelete = userDelete;
	}
	public String getUserSimpleList() {
		return userSimpleList;
	}
	public void setUserSimpleList(String userSimpleList) {
		this.userSimpleList = userSimpleList;
	}
	public List<String> getSystemEmail() {
		List<String> list = new ArrayList<String>();
		//多个邮箱号
		if(systemEmail.contains(",")) {
			String[] strArray = systemEmail.split(",");
			for (int i = 0; i < strArray.length; i++) {
				list.add(strArray[i]);
			}
		}
		return list;
	}
	public void setSystemEmail(String systemEmail) {
		this.systemEmail = systemEmail;
	}
	public String getDepartmentList() {
		return departmentList;
	}
	public void setDepartmentList(String departmentList) {
		this.departmentList = departmentList;
	}
	public String getDepartmentDelete() {
		return departmentDelete;
	}
	public void setDepartmentDelete(String departmentDelete) {
		this.departmentDelete = departmentDelete;
	}
	public String getDefaultPwd() {
		return defaultPwd;
	}
	public void setDefaultPwd(String defaultPwd) {
		this.defaultPwd = defaultPwd;
	}

}
