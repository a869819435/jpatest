package com.yang.jpatest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.Persistence;

@SpringBootTest
class JpatestApplicationTests {

    @Test
    void contextLoads() {
    }

    /**
     * 测试jpa保存
     */
    @Test
    public void testSave(){
        /**
         * 创建实体管理类工厂，借助Persistence的静态方法获取
         * 		其中传递的参数为持久化单元名称，需要jpa配置文件中指定
         */

        // 创建实体管理类

        // 获取事务对象

        // 开启事务

        // 保存操作

        // 提交事务（回滚事物）

        // 释放资源

    }

}
