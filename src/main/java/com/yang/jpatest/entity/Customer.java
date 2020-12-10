package com.yang.jpatest.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 客户实体类
 *      配置隐射关系
 *          1.实体类和表达映射关系
 *              @Entity  // 声明此类是一个实体类
 *              @Table("cst_customer")  // 配置实体类和表的映射关系
 *                  name: 配置数据库表的名称
 *          2.实体类中属性和表中字段的映射关系
 *
 * @author ywq
 */
@Setter
@Getter
@Entity  // 声明此类是一个实体类
@Table(name = "cst_customer")  // 配置实体类和表的映射关系
public class Customer implements Serializable {
    /**
     * 客户编号(主键)
     * @Id: 声明主键的配置
     * @GeneratedValue: 配置主键的生成策略
     *      GenerationType.IDENTITY 自增[mysql]
     *      SEQUENCE 序列(底层数据库必须支持序列) [oracle]
     *      TABLE: jpa提供一种机制，通过一张数据库表的形式帮助我们完成自主自增
     *      AUTO: 主键由程序控制(程序选择最佳的生成策略)
     * @Column: 配置属性和字段的映射关系
     *      name: 数据库中的字段名
     *      unique：是否唯一
     *      nullable：是否可以为空
     *      inserttable：是否可以插入
     *      updateable：是否可以更新
     *      columnDefinition: 定义建表时创建此列的DDL
     *      secondaryTable: 从表名。如果此列不建在主表上（默认建在主表），
     *                      该属性定义该列所在从表的名字搭建开发环境[重点]
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cust_id")
    private Long custId;
    /**
     * 客户名称(公司名称)
     */
    @Column(name = "cust_name")
    private String custName;
    /**
     * 客户信息来源
     */
    @Column(name = "cust_source")
    private String custSource;
    /**
     * 客户所属行业
     */
    @Column(name = "cust_industry")
    private String custIndustry;
    /**
     * 客户级别
     */
    @Column(name = "cust_level")
    private String custLevel;
    /**
     * 客户联系地址
     */
    @Column(name = "cust_address")
    private String custAddress;
    /**
     * 客户联系电话
     */
    @Column(name = "cust_phone")
    private String custPhone;

    /**
     * 配置客户和联系人的一对多关系
     */
//    @OneToMany(targetEntity = LinkMan.class)  //保留外键维护权
//    @JoinColumn(name = "lkm_cust_id",referencedColumnName = "cust_id")
    // 放弃外键维护权
    @OneToMany(mappedBy = "customer",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Set<LinkMan> linkmans = new HashSet<LinkMan>(0);
}