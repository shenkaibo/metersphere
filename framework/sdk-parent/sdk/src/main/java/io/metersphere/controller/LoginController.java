package io.metersphere.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.handler.ResultHolder;
import io.metersphere.dto.ServiceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.LoginRequest;
import io.metersphere.service.BaseDisplayService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.SSOLogoutService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping
public class LoginController {

    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseDisplayService baseDisplayService;
    @Resource
    private SSOLogoutService ssoLogoutService;
    @Value("${spring.application.name}")
    private String serviceId;
    @Value("${server.port}")
    private Integer port;

    @GetMapping(value = "/is-login")
    public ResultHolder isLogin(HttpSession session) throws Exception {
        RsaKey rsaKey = RsaUtil.getRsaKey();
        Object user = session.getAttribute("user");
        if (user != null) {
            UserDTO userDTO = baseUserService.getUserDTO((String) MethodUtils.invokeMethod(user, "getId"));
            if (StringUtils.isBlank(userDTO.getLanguage())) {
                userDTO.setLanguage(LocaleContextHolder.getLocale().toString());
            }
            baseUserService.autoSwitch(userDTO);
            SessionUser sessionUser = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
            SessionUtils.putUser(sessionUser);
            return ResultHolder.success(sessionUser);
        }
        return ResultHolder.error(rsaKey.getPublicKey());
    }

    @PostMapping(value = "/signin")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public ResultHolder login(@RequestBody LoginRequest request) {
        SessionUser sessionUser = SessionUtils.getUser();
        if (sessionUser != null) {
            if (!StringUtils.equals(sessionUser.getId(), request.getUsername())) {
                return ResultHolder.error(Translator.get("please_logout_current_user"));
            }
        }
        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LOCAL.name());
        ResultHolder result = baseUserService.login(request);
        // 登录是否提示修改密码
        boolean changePassword = baseUserService.checkWhetherChangePasswordOrNot(request);
        result.setMessage(BooleanUtils.toStringTrueFalse(changePassword));
        return result;
    }

    @GetMapping(value = "/currentUser")
    public ResultHolder currentUser() {
        return ResultHolder.success(SecurityUtils.getSubject().getSession().getAttribute("user"));
    }

    @GetMapping(value = "/signout")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, beforeEvent = "#msClass.getUserId(id)", type = OperLogConstants.LOGIN, title = "登出", msClass = SessionUtils.class)
    public ResultHolder logout() throws Exception {
        ssoLogoutService.logout(SecurityUtils.getSubject().getSession());
        SecurityUtils.getSubject().logout();
        return ResultHolder.success(StringUtils.EMPTY);
    }

    /*Get default language*/
    @GetMapping(value = "/language")
    public String getDefaultLanguage() {
        return baseUserService.getDefaultLanguage();
    }

    @GetMapping("display/file/{imageName}")
    public ResponseEntity<byte[]> image(@PathVariable("imageName") String imageName) throws IOException {
        return baseDisplayService.getImage(imageName);
    }

    @GetMapping(value = "/services")
    public List<ServiceDTO> services() {
        return List.of(new ServiceDTO(serviceId, port));
    }
}
