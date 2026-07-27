package com.example.zhimaaicodemother.model.entity;


import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * @author fanren
 */
@Data //lombok注解，自动生成get，set。toString，equals，hashCode方法
@Builder//lombok注解，配合@NoArgsConstrctor和@AllArgsConstructor使用
@NoArgsConstructor//lombok注解，生成无参构造方法
@AllArgsConstructor//lombok注解，生成全参构造方法
@Table("user")//mybatis-flex注解，指定表名
public class User implements Serializable {

    @Serial//指定序列化版本号
    private static final Long serilVersionUID = 1L;

    /**
     * ID
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)//指定主键
    private Long id;
    /**
     * 账号
     */
    @Column("userAccount")//mybatis-flex注解，括号内是数据库字段名，为了防止属性名与字段名不同
    private String userAccount;
    /**
     *密码
     */
    @Column("userPassword")
    private String userPassword;
    /**
     *用户昵称
     */
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column("userProfile")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Column("userRole")
    private String userRole;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
