package com.yupi.yupicodesandbox.security;

import java.security.Permission;

public class MySecurityManager extends SecurityManager {


    /**
     * 检查所有的权限
     *
     * @param perm the requested permission.
     */
    @Override
    public void checkPermission(Permission perm) {

        //在这里开启权限检查默认就是禁用所有的权限
        //super.checkPermission(perm);
    }

    //检测程序是否可以执行文件
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec 权限异常" + cmd);
    }



//检测程序是否可以读文件
    @Override
    public void checkRead(String file) {
//        if (file.contains("C:\\IdeaProjects\\yupi-code-sandbox")){
//            return;
//        }
        //throw new SecurityException("checkRead 权限异常" + file);
    }

    //检测程序是否可以执行写文件
    @Override
    public void checkWrite(String file) {
        //throw new SecurityException("checkWrite 权限异常" + file);
    }

    //检测程序是否可以删除文件
    @Override
    public void checkDelete(String file) {
        //throw new SecurityException("checkDelete 权限异常" + file);
    }

    //检测程序是否允许连接网络
    @Override
    public void checkConnect(String host, int port) {
        //throw new SecurityException("checkConnect 权限异常" + host + ":" + port);
    }
}
