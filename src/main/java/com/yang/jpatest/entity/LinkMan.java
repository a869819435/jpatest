package com.yang.jpatest.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 联系人的实体类（数据模型）
 */
@Entity
@Table(name="cst_linkman")
@Data
public class LinkMan implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lkm_id")
    private Long lkmId;
    @Column(name = "lkm_name")
    private String lkmName;
    @Column(name = "lkm_gender")
    private String lkmGender;
    @Column(name = "lkm_phone")
    private String lkmPhone;
    @Column(name = "lkm_mobile")
    private String lkmMobile;
    @Column(name = "lkm_email")
    private String lkmEmail;
    @Column(name = "lkm_position")
    private String lkmPosition;
    @Column(name = "lkm_memo")
    private String lkmMemo;

    //多对一关系映射：多个联系人对应客户
    @ManyToOne(targetEntity = Customer.class)
    @JoinColumn(name = "lkm_cust_id", referencedColumnName = "cust_id")
    //用它的主键，对应联系人表中的外键
    private Customer customer;
}