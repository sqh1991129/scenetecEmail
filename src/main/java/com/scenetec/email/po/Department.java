package com.scenetec.email.po;

public class Department {

	private String name;//部门名称
	private String parentId;//父部门id。id为1可表示根部门
	private String order;//在父部门中的次序值。order值小的排序靠前，1-10000为保留值，若使用保留值，将被强制重置为0
	private String parmentName;//父部门名称
	private Department department;
	private boolean isRoot;//是否是根部门
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getParmentName() {
		return parmentName;
	}
	public void setParmentName(String parmentName) {
		this.parmentName = parmentName;
	}
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}
	public boolean isRoot() {
		return isRoot;
	}
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	
}
