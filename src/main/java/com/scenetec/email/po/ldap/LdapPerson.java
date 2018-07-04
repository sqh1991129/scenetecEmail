package com.scenetec.email.po.ldap;

import java.util.List;

public class LdapPerson {

    // 人员所在机构
    private List<String> departments;
    // 人员全拼
    private String cn;
    // 人员中文名
    private String sn;
    // 邮箱
    private String email;
    // 手机号
    private String mobile;

    public String getDepartmentName() {
        if (departments != null && departments.size() > 0) {
            return departments.get(0);
        }
        return null;
    }

    public boolean needAdd = false;

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "LdapPerson{" +
                "departments=" + departments +
                ", cn='" + cn + '\'' +
                ", sn='" + sn + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", needAdd=" + needAdd +
                '}';
    }
}
