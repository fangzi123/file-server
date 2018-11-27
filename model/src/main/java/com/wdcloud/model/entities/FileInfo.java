package com.wdcloud.model.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "oss_file_info")
public class FileInfo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "origin_name")
    private String originName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "created_time")
    private Date createdTime;

    /**
     * 是否转换 0不转换1转换
     */
    @Column(name = "convert_need")
    private Integer convertNeed;

    /**
     * 转换状态 -1:失败 1:成功
     */
    @Column(name = "convert_status")
    private Integer convertStatus;

    @Column(name = "convert_type")
    private String convertType;

    @Column(name = "convert_time")
    private Date convertTime;

    @Column(name = "convert_result")
    private String convertResult;

    /**
     * 转换异常
     */
    @Column(name = "convert_error_msg")
    private String convertErrorMsg;

    private static final long serialVersionUID = 1L;
}