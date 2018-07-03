package com.scenetec.email.service;

import com.scenetec.email.exception.SyncWeixinDepartmentException;
import com.scenetec.email.po.ldap.LdapDepartment;
import com.scenetec.email.po.ldap.LdapPerson;
import com.scenetec.email.po.weixin.WeixinDepartment;
import com.scenetec.email.po.weixin.WeixinMemberAdd;
import com.scenetec.email.po.weixin.WeixinMemberSearchResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncLdapToWeixinService {

    @Resource
    private LdapManager ldapManager;

    @Resource
    private ScenetecWeixinService scenetecWeixinService;

    public void syncDepartment() {
        try {
            List<LdapPerson> ldapPeopleList = ldapManager.search();
            if (ldapPeopleList == null) {
                return;
            }
            List<LdapDepartment> ldapDepartments = ldapManager.getDepartmentTreeFromPerson(ldapPeopleList);
            List<WeixinDepartment> weixinDepartments = scenetecWeixinService.getDepartments(null);

            WeixinDepartment rootWeixinDepartment = getWeixinDepartmentRoot(weixinDepartments);

            Map<String, WeixinDepartment> weixinDepartmentMap = weixinDepartments.stream().collect(Collectors.toMap(WeixinDepartment::getName, item->item));
            Map<String, LdapDepartment> ldapDepartmentMap = ldapDepartments.stream().collect(Collectors.toMap(LdapDepartment::getName, item->item));

            for (LdapDepartment ldapDepartment: ldapDepartments) {
                String ldapDepartmentName = ldapDepartment.getName();
                WeixinDepartment weixinDepartment = weixinDepartmentMap.get(ldapDepartmentName);
                if (weixinDepartment == null) {
                    // 需要新增
                    ldapDepartment.setNeedAdd(true);
                }else {
                    weixinDepartment.setNeedDelete(false);
                }
            }

            for (LdapDepartment ldapDepartment: ldapDepartments) {
                if (ldapDepartment.isNeedAdd()) {
                    String parentLdapDepartmentName = ldapDepartment.getParentName();
                    WeixinDepartment weixinDepartment = new WeixinDepartment();
                    if (ldapDepartment.isRoot()) {
                        weixinDepartment.setName(ldapDepartment.getName());
                        weixinDepartment.setParentid(rootWeixinDepartment.getId());
                        scenetecWeixinService.createDepartment(weixinDepartment);
                    }else {
                        createDepartment(ldapDepartmentMap, weixinDepartmentMap, ldapDepartment, rootWeixinDepartment);
                    }
                }
            }

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void syncMember() {

        try {
            List<LdapPerson> ldapPeopleList = ldapManager.search();
            if (ldapPeopleList == null) {
                return;
            }
            List<WeixinDepartment> weixinDepartments = scenetecWeixinService.getDepartments(null);
            WeixinDepartment root = getWeixinDepartmentRoot(weixinDepartments);

            List<WeixinMemberSearchResult> weixinMemberSearchResults = scenetecWeixinService.searchUser(root.getId());
            Map<Integer, WeixinDepartment> weixinDepartmentMap = weixinDepartments.stream().collect(Collectors.toMap(WeixinDepartment::getId, item->item));

            Map<String, LdapPerson> ldapPersonMap = ldapPeopleList.stream().collect(Collectors.toMap(LdapPerson::getCn, item->item));
            Map<String, WeixinMemberSearchResult> weixinMemberSearchResultMap = weixinMemberSearchResults.stream().collect(Collectors.toMap(WeixinMemberSearchResult::getUserid, item->item));

            for (LdapPerson person: ldapPeopleList) {
                String cn = person.getCn();
                WeixinMemberSearchResult sr = weixinMemberSearchResultMap.get(cn);

                if (sr == null) {
                    person.needAdd = true;
                }else {
                    sr.setNeedDelete(false);
                    String ldapPersonDepartmentName = person.getDepartmentName();
                    Integer departmentId = sr.getDepartment().get(0);
                    WeixinDepartment srDepartment = weixinDepartmentMap.get(departmentId);
                    if (srDepartment != null ){
                        if(!ldapPersonDepartmentName.equals(srDepartment.getName())) {
                            sr.setNeedUpdate(true);
                            List<Integer> departments = new ArrayList<>();
                            departments.add(departmentId);
                            sr.setDepartment(departments);
                        }
                    }
                }

            }

            for (LdapPerson person: ldapPeopleList) {
                if (person.needAdd) {
                    String departmentName = person.getDepartmentName();
                    Integer departmentId = weixinDepartmentMap.get(departmentName).getId();
                    List<Integer> departments = new ArrayList<>();
                    departments.add(departmentId);
                    WeixinMemberAdd weixinMemberAdd = new WeixinMemberAdd(person.getCn(), person.getSn(), person.getMobile(), person.getEmail(), departments);
                    scenetecWeixinService.createUser(weixinMemberAdd);
                }
            }
            for (WeixinMemberSearchResult sr: weixinMemberSearchResults) {
                if (sr.isNeedUpdate()) {
                    scenetecWeixinService.updateUser(sr);
                }
                if (sr.isNeedDelete()) {
                    scenetecWeixinService.deleteUser(sr.getUserid());
                }
            }

        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    private WeixinDepartment getWeixinDepartmentRoot(List<WeixinDepartment> weixinDepartments) {

        for (WeixinDepartment department: weixinDepartments) {
            if (department.getParentid() == 0) {
                return department;
            }
        }
        return null;
    }

    private void createDepartment(Map<String, LdapDepartment> ldapDepartmentMap, Map<String, WeixinDepartment> weixinDepartmentMap, LdapDepartment ldapDepartment, WeixinDepartment rootWeixinDepartment) {
        LdapDepartment parentLdapDepartment = ldapDepartmentMap.get(ldapDepartment.getParentName());

        if (parentLdapDepartment!=null && parentLdapDepartment.isNeedAdd()) {
            createDepartment(ldapDepartmentMap, weixinDepartmentMap, parentLdapDepartment, rootWeixinDepartment);
        }else {
            WeixinDepartment weixinDepartment = new WeixinDepartment();
            weixinDepartment.setName(ldapDepartment.getName());

            if (parentLdapDepartment == null) {
                weixinDepartment.setParentid(rootWeixinDepartment.getId());
            }else {
                WeixinDepartment parentWeixinDepartment = weixinDepartmentMap.get(parentLdapDepartment.getName());
                if (parentWeixinDepartment == null) {
                    throw new SyncWeixinDepartmentException("无法获取父节点");
                }
                weixinDepartment.setParentid(parentWeixinDepartment.getId());
            }

            ldapDepartment.setNeedAdd(false);
            scenetecWeixinService.createDepartment(weixinDepartment);
        }
    }

}
