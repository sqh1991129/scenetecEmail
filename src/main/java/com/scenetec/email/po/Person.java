package com.scenetec.email.po;

import java.util.List;

public class Person {
	
	private String userId;//成员UserID。企业邮帐号名，邮箱格式
	private String name;//成员名称
	private List<String> department;//成员所属部门id列表，不超过20个
	private String position;//职位信息。长度为0~64个字节
	private String mobile;//手机号
	private String tel;//座机号码
	private String extid;//编号
	private String gender;//性别。1表示男性，2表示女性
	private String[] slaves;//别名列表Slaves 上限为5个 Slaves 为邮箱格式
    private String password;//密码
    private String cpwdLogin;//用户重新登录时是否重设密码, 登陆重设密码后，该标志位还原。0表示否，1表示是，缺省为0
    private String enable;
    
	public String getEnable() {
        return enable;
    }
    public void setEnable(String enable) {
        this.enable = enable;
    }
    public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getExtid() {
		return extid;
	}
	public void setExtid(String extid) {
		this.extid = extid;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCpwdLogin() {
		return cpwdLogin;
	}
	public void setCpwdLogin(String cpwdLogin) {
		this.cpwdLogin = cpwdLogin;
	}
	public List<String> getDepartment() {
		return department;
	}
	public void setDepartment(List<String> department) {
		this.department = department;
	}
	public String[] getSlaves() {
		return slaves;
	}
	public void setSlaves(String[] slaves) {
		this.slaves = slaves;
	}
    
    
    
}
