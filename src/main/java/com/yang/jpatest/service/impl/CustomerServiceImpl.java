package com.yang.jpatest.service.impl;

import com.yang.jpatest.dao.CustomerRepository;
import com.yang.jpatest.entity.Customer;
import com.yang.jpatest.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Resource
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Customer customer) {
        customerRepository.delete(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Customer customer) {
        /**
         * 修改的方法有四种
         * 1. 通过set方法修改：直接通过主键查询，然后set对应需要修改的字段，
         *    方便但是大量操作下效率很低，修改一个字段就生成一个sql
         * 2. JPAQL语句修改：语法与sql不同，这里使用实体类的字段作为，而不是数据库
         * 3. SQL语句修改：与JPAQL语句修改不同的是调用的是EntityManager的createNativeQuery方法。
         * 4. 标准查询修改：采用CriteriaBuilder标准构造器
         */
        /*1.通过set方法修改 */
        Customer customer1 = customerRepository.getOne(customer.getCustId());
        customer1.setCustAddress("???");
        customer1.setCustName("....");
        customerRepository.save(customer);
        /*2.JPAQL语句修改*/
        Class clazz = customer.getClass();
        String jpql = "UPDATE" + clazz.getName() + "tb SET tb.cust_name=:name " +
                "WHERE tb.custId =:id AND tb.custLevel =:custLevel";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("id",customer.getCustId())
                .setParameter("name",customer.getCustName())
                .setParameter("custLevel",customer.getCustLevel());
        query.executeUpdate();
        /*3.SQL语句修改  重点在dao层上的@Query注解,
        nativeQuery=true表示是原生SQL,nativeQuery=false表示不是原生SQL*/
        Customer updateCustomer = customerRepository.update(customer);
        /*4.标准查询修改 */
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Customer> update = cb.createCriteriaUpdate(Customer.class);
        Root<Customer> root = update.from(Customer.class);
        Path<String> address = root.get("custAddress");
        update.set(address,customer.getCustAddress());
        update.where(cb.equal(root.get("custId"),customer.getCustId()));
        Query q = entityManager.createQuery(update);
        // entityManager.merge(customer);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> findSort() {
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "cust_id"));
    }

    @Override
    public Object findPage(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public List<Customer> findMoreField1(Customer customer) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Customer> query = criteriaBuilder.createQuery(Customer.class);
        TypedQuery<Customer> typedQuery = entityManager.createQuery(query);
        // setFirstResult表示从第几条记录开始
        typedQuery.setFirstResult(0);
        // setMaxResults 是每页要查询的条数
        typedQuery.setMaxResults(100);
        Root<Customer> root = query.from(Customer.class);
        Predicate predicate = criteriaBuilder
                // 相等
                .equal(root.get("cust_id"), "???")
                // 包含
                .in();
        query.where(predicate);
        List<Customer> list = typedQuery.getResultList();
        return list;
    }

    @Override
    public List<Customer> findMoreField2(Customer customer) {
        Pageable pageable = PageRequest.of(1,10);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                //未简化：ExampleMatcher.GenericPropertyMatchers.endsWith()
                // custAddress字段参与匹配的方式(结尾含有)
                .withMatcher("custAddress",match -> match.endsWith())
                // isFace字段不参于匹配
                .withIgnorePaths("isFace");
        Example<Customer> example = Example.of(customer,exampleMatcher);
        Page<Customer> pageList = customerRepository.findAll(example,pageable);
        return pageList.getContent();
    }
}
