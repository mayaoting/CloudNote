<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/global/taglib.jsp" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <title>CloudNote</title>
    <!-- Required meta tags always come first -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="${ctx}/css/bootstrap.css">
    <!-- jQuery first, then Bootstrap JS. -->
    <script src="${ctx}/js/jquery-3.2.1.min.js"></script>
    <script src="${ctx}/js/bootstrap.js"></script>
    <style>
        *{
            margin: 0;
            padding: 0;
        }
        body{
            background: #fbfcfe;
        }
        #main{
            width:400px;
            height:390px;
            box-shadow:0 0 5px #46B4E6;
            margin:0 auto;
            margin-top: 100px;
            position: relative;
        }
        header{
            width: 400px;
            height:45px;
            background: #46b4e6;
        }
        .inputPhone{
            margin-top: 30px;
        }

        #main .inputPhone .name{
            width: 370px;
            margin-left: 15px;
            margin-top: 20px;
        }

        .send{
            width:100px;
            height:35px;
            border-radius: 5px;
            background: #46b4e6;
            color: white;
            margin-right: 15px;
            float: right;
            margin-top: 15px;
        }
        .changePasswd p{
            font-size: 15px;
            line-height: 15px;
            text-align: center;
            color: #949494;
        }
        #main .changePasswd .changmm{
            text-align: left;
            margin-left: 15px;
            font-size: 20px;
            color: #949494;
        }
        #main .changePasswd .mima{
            width:300px;
            float: left;
            margin-left: 15px;

        }
        #main .changePasswd .r_mima{
            width:300px;
            float: left;
            margin-left: 15px;

        }
        #reset{
            color: white;
            font-size: 20px;
            background: #46B4E6;
            margin-left: 130px;
            margin-top: 30px;
            width:150px;
        }
    </style>
</head>

<body>
<h1>首页</h1>

<a href="${ctx}/login">登陆</a>
<a href="${ctx}/register">注册</a>

<div id="main">
    <header >
        <p style="font-size: 20px;line-height: 45px;text-align: center;color: white;">找回密码</p>
    </header>
    <div class="inputPhone" >
        <input id="phonenum"type="text" class="form-control name"
               placeholder="请输入手机号获取验证码">
        <input type="button" class="btn send" style="margin-bottom: 15px" id="second" value="发送验证码"></input>

        <input id="yanzhengma" name="code" style="margin-top: 35px" type="text" class="form-control name"
               placeholder="请输入验证码">
        <a class="btn send" onclick="changePassword()">确定</a>
    </div>

    <div class="changePasswd" style="display: none">
        <p style="margin-top: 20px;">您正在通过手机验证找回密码，</p>
        <p style="margin-top: 10px">请输入新密码并重复确认，以完成密码重置操作</p>
        <hr>
        <p class="changmm" >新密码：</p>
        <input id="newmima" style="margin-top: 5px" type="text" class="form-control mima"
               placeholder="请输入新密码">
        <p class="changmm" style="margin-top: 60px">确认密码：</p>
        <input id="resetmima" style="margin-top: 5px" type="text" class="form-control r_mima"
               placeholder="确认新密码">
        <a class="btn" id="reset" onclick="reset()">重置密码</a>

    </div>
</div>

<jsp:include page="${ctx}/WEB-INF/jsp/global/footer.jsp"/>
<script>
    //发送验证码时添加cookie
    function addCookie(name,value,expiresHours){
        //判断是否设置过期时间,0代表关闭浏览器时失效
        if(expiresHours>0){
            var date=new Date();
            date.setTime(date.getTime()+expiresHours*1000);
            $.cookie(name, escape(value), {expires: date});
        }else{
            $.cookie(name, escape(value));

        }
    }
    //修改cookie的值
    function editCookie(name,value,expiresHours){
        if(expiresHours>0){
            var date=new Date();
            date.setTime(date.getTime()+expiresHours*1000); //单位是毫秒
            $.cookie(name, escape(value), {expires: date});
        } else{
            $.cookie(name, escape(value));

        }

    }
    //根据名字获取cookie的值
    function getCookieValue(name){
        return $.cookie(name);

    }
    $(function(){
        $.cookie("secondsremained",null);
        $("#second").click(function (){
            sendCode($("#second"));
        });
        v = getCookieValue("secondsremained");//获取cookie值
        if(v>0){
            settime($("#second"));//开始倒计时
        }
    })
    //发送验证码
    function sendCode(obj){
        var phonenum = $("#phonenum").val();
        var result = isPhoneNum();
        if(result){
// doPostBack('${base}/login/getCode.htm',backFunc1,{"phonenum":phonenum});
            addCookie("secondsremained",60,60);//添加cookie记录,有效时间60s
            settime(obj);//开始倒计时
        }
    }
    //开始倒计时
    var countdown;
    function settime(obj) {
        countdown=getCookieValue("secondsremained");
        if (countdown == 0) {
            obj.removeAttr("disabled");
            obj.val("免费获取验证码");
            return;
        } else {
            obj.attr("disabled", true);
            obj.val("重新发送(" + countdown + ")");
            countdown--;
            editCookie("secondsremained",countdown,countdown+1);
        }
        setTimeout(function() { settime(obj) },1000) //每1000毫秒执行一次
    }
    //校验手机号是否合法
    function isPhoneNum(){
        var phonenum = $("#phonenum").val();
        var myreg = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/;
        if(!myreg.test(phonenum)){
            alert('请输入有效的手机号码！');
            return false;
        }else{
            return true;
        }
    }


    function changePassword() {
        var code = $("#yanzhengma").val();
        $.ajax({
            type:'post',
            url: '/findPassword',
            async:false,
            dataType:'json',
            data: {
                'code':code
            },
            success:function (msg) {
                if (!msg.res){
                    alert("验证码错误！");
                }
                else{
                    $('.inputPhone').hide();
                    $('.changePasswd').show();
                }
            },
            error:function (error) {
                alert("出错啦！");
            }
        });
    }
    function reset(){
        var newmima= $('#newmima').val();
        var resetmima = $('#resetmima').val();
        if (newmima != retmm){
            alert("两次密码不一致！");
            return false;
        }
        $.ajax({
            type:'post',
            url: '/findPassword',
            async:false,
            dataType:'json',
            data:{
                'resetmima':resetmima,
            },
            success:function (msg) {
                if(!msg.res){
                    return false;
                }
                return true;
            },
            error:function (error) {
                return false;
            }
        });

    }
</script>
</body>

</html>