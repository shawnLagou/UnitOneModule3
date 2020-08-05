package com.shawn.pojo;

import javax.persistence.*;

/**
 * @author ：Shawn
 * @date ：Created in 2020/5/9 22:36
 * @description：Resume object class (create map relation between database and object class)
 * 1. map relation between Entity class and database table
 * @Entity
 * @Table
 * 2. map relation between attribute of Entity class and field of database table
 * @Id identify primary key
 * @GeneratedValue identify generated strategy of primary key
 * @Column create maps of attributes and fields
 * @modified By：
 * @version: $
 */

@Entity
@Table(name = "tb_resume")
public class Resume {

    @Id
    /**
     * two types of generated strategy:
     * 1. GenerationType.IDENTITY: relay primary auto-increased function in database  Mysql
     * 2. GenerationType.SEQUENCE: relay sequence to produce primary key  Oracle
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Resume{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
