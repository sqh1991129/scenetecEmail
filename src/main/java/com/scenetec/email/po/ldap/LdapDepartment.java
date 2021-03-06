package com.scenetec.email.po.ldap;

import java.util.Objects;

public class LdapDepartment {

    private String name;

    private String parentName;

    private boolean needAdd = false;
    
    private String parentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public boolean isRoot() {
        return parentName == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LdapDepartment that = (LdapDepartment) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(parentName, that.parentName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, parentName);
    }

    public boolean isNeedAdd() {
        return needAdd;
    }

    public void setNeedAdd(boolean needAdd) {
        this.needAdd = needAdd;
    }

    @Override
    public String toString() {
        return "LdapDepartment{" +
                "name='" + name + '\'' +
                ", parentName='" + parentName + '\'' +
                ", needAdd=" + needAdd +
                '}';
    }

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
    
    
}
