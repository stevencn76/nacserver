<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Noughts &mp; Crosses Server Admin</title>
<link rel="stylesheet" type="text/css" href="plugins/easyui/themes/bootstrap/easyui.css">
<link rel="stylesheet" type="text/css" href="plugins/easyui/themes/icon.css">
<link rel="stylesheet" type="text/css" href="css/my.css">
<script type="text/javascript" src="plugins/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="plugins/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript" src="js/global.js"></script>
<script type="text/javascript" src="js/pages/adminlogin.js"></script>
</head>
<body>
<div style="margin-top: 100px;"></div>
<form name="nac_admin_login_form" onsubmit="return login();">
<table align="center" border="0" cellspacing="5" cellpadding="5">
	<tr>
		<td colspan="2"><h2>Noughts & Crosses Server Login</h2></td>
	</tr>
	<tr>
		<td>Username</td>
		<td><input id="username" class="easyui-validatebox" style="width: 200px;" type="text" value=""
			data-options="required:true"></input></td>
	</tr>
	<tr>
		<td>Password</td>
		<td><input id="password" class="easyui-validatebox" style="width: 200px;" type="password" value=""
			data-options="required:true" onkeypress="if(event.keyCode==13) {return login();}"></input></td>
	</tr>
	<tr>
		<td></td>
		<td><a id="submitbtn" href="#" onClick="login()" class="easyui-linkbutton" data-options="iconCls:'icon-ok'">Login</a></td>
	</tr>
</table>
</form>
</body>
</html>