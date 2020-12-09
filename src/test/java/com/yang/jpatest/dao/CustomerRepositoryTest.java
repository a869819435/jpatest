package com.yang.jpatest.dao;

import com.yang.jpatest.entity.Customer;
import com.yang.jpatest.entity.LinkMan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
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

    @Autowired
    private LinkManDao linkManDao;

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

    /**
     * 保存一个客户，保存一个联系人(Rollback 关闭自动回滚)
     * 实体类采用双向关联(可能会多出一条无用update语句)
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false)
    public void OneToManyAdd(){
        // 创建一个客户、创建一个联系人
        Customer customer = new Customer();
        customer.setCustName("谷歌");

        LinkMan linkMan = new LinkMan();
        linkMan.setLkmName("谷歌客服1");
        /*
         * 配置了客户到联系人的关系
         *       从客户的角度上：发送两条insert语句，发送一条update语句更新数据库(更新外键)
         * 由于我们配置了客户到联系人的关系，客户可以对外键进行维护
         * */
        //customer.getLinkmans().add(linkMan);

        customerDao.save(customer);
        /*
        * 配置了联系人到客户的关系(多对一)
        *       只发送了两条insert语句
        * 由于配置了联系人到客户的映射关系(多对一)
        * */
        linkMan.setCustomer(customer);
        linkManDao.save(linkMan);
    }

    /**
     * 保存一个客户，保存一个联系人(Rollback 关闭自动回滚)
     * 实体类采用单向关联（对于客户仅用于声明，放弃维护权）
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false)
    public void OneToManyAdd1(){
        // 创建一个客户、创建一个联系人
        Customer customer = new Customer();
        customer.setCustName("谷歌");

        LinkMan linkMan = new LinkMan();
        linkMan.setLkmName("谷歌客服1");
        /*
         * 配置了客户到联系人的关系
         *       从客户的角度上：发送两条insert语句，发送一条update语句更新数据库(更新外键)
         * 由于我们配置了客户到联系人的关系，客户可以对外键进行维护
         * */
        //customer.getLinkmans().add(linkMan);

        customerDao.save(customer);
        /*
         * 配置了联系人到客户的关系(多对一)
         *       只发送了两条insert语句
         * 由于配置了联系人到客户的映射关系(多对一)
         * */
        linkMan.setCustomer(customer);
        linkManDao.save(linkMan);
    }

    /**
     * 删除数据：
     *      删除主表的时候分为两种情况
     *          1.主表有外键维护权：如果外键被占用会把从表的外键置为null
     *          2.主表没有外键维护权：如果外键被占用会把报错，不给删除，如果需要删除就要用级联删除去完成
     *      删除从表无特殊情况
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false)
    public void OneToManyDelete(){
        // 创建一个客户、创建一个联系人
        Customer customer = new Customer();
        customer.setCustName("谷歌");

        LinkMan linkMan = new LinkMan();
        linkMan.setLkmName("谷歌客服1");
        /*
         * 配置了客户到联系人的关系
         *       从客户的角度上：发送两条insert语句，发送一条update语句更新数据库(更新外键)
         * 由于我们配置了客户到联系人的关系，客户可以对外键进行维护
         * */
        //customer.getLinkmans().add(linkMan);

        customerDao.save(customer);
        /*
         * 配置了联系人到客户的关系(多对一)
         *       只发送了两条insert语句
         * 由于配置了联系人到客户的映射关系(多对一)
         * */
        linkMan.setCustomer(customer);
        linkManDao.save(linkMan);
    }

    /**
     * 级联添加：保存一个客户的同时，保存客户的所有联系人
     *      需要在操作的主体类上,配置casacde属性
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false)
    public void OneToManyCascadeAdd(){
        // 创建一个客户、创建一个联系人
        Customer customer = new Customer();
        customer.setCustName("谷歌2");

        LinkMan linkMan = new LinkMan();
        linkMan.setLkmName("谷歌客服2");

        customer.getLinkmans().add(linkMan);
        linkMan.setCustomer(customer);

        customerDao.save(customer);
    }

    /**
     * 级联删除：删除一个客户的同时，删除客户的所有联系人
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false)
    public void OneToManyCascadeDelete(){
        // 查询id为22的客户
        Optional<Customer> customerOptional = customerDao.findById(22L);
        // 删除id为22的客户
        customerDao.delete(customerOptional.get());
    }
}