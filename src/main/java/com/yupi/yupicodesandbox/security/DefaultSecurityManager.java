package com.yupi.yupicodesandbox.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{

    /**
     * 检查所有的权限
     * @param perm   the requested permission.
     */
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何的权限限制");
        System.out.println(perm);
        //super.checkPermission(perm);
    }
}
