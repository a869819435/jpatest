package com.yang.jpatest.service;

import com.yang.jpatest.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.jaxb.SpringDataJaxb;

import java.util.List;

public interface CustomerService {

    void save(Customer customer);

    void delete(Customer customer);

    void update(Customer customer);

    List<Customer> findAll();

    List<Customer> findSort();

    Object findPage(Pageable pageable);

    List<Customer> findMoreField1(Customer customer);

    List<Customer> findMoreField2(Customer customer);
}
