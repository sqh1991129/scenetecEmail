package com.scenetec.email.bean;

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
	

}
