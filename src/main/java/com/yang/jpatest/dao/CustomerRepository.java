package com.yang.jpatest.dao;

import com.yang.jpatest.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * JpaRepository<实体类类型，主键类型>：用来完成基本CRUD操作
 * JpaSpecificationExecutor<实体类类型>：用于复杂查询（分页等查询操作）
 *
 * @author ywq
 */
public interface CustomerRepository extends JpaRepository<Customer,Long>, JpaSpecificationExecutor<Customer> {

    /**
     * 表明是修改或删除操作，jpql无插入操作 @Modifying
     * 书写sql或者jpql语句@Query
     * @param customer
     * @return
     */
    @Modifying
    @Query(value = "UPDATE cst_customer SET cust_name = #{#customer.custName} " +
            "WHERE cust_address = #{#customer.custAddress}",nativeQuery = true)
    Customer update(Customer customer);

    /**
     * 使用jpql根据名称查询
     * ?1代表参数的占位符，其中1对应方法中的参数索引
     * @param name
     * @return
     */
    @Query(value = "from Customer where custName = ?1")
    Customer findJPQL(String name);

    /**
     * 使用jpql完成更新操作
     *      * 在jpql完成更新/删除的操作的时候，在DAO方法上@Modifying
     *      * 必须在调用的方法上使用事物注解
     * @param custName
     * @param custId
     * @return
     */
    @Modifying
    @Query(value = "UPDATE Customer SET custName = ?1 " +
            "WHERE custId = ?2")
    void updateCustomer(String custName,Long custId);

    /**
     * 使用sql语句查询全部客户信息
     * @return
     */
    @Query(value = "SELECT * FROM cst_customer",nativeQuery = true)
    List<Customer> findAllSql();

    Customer findByCustName(String custName);

    Customer findByCustNameLike(String custName);

    List<Customer> findByCustNameLikeAndCustIndustry(String custName,String custIndustry);
}
