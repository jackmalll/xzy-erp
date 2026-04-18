package cn.xzy.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 钉钉免密登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthDingTalkLoginReqVO {

    @Schema(description = "钉钉免登授权码", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc123xyz")
    @NotEmpty(message = "授权码不能为空")
    private String authCode;

}
