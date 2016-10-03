$(function(){
	$('#username').focus();
	$('#username').click();
});

function login() {
	var username = $('#username').val();
	var password = $('#password').val();
	
	if(username == "") {
		alert("Please input username");
		return;
	}
	
	if(password == "") {
		alert("Please input password");
		return;
	}
	
	$.ajax({
		url: 'rest/system/login',
		type: 'POST',
		timeout: gAjaxTimeout,//
		data: ({
			'username': username,
			'password': password
		}),//
		error: function(xhr, textStatus, thrownError){
			if(xhr.readyState != 0 && xhr.readyState != 1) {
	     		alert("Login failed, error no:  " + xhr.status + ", error info: " + textStatus);
			}
			else {
			 	alert("Login failed, error info: " + textStatus);
			}
		},
		success: function(response, textStatus, xhr) {
			if(xhr.status == 200) {
				if(response.result == "ok") {
					location = "admin/index.jsp";
				}
				else {
					alert(response.result);
				}
			} else {
				alert("Login failed, error no:  " + xhr.status);
			}
		}
	});
	
	return false;
}