package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "message")
public class MessageEntity implements Serializable {
    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;

    @Indexed
    private Integer senderId;

    private String senderPhoto = "https://thirdwx.qlogo.cn/mmopen/vi_32/fSlhbFAusGQNMGGHfSbgXefZKcUuKpkt9CIQePm0qPPELXZfYVickmNOTS88t4801Co8WpxwwBdnPcicwIkfibNMw/132";

    private  String senderName;

    private String msg;

    @Indexed
    private Date sendTime;
}
