package cn.xzy.module.member.convert.auth;

import cn.xzy.module.member.controller.app.auth.vo.*;
import cn.xzy.module.member.controller.app.social.vo.AppSocialUserUnbindReqVO;
import cn.xzy.module.member.controller.app.user.vo.AppMemberUserResetPasswordReqVO;
import cn.xzy.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import cn.xzy.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import cn.xzy.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import cn.xzy.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import cn.xzy.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.xzy.module.system.api.social.dto.SocialUserUnbindReqDTO;
import cn.xzy.module.system.api.social.dto.SocialWxJsapiSignatureRespDTO;
import cn.xzy.module.system.enums.sms.SmsSceneEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    SocialUserBindReqDTO convert(Long userId, Integer userType, AppAuthSocialLoginReqVO reqVO);
    SocialUserUnbindReqDTO convert(Long userId, Integer userType, AppSocialUserUnbindReqVO reqVO);

    SmsCodeSendReqDTO convert(AppAuthSmsSendReqVO reqVO);
    SmsCodeUseReqDTO convert(AppMemberUserResetPasswordReqVO reqVO, SmsSceneEnum scene, String usedIp);
    SmsCodeUseReqDTO convert(AppAuthSmsLoginReqVO reqVO, Integer scene, String usedIp);

    AppAuthLoginRespVO convert(OAuth2AccessTokenRespDTO bean, String openid);

    SmsCodeValidateReqDTO convert(AppAuthSmsValidateReqVO bean);

    SocialWxJsapiSignatureRespDTO convert(SocialWxJsapiSignatureRespDTO bean);

}
