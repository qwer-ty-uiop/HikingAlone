package com.ty.hikingalone.interfaces.email.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 发送验证码响应体
 * <p>不携带验证码本体（code 只能通过邮件收到），只告诉前端是否已发送</p>
 */
@Data
@Builder
public class EmailSendVO {

    /** true=已发送；false=冷却中（60s 内重复请求），未发送 */
    private Boolean sent;

}
