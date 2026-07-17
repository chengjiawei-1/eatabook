package com.example.springboot.entity;

public class SideBarData {
    private Integer totalPosts;
    private Integer totalUsers;
    private Integer totalCategories;

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(Integer totalCategories) {
        this.totalCategories = totalCategories;
    }
}
