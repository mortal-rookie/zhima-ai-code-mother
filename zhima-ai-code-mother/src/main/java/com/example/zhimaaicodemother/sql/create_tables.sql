create database zhimaAicodeMother;

use zhimaAicodeMother;

create table if not exists user(
    id bigint primary key auto_increment comment'id',
    userAccount varchar(256)   not null comment'账号',
    userPassword varchar(512)  not null comment'密码',
    userName  varchar(256)   null comment'用户昵称',
    userAvatar varchar(1024)  null comment'用户头像',
    userProfile varchar(512)   null comment'用户简介',
    userRole varchar(256) default'user'  not null comment'用户角色',
    dailyGenQuota int default 2 not null comment'每日免费生成次数（管理员不受限制）',
    dailyGenUsed  int default 0 not null comment'今日已生成次数',
    genResetDate  date null comment'生成次数统计日期，跨天自动重置',
    editTime datetime default CURRENT_TIMESTAMP not null comment'编辑时间',
    createTime datetime default CURRENT_TIMESTAMP not null comment'创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment'更新时间',
    isDelete tinyint default 0 not null comment'是否删除',
    UNIQUE KEY uk_userAccount(userAccount),
    INDEX id_userNmae(userName)
)comment'用户' collate = utf8mb4_unicode_ci;


create table if not exists app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的提示词',
    codeGenType  varchar(64)                        null comment '代码生成类型',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey),-- 确保部署标识唯一
    INDEX idx_appName (appName),
    INDEX idx_userId (userId)
) comment '应用' collate=utf8mb4_unicode_ci;

create table chat_history
(
    id bigint auto_increment comment 'id' primary key ,
    message text not null comment '消息',
    messageType varchar(32) not null comment 'user/ai',
    appId bigint not null comment '应用id',
    userId bigint not null comment '创建用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete tinyint default 0 not null comment '是否删除',
    INDEX idx_appId(appid),-- 提升基于应用的查询性能
    INDEX idx_createTime(createTime),-- 提升基于时间的查询性能
    INDEX idx_appId_createTime(appId,createTime)-- 游标查询核心索引
) comment '对话历史' collate  = utf8mb4_unicode_ci;

alter table `user`
    add column `dailyGenUsed` int not null default 0 comment '今日已使用次数',
    add column `genResetData` date null comment '次数重置日期';
