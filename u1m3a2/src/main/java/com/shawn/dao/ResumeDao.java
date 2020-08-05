package com.shawn.dao;

import com.shawn.pojo.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author ：Shawn
 * @date ：Created in 2020/5/10 22:28
 * @description：
 * @modified By：
 * @version: $
 */

/**
 * One meets requirements of SpringDataJpa's Dao layer interface needs to extend JpaRepository & JpaSpecificationExecutor
 *
 * JpaRepository<Operation object Class, primary key type>
 *     encapsulate basic CRUD operation
 *
 * JpaSpecificationExecutor<Operation object class>
 *     encapsulate complex query (paging, order etc...)
 */
public interface ResumeDao extends JpaRepository<Resume,Long>, JpaSpecificationExecutor<Resume> {

    @Query("from Resume where id=?1 and name=?2")
    List<Resume> findByJpql(Long id, String name);

    /**
     * use native sql query need set nativeQuery to true
     * @param name
     * @param address
     * @return
     */
    @Query(value = "select * from tb_resume where name like ?1 and address like ?2", nativeQuery = true)
    List<Resume> findByJpql(String name, String address);

    /**
     * 方法命名规则查询
     * 按照name模糊查询（like）
     *  方法名以findBy开头
     *          -属性名（首字母大写）
     *                  -查询方式（模糊查询、等价查询），如果不写查询方式，默认等价查询
     */
    List<Resume> findByNameLikeAndAddress(String name,String address);


}
