package com.scenetec.email.po.weixin;

import java.util.List;

public class WeixinMemberSearchResult {

    private String userid;

    private String name;

    private List<Integer> department;

    private boolean needDelete;

    private boolean needUpdate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getDepartment() {
        return department;
    }

    public void setDepartment(List<Integer> department) {
        this.department = department;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public boolean isNeedDelete() {
        return needDelete;
    }

    public void setNeedDelete(boolean needDelete) {
        this.needDelete = needDelete;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    @Override
    public String toString() {
        return "WeixinMemberSearchResult{" +
                "userid='" + userid + '\'' +
                ", name='" + name + '\'' +
                ", department=" + department +
                ", needDelete=" + needDelete +
                ", needUpdate=" + needUpdate +
                '}';
    }
}
