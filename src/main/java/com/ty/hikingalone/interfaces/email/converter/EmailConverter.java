package com.ty.hikingalone.interfaces.email.converter;

import com.ty.hikingalone.application.email.cmd.GetCodeCmd;
import com.ty.hikingalone.application.email.cmd.VerifyCodeCmd;
import com.ty.hikingalone.interfaces.email.dto.EmailDTO;
import com.ty.hikingalone.interfaces.email.dto.EmailVerifyDTO;
import com.ty.hikingalone.interfaces.email.vo.EmailSendVO;
import org.springframework.stereotype.Component;

/**
 * 邮箱模块接口层转换器：HTTP DTO → 应用层命令；应用层结果 → 视图对象
 */
@Component
public class EmailConverter {

    public GetCodeCmd toSendEmailCmd(EmailDTO emailDTO) {
        if (emailDTO == null) {
            return null;
        }
        return new GetCodeCmd(emailDTO.getEmail());
    }

    public VerifyCodeCmd toVerifyCodeCmd(EmailVerifyDTO emailVerifyDTO) {
        if (emailVerifyDTO == null) {
            return null;
        }
        return new VerifyCodeCmd(emailVerifyDTO.getEmail(), emailVerifyDTO.getCode());
    }

    public EmailSendVO toEmailSendVO(boolean sent) {
        return EmailSendVO.builder()
                .sent(sent)
                .build();
    }

}
