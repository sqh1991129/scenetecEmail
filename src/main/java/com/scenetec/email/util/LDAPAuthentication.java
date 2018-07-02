package com.scenetec.email.util;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LDAPAuthentication {
    private final String URL = "ldap://154.8.178.194:389/";
    private final String BASEDN = "dc=scenetec,dc=com";  // 根据自己情况进行修改
    private final String FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private LdapContext ctx = null;
    private final Control[] connCtls = null;

    private void LDAP_connect() {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FACTORY);
        env.put(Context.PROVIDER_URL, URL + BASEDN);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        String root = "cn=admin,dc=scenetec,dc=com";  //根据自己情况修改
        env.put(Context.SECURITY_PRINCIPAL, root);   // 管理员
        env.put(Context.SECURITY_CREDENTIALS, "123456");  // 管理员密码

        try {
            ctx = new InitialLdapContext(env, connCtls);
            System.out.println( "连接成功" );

        } catch (javax.naming.AuthenticationException e) {
            System.out.println("连接失败：");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("连接出错：");
            e.printStackTrace();
        }

    }
    private void closeContext(){
        if (ctx != null) {
            try {
                ctx.close();
            }
            catch (NamingException e) {
                e.printStackTrace();
            }

        }
    }
    private String getUserDN(String uid) {
        String userDN = "";
        LDAP_connect();
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> en = ctx.search("", "uid=" + uid, constraints);

            if (en == null || !en.hasMoreElements()) {
                System.out.println("未找到该用户");
            }
            // maybe more than one element
            while (en != null && en.hasMoreElements()) {
                Object obj = en.nextElement();
                if (obj instanceof SearchResult) {
                    SearchResult si = (SearchResult) obj;
                    userDN += si.getName();
                    userDN += "," + BASEDN;
                } else {
                    System.out.println(obj);
                }
            }
        } catch (Exception e) {
            System.out.println("查找用户时产生异常。");
            e.printStackTrace();
        }

        return userDN;
    }

    public void search() throws NamingException {
    	LDAP_connect();
        SearchControls   constraints   =   new   SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration en = null;
        try {
            en = ctx.search("", "uid=*", constraints);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        while   (en   !=   null   &&   en.hasMoreElements())
        {
            Object   obj   =   en.nextElement();
            if(obj   instanceof   SearchResult)
            {
                SearchResult   si   =   (SearchResult)   obj;

                System.out.println( "name:   "   +   si.getName());//人员机构

                Attributes attrs   =   si.getAttributes();
                if   (attrs   ==   null)
                {
                    System.out.println( "No   attributes ");
                }
                else
                {
                    for   (NamingEnumeration   ae   =   attrs.getAll();   ae.hasMoreElements();)
                    { 
                        Attribute   attr   =   (Attribute)   ae.next();
                        String   attrId   =   attr.getID();//属性key值

                        for   (Enumeration vals = attr.getAll(); vals.hasMoreElements();)
                        {
                            System.out.print(attrId   +   ":   ");
                            Object   o   =   vals.nextElement();
                            if(o   instanceof   byte[])
                                System.out.println(new   String((byte[])o));
                            else
                                System.out.println(o);//属性value值
                        }
                    }
                }
            }
            else
            {
                System.out.println(obj);
            }
            System.out.println();
        }
        ctx.close();
    }

    public boolean list() throws NamingException {
        NamingEnumeration<NameClassPair> list = ctx.list("text1");

        while(list.hasMore()){
            NameClassPair ncp = list.next();
            String nou = ncp.getNameInNamespace();
            System.out.println(nou);
        }

        return false;
    }

    public boolean authenricate(String UID, String password) {
        boolean valide = false;
        String userDN = getUserDN(UID);

        try {
            ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDN);
            ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            ctx.reconnect(connCtls);
            System.out.println(userDN + " 验证通过");
            valide = true;
        } catch (AuthenticationException e) {
            System.out.println(userDN + " 验证失败");
            System.out.println(e.toString());
            valide = false;
        } catch (NamingException e) {
            System.out.println(userDN + " 验证失败");
            valide = false;
        }
        closeContext();
        return valide;
    }
    private  boolean addUser(String usr, String pwd) {

        try {
            LDAP_connect();
            BasicAttributes attrsbu = new BasicAttributes();
            BasicAttribute objclassSet = new BasicAttribute("objectclass");
            objclassSet.add("inetOrgPerson");
            attrsbu.put(objclassSet);
            attrsbu.put("sn", usr);
            attrsbu.put("cn", usr);
            attrsbu.put("uid", usr);
            attrsbu.put("userPassword", pwd);
            ctx.createSubcontext("uid=yorker", attrsbu);

            return true;
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
        closeContext();
        return false;
    }
    public static void main(String[] args) {
        LDAPAuthentication ldap = new LDAPAuthentication();

        ldap.LDAP_connect();

        try {
            ldap.search();
        } catch (NamingException e) {
            e.printStackTrace();
        }

        if(ldap.authenricate("yorker", "secret") == true){

            System.out.println( "该用户认证成功" );

        }
        //ldap.addUser("yorker","secret");

    }
}

