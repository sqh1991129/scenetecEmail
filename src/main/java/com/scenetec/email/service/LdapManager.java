package com.scenetec.email.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.scenetec.email.po.Department;
import com.scenetec.email.po.EmailInfo;
import com.scenetec.email.po.ldap.LdapDepartment;
import com.scenetec.email.po.ldap.LdapPerson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdapManager {
    @Value("${ldap.url}")
    private String URL;
    @Value("${ldap.basedn}")
    private String BASEDN;  // 根据自己情况进行修改
    @Value("${ldap.password}")
    private String password;
    @Value("cn=admin,dc=scenetec,dc=com")
    private String root;
    private final String FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private LdapContext ctx = null;
    private final Control[] connCtls = null;

    public List<LdapPerson> search() throws NamingException {

        List<LdapPerson> ldapPeopleList = new ArrayList<LdapPerson>();
        LDAP_connect();
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration en = null;
        try {
            en = ctx.search("", "uid=*", constraints);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        while (en != null && en.hasMoreElements()) {
            Object obj = en.nextElement();
            if (obj instanceof SearchResult) {
                SearchResult si = (SearchResult) obj;
                ldapPeopleList.add(parseFromSi(si));
            }
        }
        ctx.close();
        return ldapPeopleList;
    }

    public List<LdapDepartment> getDepartmentTreeFromPerson(List<LdapPerson> ldapPeopleList) {

        List<LdapDepartment> departmentList = new ArrayList<>();

        for (LdapPerson person : ldapPeopleList) {
            List<String> departments = person.getDepartments();

            for (int i=0; i < departments.size(); i++) {
                LdapDepartment ldapDepartment = new LdapDepartment();
                ldapDepartment.setName(departments.get(i));
                if (i < departments.size()-1) {
                    ldapDepartment.setParentName(departments.get(i+1));
                }
                if (!departmentList.contains(ldapDepartment)) {
                    departmentList.add(ldapDepartment);
                }
            }
        }
        return departmentList;
    }

    private LdapPerson parseFromSi(SearchResult si) {
        String siName = si.getName();
        // 格式： cn=xiaoming,ou=研发部门,ou=帝网
        String[] tokens = siName.split(",");
        LdapPerson ldapPerson = new LdapPerson();
        ldapPerson.setCn(tokens[0].split("=")[1]);
        // 部门从1开始
        List<String> departments = new ArrayList<>();
        for (int i=1; i < tokens.length; i++) {
            String ou = tokens[i];
            departments.add(ou.split("=")[1]);
        }
        ldapPerson.setDepartments(departments);
        Attributes attributes = si.getAttributes();

        for (NamingEnumeration ae = attributes.getAll(); ae.hasMoreElements();) {
            Attribute attribute = null;
            try {
                attribute = (Attribute) ae.next();
                String attrId = attribute.getID();
                if (attrId.equals("mobile")) {
                    String mobile = (String) attribute.get();
                    ldapPerson.setMobile(mobile);
                }
                if (attrId.equals("email")) {
                    String email = (String) attribute.get();
                    ldapPerson.setEmail(email);
                }

            } catch (NamingException e) {
                e.printStackTrace();
            }
        }

        return ldapPerson;
    }

    //处理人员机构信息
    public static List<Department> departmentInfo(String departmentStr) {
        List<Department> list = new ArrayList<Department>();
        //cn=111 222,ou=下一集,ou=测试
        String[] departmentArray = departmentStr.split(",");
        for (int i = departmentArray.length - 1; i > 0; i--) {
            Department department = new Department();
            String[] departmentName = departmentArray[i].split("=");
            department.setName(departmentName[1]);
            if (i == departmentArray.length - 1) {
                department.setRoot(true);
            } else {
                department.setRoot(false);
            }
            list.add(department);
        }
        return list;
    }

    //获取人员所在机构名称
    public String memberDepartment(String departmentStr) {
        if (departmentStr.contains(",")) {
            String[] departmentArray = departmentStr.split(",");
            String[] departmentName = departmentArray[1].split("=");
            return departmentName[1];
        }
        return null;
    }

    private void LDAP_connect() {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FACTORY);
        env.put(Context.PROVIDER_URL, URL + BASEDN);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        env.put(Context.SECURITY_PRINCIPAL, root);   // 管理员
        env.put(Context.SECURITY_CREDENTIALS, password);  // 管理员密码

        try {
            ctx = new InitialLdapContext(env, connCtls);
            System.out.println("连接成功");

        } catch (javax.naming.AuthenticationException e) {
            System.out.println("连接失败：");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("连接出错：");
            e.printStackTrace();
        }

    }

    private void closeContext() {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getBASEDN() {
		return BASEDN;
	}

	public void setBASEDN(String bASEDN) {
		BASEDN = bASEDN;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}
    
    
}

