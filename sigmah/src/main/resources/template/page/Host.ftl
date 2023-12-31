<#-- @ftlvariable name="" type="org.sigmah.server.bootstrap.model.HostPageModel" -->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html manifest="ActivityInfo/html5.manifest">
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta name="application-name" content="ActivityInfo"/>
    <meta name="description" content="ActivityInfo"/>
    <meta name="application-url" content="${appUrl}"/>
    <link rel="icon" href="ActivityInfo/desktopicons/16x16.png" sizes="16x16"/>
    <link rel="icon" href="ActivityInfo/desktopicons/32x32.png" sizes="32x32"/>
    <link rel="icon" href="ActivityInfo/desktopicons/48x48.png" sizes="48x48"/>
    <link rel="icon" href="ActivityInfo/desktopicons/64x64.png" sizes="64x64"/>
    <link rel="icon" href="ActivityInfo/desktopicons/128x128.png" sizes="128x128"/>

    <title>ActivityInfo</title>
    <style type="text/css">
        #loading-box {
            position: absolute;
            left: 45%;
            top: 40%;
            padding: 2px;
            margin-left: -45px;
            z-index: 20001;
            border: 1px solid #ccc;
        }

        #loading-box .loading-indicator {
            background: #eef;
            font: bold 13px tahoma, arial, helvetica;
            padding: 10px;
            margin: 0;
            height: auto;
            color: #444;
        }

        #loading-box .loading-indicator img {
            margin-right: 8px;
            float: left;
            vertical-align: top;
        }

        #loading-msg {
            font: normal 10px tahoma, arial, sans-serif;
        }

        #loading-options {
            position: absolute;
            right: 10px;
            bottom: 10px;
            font: normal 13px tahoma, arial, sans-serif;
            text-align: right;
        }
        <#include "Application.css">
    </style>
    <script type="text/javascript">
        var UserInfo = {
            userId: ${auth.user.id},
            authToken: "${auth.id}",
            email: "${auth.user.email}"
        };
        var GoogleMapsAPI = {
            key: "ABQIAAAAHxDe2DM8fTqR3KT6JA3uSxQowpcadhvcZvF-AZHbBConS0qRQRSyDoOLQrw76pJeJNUXt2g-yC8FAg"
        };
        var VersionInfo = {
            revision: ${activityinfo.revision}
        };
        <#if offline == true>
        function disableOffline() {
            document.cookie = "offline=false;expires=Thu, 01-Jan-1970 00:00:01 GMT";
            window.location.reload();
        }
        </#if>
    </script>

    <script language='javascript' src='ActivityInfo/gxt/flash/swfobject.js'></script>
    <script type="text/javascript" language="javascript" src="ActivityInfo/ActivityInfo.nocache.js"></script>
    <link rel="stylesheet" type="text/css" href="ActivityInfo/gxt/css/gxt-all.css"/>
    <link rel="stylesheet" type="text/css" href="report.css"/>

</head>
<body>
<div id="loading">
    <div id="loading-box">
        <div class="loading-indicator">
            <img src="ActivityInfo/gxt/images/default/shared/large-loading.gif" alt=""/>
            ActivityInfo r${activityinfo.revision}<br/>
            <span id="loading-msg">Chargement en cours...</span>

        </div>
    </div>
<#if offline == true>
    <div id="loading-options">
        <a href="javascript:disableOffline()">Charger avec mode hors connexion desactiver</a>
    </div>
</#if>
</div>
<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1'
        style="position:absolute;width:0;height:0;border:0"></iframe>
<iframe src="javascript:''" id="_downloadFrame" name="_downloadFrame" tabIndex='-1'
        style="position:absolute;width:0;height:0;border:0"></iframe>
<script type="text/javascript">
    var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
    document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
    try {
        var pageTracker = _gat._getTracker("UA-11567120-1");
        pageTracker._trackPageview("/login/success");
    } catch(err) {
    }</script>
</body>
</html>