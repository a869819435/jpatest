package com.yang.jpatest.dao;

import com.yang.jpatest.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

    /**
     * 查询单个对象，根据单条件查询
     */
    @Test
    public void specFindOne(){
        /* 自定义查询条件
        * 1. 实现Specification接口（提供泛型：查询的想类型）
        * 2. 实现toPredicate方法（构造查询条件）
        * 3. 需要借助方法参数中的两个参数（
        *           root：获取需要查询的属性
        *           criteriaBuilder：构造查询条件的，内部封装了很多的查询条件（模糊，精确）
        * ）
        * 案列：根据客户id查询，查询客户id为1
        *       查询条件
        *           1.查询方式(cb 对象)
        *           2.比较的属性名称(root对象)
        * */
        Specification<Customer> spec = new Specification<Customer>() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                // 1.获取比较的属性
                Path<Object> custId = root.get("custId");

                // 2.构造查询条件
            /* 进行精确的匹配
               第一个参数：需要比较的属性(path对象)
               第二个参数：需要比较的取值
            */
                Predicate predicate = criteriaBuilder.equal(custId, 2L);
                return predicate;
            }
        };
        Optional<Customer> one = customerDao.findOne(spec);
        System.out.println(one.get().toString());
    }

    /**
     * 查询多个对象，根据多条件查询
     */
    @Test
    public void specFindAll(){
        /* 案列：根据客户名称模糊和电话模糊查询
         *       查询条件
         *           1.查询方式(cb 对象)
         *           2.比较的属性名称(root对象)
         * */
        List<Customer> list = customerDao.findAll((root, criteriaQuery, criteriaBuilder) -> {
            // 1.获取比较的属性
            Path<Object> custName = root.get("custName");
            Path<Object> custPhone = root.get("custPhone");

            // 2.构造查询条件
            /* 进行模糊的匹配
               第一个参数：需要比较的属性(得到path对象，根据path对象指定比较类型的参数类型，再比较)
                        指定参数类型：path.as(类型的字节码对象)
               第二个参数：需要比较的取值
            */
            Predicate predicate = null;
            try {
                Field field1 = Customer.class.getDeclaredField("custName");
                Field field2 = Customer.class.getDeclaredField("custPhone");
                Class<?> type1 = field1.getType();
                Class<?> type2 = field2.getType();
                Predicate predicate1 = criteriaBuilder.like((Expression<String>) custName.as(type1), "测试%");
                Predicate predicate2 = criteriaBuilder.like((Expression<String>) custPhone.as(type2), "%1%");
                predicate = criteriaBuilder.and(predicate1, predicate2);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return predicate;
        });
        list.forEach(i -> System.out.println(i.toString()));
    }

    /**
     * 查询列表排序
     */
    @Test
    public void specFindSort(){
        /*
        * 创建排序对象，需要调用静态方法实例化sort对象(2.x之后不允许实例化Sort)
        * 第一个参数：排序的顺序(倒叙Sort.Direction.DESC，正序Sort.Direction.ASC)
        * 第二个参数：排序的属性名
        * */
        List<Customer> all = customerDao.findAll(Sort.by(Sort.Direction.DESC,"custId"));
        all.forEach(i -> System.out.println(i.toString()));
    }

    /**
     * 分页查询列表
     * PageRequest.of静态方法实例化一个分页参数对象
     * Specification:查询条件
     * Pageable: 分页参数（查询的页码(从0开始)，每页查询的条数，排序条件(如果有需要)）
     * 返回：Page (Spring Data JPA封装好的PageBean对象，数据列表，总条数)
     */
    @Test
    public void specFindPage(){
        Page<Customer> all = customerDao.findAll((root, criteriaQuery, criteriaBuilder) -> {
            try {
                Path<Object> custName = root.get("custName");
                Field field = Customer.class.getField("custName");
                Predicate predicate = criteriaBuilder.like((Expression<String>) custName.as(field.getType()),"%测试%");
                return predicate;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }
        }, PageRequest.of(0,2,Sort.by(Sort.Direction.DESC,"custId")));
        all.forEach(i -> System.out.println(i.toString()));
        // 得到的数据集合表
        System.out.println(all.getContent());
        // 得到总条数
        System.out.println(all.getTotalElements());
        // 得到总页数
        System.out.println(all.getTotalPages());
    }
}