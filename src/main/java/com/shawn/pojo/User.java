package com.shawn.pojo;

/**
 * @author ：Shawn
 * @date ：Created in 2020/5/9 22:36
 */
public class User {

    @Override
    public String toString() {
        return "User [username=" + username + ", password="
                + password + ", roleName=" + "]";
    }

    private String username; // 主键
    private String password; // 密码

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
