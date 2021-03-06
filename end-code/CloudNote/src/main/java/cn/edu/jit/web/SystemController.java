package cn.edu.jit.web;

import cn.edu.jit.dto.UserDto;
import cn.edu.jit.entry.Area;
import cn.edu.jit.entry.Login;
import cn.edu.jit.entry.Message;
import cn.edu.jit.entry.User;
import cn.edu.jit.global.GlobalConstant;
import cn.edu.jit.global.GlobalFunction;
import cn.edu.jit.service.AreaService;
import cn.edu.jit.util.Sha1Utils;
import cn.edu.jit.service.LoginService;
import cn.edu.jit.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.PageFans;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.PageFansBean;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.javabeans.weibo.Company;
import com.qq.connect.oauth.Oauth;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 系统管理
 * @author jitwxs
 * @date 2018/1/2 19:24
 */
@Controller
public class SystemController {

    @Resource(name = "loginServiceImpl")
    private LoginService loginService;

    @Resource(name = "userServiceImpl")
    private UserService userService;

    @Resource(name = "areaServiceImpl")
    private AreaService areaService;

    /**
     * 获取当前用户id
     */
    private String getSelfId() {
        User user = userService.getByTel(GlobalFunction.getSelfTel());
        return user.getId();
    }

    private void initPath(HttpServletRequest request) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        GlobalConstant.SHARE_TEMPLATE  = loader.getResource("shareTemplate.html").getPath();
        GlobalConstant.TEMP_PATH = request.getSession().getServletContext().getRealPath("temp");
        GlobalConstant.UPLOAD_PATH  = request.getSession().getServletContext().getRealPath("upload");
        GlobalConstant.USER_HOME_PATH = GlobalConstant.UPLOAD_PATH  + "/"  + GlobalFunction.getSelfTel();
        GlobalConstant.USER_ARTICLE_PATH  = GlobalConstant.USER_HOME_PATH  + "/" + "article";
        GlobalConstant.USER_IMG_PATH = GlobalConstant.USER_HOME_PATH  + "/" + "images";
        GlobalConstant.USER_PAN_PATH = GlobalConstant.USER_HOME_PATH  + "/"  + "pan" ;
    }

    private void parserUser(User user, String key, String value) throws Exception {
        switch (key) {
            case "id":
                BeanUtils.copyProperties(userService.getById(value), user);
                break;
            case "name":
                user.setName(value);
                break;
            case "email":
                user.setEmail(value);
                break;
            case "area":
                int v = Integer.parseInt(value);
                if(v != -1) {
                    user.setArea(v);
                }
                break;
            case "icon":
                user.setIcon(value);
                break;
            case "sex":
                user.setSex(value);
                break;
            case "sign":
                user.setSign(value);
                break;
            default:
                break;
        }
    }

    @RequestMapping(value = "qqLogin", method = {RequestMethod.GET})
    public void qqLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        try {
            response.sendRedirect(new Oauth().getAuthorizeURL(request));
        } catch (QQConnectException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "afterQQLogin", method = {RequestMethod.GET})
    public void afterQQLoginGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        afterQQLoginPost(request,response);
    }
    @RequestMapping(value = "afterQQLogin", method = {RequestMethod.POST})
    public void afterQQLoginPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        try {
            AccessToken accessTokenObj = (new Oauth()).getAccessTokenByRequest(request);
            String accessToken = null, openID = null;
            long tokenExpireIn = 0L;
            if (accessTokenObj.getAccessToken().equals("")) {
                System.out.print("没有获取到响应参数");
            } else {
                accessToken = accessTokenObj.getAccessToken();
                tokenExpireIn = accessTokenObj.getExpireIn();
                request.getSession().setAttribute("demo_access_token", accessToken);
                request.getSession().setAttribute("demo_token_expirein", String.valueOf(tokenExpireIn));
                // 利用获取到的accessToken 去获取当前用的openid
                OpenID openIDObj = new OpenID(accessToken);
                openID = openIDObj.getUserOpenID();
                request.getSession().setAttribute("demo_openid", openID);
                UserInfo qzoneUserInfo = new UserInfo(accessToken, openID);
                UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();
                out.println("<br/>");
                if (userInfoBean.getRet() == 0) {
                    out.println(userInfoBean.getNickname() + "<br/>");
                    out.println(userInfoBean.getGender() + "<br/>");
                } else {
                    out.println("很抱歉，我们没能正确获取到您的信息，原因是： " + userInfoBean.getMsg());
                }
            }
        } catch (QQConnectException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "login", method = {RequestMethod.GET})
    public String loginUI() {
        return "login";
    }

    @RequestMapping(value = "loginCheck", method = {RequestMethod.POST})
    public void loginCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        Boolean status = true;

        String tel = request.getParameter("tel");
        String password = request.getParameter("password");

        Login login = loginService.getByTel(tel);

        // 账户不存在或密码错误
        if (login == null) {
            status = false;
        } else if(!Sha1Utils.validatePassword(password, login.getPassword())) {
            status = false;
        }

        Message msg = new Message();
        msg.setStatus(status);
        String data = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat);
        response.getWriter().write(data);
    }

    @RequestMapping(value = "login", method = {RequestMethod.POST})
    public String login(Login login, HttpServletRequest request) {
        // Shiro验证
        UsernamePasswordToken token = new UsernamePasswordToken(login.getTel(), login.getPassword());
        Subject subject = SecurityUtils.getSubject();

        // 如果获取不到用户名就是登录失败，登录失败会直接抛出异常
        subject.login(token);

        // 初始化项目路径
        initPath(request);

        // 所有用户均重定向对应首页
        if (subject.hasRole(GlobalConstant.ROLE.ADMIN.getName())) {
            GlobalConstant.HAS_SHOW_LOGIN_INFO = true;
            return "redirect:/admin/index";
        } else if (subject.hasRole(GlobalConstant.ROLE.USER.getName())) {
            GlobalConstant.HAS_SHOW_LOGIN_INFO = true;
            return "redirect:/user/index";
        } else {
            return "/login";
        }
    }

    @RequestMapping(value = "logout")
    public String logout(){
        return "redirect:/logout";
    }

    @RequestMapping(value = "register", method = {RequestMethod.GET})
    public String registerUI() {
        return "register";
    }

    @RequestMapping(value = "registerCheck", method = {RequestMethod.POST})
    public void registerCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Boolean status = true;
        String info = null;
        response.setContentType("text/html;charset=utf-8");
        String tel = request.getParameter("tel");
        String verityCode = request.getParameter("code");
        // 手机号已经被注册

        HttpSession session = request.getSession();
        String sendTel = (String)session.getAttribute("tel");
        String sendCode = (String)session.getAttribute("code");

        if (tel == null) {
            status = false;
            info = "请输入手机号";
        } else {
            if (loginService.getByTel(tel) != null) {
                status = false;
                info = "手机号已被注册";
            } else {
                if (sendTel == null) {
                    status = false;
                    info = "请发送验证码";
                } else {
                    if (!tel.equals(sendTel)) {
                        status = false;
                        info = "请勿随意修改手机号";
                    } else {
                        if (!verityCode.equals(sendCode)){
                            status = false;
                            info = "验证码输入错误";
                        }
                    }
                }
            }
        }
        response.getWriter().write("{\"status\":" + status +",\"info\":" + "\"" + info + "\"" + "}");
    }

    @RequestMapping(value = "register", method = {RequestMethod.POST})
    public String register(Login login) {
        loginService.save(login);

        User user = GlobalFunction.login2User(login);
        userService.save(user);

        return "redirect:/login";
    }

    /**
     * 发送短信验证
     */
    @RequestMapping(value = "smsVerification", method = {RequestMethod.POST})
    public void smsVerification(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        boolean status = true;
        String tel = request.getParameter("tel");
        // 生成6位验证码
        String verifyCode = (int)((Math.random()*9+1)*100000) + "";
        System.out.println(verifyCode);
        HttpSession session = request.getSession();
        session.setAttribute("code",verifyCode);
        session.setAttribute("tel",tel);

        String sessionId = session.getId();

        Cookie cookie = new Cookie("JSESSIONID", sessionId);
        cookie.setMaxAge(60 * 5);
        response.addCookie(cookie);

        try {
            SendSmsResponse res = GlobalFunction.sendSms(tel, verifyCode);
            if (!"OK".equals(res.getCode())) {
                status = false;
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }
        try {
            response.getWriter().write("{\"status\": " + status + "}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "codeCheck", method = {RequestMethod.POST})
    public void codeCheck(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        Boolean status = false;
        String info = null;
        try {
            String tel = request.getParameter("tel");
            String verityCode = request.getParameter("verityCode");

            HttpSession session = request.getSession();
            String sendTel = (String)session.getAttribute("tel");
            String sendCode = (String)session.getAttribute("code");

            if(StringUtils.isEmpty(tel)) {
                info = "手机号为空";
            } else {
                if(loginService.getByTel(tel) == null) {
                    info = "手机号未注册";
                } else {
                    if (StringUtils.isEmpty(sendTel) && !StringUtils.equals(tel, sendTel)) {
                        info = "验证码未发送";
                    } else {
                        if(StringUtils.equals(sendCode,verityCode)) {
                            info = "验证成功";
                            status = true;
                        } else {
                            info = "验证码错误";
                        }
                    }
                }
            }
            Message msg = new Message();
            msg.setStatus(status);
            msg.setInfo(info);
            String data = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat);
            response.getWriter().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "foundPassword", method = {RequestMethod.GET})
    public String resetPasswordUI() {
        return "foundPassword";
    }

    @RequestMapping(value = "foundPassword", method = {RequestMethod.POST})
    public void resetPassword(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        Boolean status = true;
        String info = null;
        try {
            String newPassword = request.getParameter("newPassword");
            String tel = request.getParameter("tel");

            Login login = loginService.getByTel(tel);
            if(login != null) {
                String encryptedPassword = Sha1Utils.entryptPassword(newPassword);
                login.setPassword(encryptedPassword);
                if (loginService.update(login) != 1) {
                    status = false;
                    info = "修改密码失败";
                }
            }
            // status：是否成功；info：成功返回null，失败返回错误原因
            Message msg = new Message();
            msg.setStatus(status);
            msg.setInfo(info);
            String data = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat);
            response.getWriter().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 省市二级联动
     */
    @RequestMapping(value = "getSecondArea", method = {RequestMethod.POST})
    public void getSecondArea(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        try {
            String areaId = request.getParameter("areaId");
            if (areaId != null) {
                List<Area> lists = areaService.listByPid(Integer.parseInt(areaId));
                String data = JSON.toJSONString(lists, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat);
                response.getWriter().write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 显示个人信息
     */
    @RequestMapping(value = "showSelfInfo", method = {RequestMethod.GET})
    public void showUserInfo(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        String id = getSelfId();
        try {
            User user = userService.getById(id);
            Area area = areaService.getById(user.getArea());
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            userDto.setAreaName(area.getName());

            List<Area> areas = areaService.listByPid(0);;

            List<Object> objects = new LinkedList<>();
            objects.add(userDto);
            objects.add(areas);
            String data = JSON.toJSONString(objects, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat);
            response.getWriter().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存个人信息
     */
    @RequestMapping(value = "saveSelfInfo", method = {RequestMethod.POST})
    public String saveUserInfo(HttpServletRequest request, HttpServletResponse response) {
        User user = new User();
        try {
            // 1.创建磁盘文件项工厂 sizeThreshold：每次缓存大小，单位为字节  File：临时文件路径
            DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024, new File(GlobalConstant.TEMP_PATH));

            // 2.创建文件上传核心类
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 设置上传文件的编码
            upload.setHeaderEncoding("UTF-8");

            // 3.判断是否为上传文件的表单
            if (ServletFileUpload.isMultipartContent(request)) {
                // 4.解析request获得文件项集合
                List<FileItem> fileItems = upload.parseRequest(request);
                if (fileItems.size() != 0) {
                    for (FileItem item : fileItems) {
                        // 5,判断是不是一个普通的表单项
                        if (item.isFormField()) {
                            String fieldName = item.getFieldName();
                            String fieldValue = item.getString("UTF-8");
                            parserUser(user, fieldName, fieldValue);
                        } else {
                            // 获取上传头像的文件名
                            String fileName = item.getName();
                            // 如果文件名为空，就跳过
                            if (StringUtils.isEmpty(fileName)) {
                                continue;
                            }
                            // 重命名：规定icon+后缀作为头像名
                            fileName = "icon." + fileName.split("\\.")[1];

                            // 拼装路径
                            String targetFilePath = GlobalConstant.USER_IMG_PATH + "/" + fileName;
                            // 上传文件
                            GlobalFunction.uploadFile(item, targetFilePath);

                            // 设置数据库中头像url
                            user.setIcon(fileName);
                        }
                    }
                }
            }
            userService.update(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole(GlobalConstant.ROLE.ADMIN.getName())) {
            return "redirect:/admin/index";
        } else if (subject.hasRole(GlobalConstant.ROLE.USER.getName())) {
            return "redirect:/user/index";
        } else {
            return "/login";
        }
    }
}
