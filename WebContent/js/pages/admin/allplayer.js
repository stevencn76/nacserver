$(function() {
	initTable();
	reloadTable(1);
});

function initTable() {
	$('#allPlayerTable').datagrid({
		title: "All Player List",
		fit: true,
		iconCls:'icon-sum',
		singleSelect : true,
		loadMsg : 'Loading all player list, please wait......',
		nowrap: false,
		striped: true,
		url:'',
		sortName: null,
		sortOrder: null,
		remoteSort: false,
		idField:'id',
		pageSize : 50,
		showFooter:false,
		toolbar: [{
			id: 'deletebtn',
			iconCls: 'icon-remove',
			text: 'Delete',
			handler: function() {
				showDeleteNewsDlg();
			}
		}],
		columns : [ [ 
		  {field : 'id',title : 'ID', width : 80, sortable : false},
		  {field : 'name',title : 'Name', width : 250, sortable : false},
		  {field : 'ip',title : 'IP Address', width : 250, sortable : false},
		  {field : 'status',title : 'Status', width : 250, sortable : false},
		  {field : 'createTimeStr',title : 'Register Time', width : 250, sortable : false},
		  {field : 'lastTimeStr',title : 'Recent Access', width : 250, sortable : false}
		 ] ],
		fitColumns: false,
		pagination:true,
		rownumbers:true,
		onLoadSuccess : function(data) {
			$(this).datagrid('unselectAll');
		},
		onSelect : function(rowIndex, rowData) {
		}
	});
	//初始化分页信息
	var pageInfo = $('#allPlayerTable').datagrid('getPager');
	if (pageInfo){
		$(pageInfo).pagination({
			onBeforeRefresh:function(pageNumber, pageSize){
				reloadTable(pageNumber);
				return false;
			},
			onSelectPage : function(pageNumber, pageSize) {
				pageInfo.pagination('options').pageNumber = pageNumber;
				pageInfo.pagination('options').pageSize = pageSize;
				$('#allPlayerTable').datagrid('options').pageNumber = pageNumber;
				$('#allPlayerTable').datagrid('options').pageSize = pageSize;
				reloadTable(pageNumber);
			}
		});
	}
}

function reloadTable(pageNumber) {
	$('#allPlayerTable').datagrid('loading');
	
	var pageSize = $('#allPlayerTable').datagrid('options').pageSize;
	$('#allPlayerTable').datagrid('getPager').pagination('options').pageNumber = pageNumber;
	
	var paramData = JSON.stringify({
		'pageNumber' : pageNumber,
		'pageSize' : pageSize
	});
	
	$.ajax({
		url: '../rest/game/playerlist',
		type: 'POST',
		timeout: gAjaxTimeout,//超时时间设定
		dataType: "json",
		contentType: 'application/json;charset=UTF-8',
		beforeSend: function(x) {
            if (x && x.overrideMimeType) {
              x.overrideMimeType("application/j-son;charset=UTF-8");
            }
            //跨域使用
            x.setRequestHeader("Accept", "application/json");
        },
		data: paramData,//参数设置
		error: function(xhr, textStatus, thrownError){
			$('#allPlayerTable').datagrid('loaded');
			if(xhr.readyState != 0 && xhr.readyState != 1) {
				$.messager.alert('Error',"Fail to get player list, error no:  " + xhr.status + ", error info: " + textStatus,'error');
			}
			else {
				$.messager.alert('Error',"Fail to get player list, error info:  " + textStatus,'error');
			}
		},
		success: function(response, textStatus, xhr) {
			$('#allPlayerTable').datagrid('loaded');
			if(xhr.status == 200) {
				if(response.result == "ok") {
					if(response.data != null) {
						$('#allPlayerTable').datagrid('loadData', response.data);
					} else {
						$.messager.alert('Tip',response.result,'info');
					}
				}
				else {
					$.messager.alert('Error',"Fail to get player list, error info:  " + response.result,'error');
				}
			} else {
				$.messager.alert('Error',"Fail to get player list, error no:  " + xhr.status,'error');
			}
		}
	});
}

function clearTable(){
	$('#allPlayerTable').datagrid('unselectAll');
	$('#allPlayerTable').datagrid('loadData',{total:0,rows:[]});
}

