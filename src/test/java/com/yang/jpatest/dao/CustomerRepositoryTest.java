package com.yang.jpatest.dao;

import com.yang.jpatest.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerDao;

    /**
     * 保存客户：调用save(obj)方法
     */
    @Test
    public void testSave() {
        Customer c = new Customer();
        c.setCustName("测试用户1");
        customerDao.save(c);
    }

    /**
     * 修改客户：调用save(obj)方法
     *      对于save方法的解释：如果执行此方法是对象中存在id属性，即为更新操作会先根据id查询，再更新
     *                      如果执行此方法中对象中不存在id属性，即为保存操作
     *
     */
    @Test
    public void testUpdate() {
        //根据id查询id为1的客户【若不先查出,实体类属性赋值的时候需要注意被赋null】
        Optional<Customer> customer = customerDao.findById(1L);
        //修改客户名称
        customer.get().setCustName("测试修改用户名");
        //更新
        customerDao.save(customer.get());
    }

    /**
     * 根据id删除：调用delete(id)方法
     */
    @Test
    public void testDelete() {
        customerDao.deleteById(1L);
    }

    /**
     * 根据id查询：调用findOne(id)方法
     */
    @Test
    public void testFindById() {
        Optional<Customer> customer = customerDao.findById(2L);
        System.out.println(customer.get().toString());
    }

    /**
     * 查询全部的数据
     */
    @Test
    public void testFindAll(){
        List<Customer> list = customerDao.findAll();
        list.forEach(i -> System.out.println(i.toString()));
    }

    /**
     * 统计查询
     */
    @Test
    public void testCount(){
        Long count = customerDao.count();
        System.out.println(count);
    }

    /**
     * 查询此id的数据是否存在
     */
    @Test
    public void testExists(){
        Boolean flag = customerDao.existsById(4L);
        System.out.println(flag);
    }

    /**
     * 查询此id的数据是否存在
     * findOne:
     *      em.find()  立即加载
     * getOne:
     *      em.getReference()   延迟加载【采用动态代理对象】
     *      返回的是一个客户的动态代理对象
     *      什么时候用，什么时候进行查询
     * 所以为了保持事物的完整性，需要加@Transactional来防止报错
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    public void testGetOne(){
        Customer customer= customerDao.getOne(4L);
        System.out.println(customer);
    }

    @Test
    public void testFindAllSql(){
        List<Customer> list = customerDao.findAllSql();
        list.forEach(i -> System.out.println(i.toString()));
    }
}