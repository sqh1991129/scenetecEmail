package com.scenetec.email.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.scenetec.email.exception.LdapConnectErrorException;
import com.scenetec.email.po.Department;
import com.scenetec.email.po.ldap.LdapDepartment;
import com.scenetec.email.po.ldap.LdapPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class LdapManager {
    @Value("${ldap.url}")
    private String URL;
    @Value("${ldap.basedn}")
    private String BASEDN;  // 根据自己情况进行修改
    @Value("${ldap.password}")
    private String password;
    @Value("${ldap.admin}")
    private String root;
    private final String FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private LdapContext ctx = null;
    private final Control[] connCtls = null;

    private final Logger logger = LoggerFactory.getLogger(LdapManager.class);


    public List<LdapPerson> search(){

        List<LdapPerson> ldapPeopleList = new ArrayList<>();
        LDAP_connect();
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration en = null;
        try {
            en = ctx.search("", "uid=*", constraints);
        } catch (Exception e) {
            throw new LdapConnectErrorException("ldap 连接失败");
        }
        while (en != null && en.hasMoreElements()) {
            Object obj = en.nextElement();
            if (obj instanceof SearchResult) {
                SearchResult si = (SearchResult) obj;
                ldapPeopleList.add(parseFromSi(si));
            }
        }
        try {
            ctx.close();
        } catch (NamingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        if (CollectionUtils.isEmpty(ldapPeopleList)) {
            throw new LdapConnectErrorException("无法获取ldap信息");
        }
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
                if (attrId.equals("mail")) {
                    String email = (String) attribute.get();
                    ldapPerson.setEmail(email);
                }
                if (attrId.equals("sn")) {
                    String sn = (String) attribute.get();
                    ldapPerson.setSn(sn);
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
}

