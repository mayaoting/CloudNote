<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/global/taglib.jsp" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <title>无道云笔记</title>
    <!-- Required meta tags always come first -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="${ctx}/css/bootstrap.css">
    <link rel="stylesheet" href="${ctx}/css/wangEditor-fullscreen-plugin.css">
    <!-- 弹窗CSS -->
    <link rel="stylesheet" href="${ctx}/css/toastr.css">
    <!-- 自定义CSS -->
    <link rel="stylesheet" href="${ctx}/css/custom.css">
    <!-- home页CSS -->
    <link rel="stylesheet" href="${ctx}/css/home_css.css">
    <!-- jQuery first, then Bootstrap JS. -->
    <script src="${ctx}/js/jquery-3.2.1.min.js"></script>
    <script src="${ctx}/js/bootstrap.js"></script>
    <!-- jQuery百叶窗 -->
    <script src="${ctx}/js/jquery.contextify.js"></script>
    <!-- wangEditor依赖 -->
    <script src="${ctx}/js/wangEditor.js"></script>
    <script src="${ctx}/js/wangEditor-fullscreen-plugin.js"></script>
    <!-- 弹窗依赖 -->
    <script src="${ctx}/js/toastr.js"></script>
    <!-- 封装ajax -->
    <script src="${ctx}/js/http.js"></script>
    <!-- home页JS -->
    <script src="${ctx}/js/home_js.js"></script>


</head>

<body style="margin-top: 60px;position: absolute;width: 100%" id="home_body">

<!-- 引入模块框 -->
<jsp:include page="../showSelfInfoModel.jsp"/>
<jsp:include page="shareNoteModel.jsp"/>
<jsp:include page="importNote.jsp"/>

<input type="hidden" id="lastLoginTime" value="${lastLoginTime}">

<!-- 头部 -->
<jsp:include page="head.jsp"/>

<!-- 分享区域 -->
<jsp:include page="share.jsp"/>

<!--主体-->
<div  class="container" style="padding-right: 0px; width:100%" id="left">
    <div class="row">
        <div id="wangeditor" class="col-lg-10">
            <!-- 目录区域 -->
            <jsp:include page="directory.jsp"/>
            <!-- 编辑器区域 -->
            <jsp:include page="articleEditor.jsp"/>
        </div>

        <!-- 广告位 -->
        <div class="col-lg-2" id="advertisment" style="background: yellow;height: 700px;">
        </div>
    </div>
</div>

<!-- 引入页脚 -->
<jsp:include page="${ctx}/WEB-INF/jsp/global/footer.jsp"/>

<script>
    var userTel;
    // 页面加载函数
    $(function(){
        // 得到当前用户手机号码
        userTel = $.trim($("#showId").text());
        // 打印上次登陆事件
        var lastTime = $("#lastLoginTime").val();
        if(lastTime != null && lastTime != "") {
            toastr.info(lastTime);
            $("#text1").val(null);
        }
    });
</script>
</body>
</html>