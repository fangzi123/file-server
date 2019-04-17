-- auto-generated definition
create table oss_file_info
(
  id                bigint auto_increment
    primary key,
  file_id           varchar(64)                         not null,
  origin_name       varchar(128)                        null,
  file_size         bigint                              not null,
  file_type         varchar(16)                         not null,
  created_time      timestamp default CURRENT_TIMESTAMP null,
  convert_need      int default '0'                     null
  comment '是否转换 0不转换1转换',
  convert_status    int default '0'                     not null
  comment '转换状态 -1:失败 1:成功',
  convert_type      varchar(8)                          null,
  convert_count     int default '0'                     null
  comment '是否次数',
  convert_time      timestamp                           null,
  convert_result    varchar(512)                        null,
  convert_error_msg varchar(4096)                        null
  comment '转换异常'
)
 comment 'oss文件信息';
CREATE INDEX oss_file_info_file_id_index ON oss_file_info (file_id);

