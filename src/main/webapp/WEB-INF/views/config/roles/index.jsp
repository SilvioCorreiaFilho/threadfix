<%@ include file="/common/taglibs.jsp"%>

<head>
	<title>Manage Roles</title>
	<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/ajax_replace.js"></script>
</head>

<body>
	<h2>Manage Roles</h2>

	<c:if test="${ not empty error }">
		<span class="errors"><c:out value="${ error }" /></span>
	</c:if>

	<div id="helpText">
		ThreadFix Roles determine functional capabilities for associated users.<br/>
	</div>
	
	<div id="tableDiv">
		<%@ include file="/WEB-INF/views/config/roles/rolesTable.jsp" %>
	</div>
	
	<br/>
	<a id="createRoleModalLink" href="#createRoleModal" role="button" class="btn" data-toggle="modal">Create New Role</a>
	
	<div id="createRoleModal" class="modal hide fade" tabindex="-1"
			role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
		<%@ include file="/WEB-INF/views/config/roles/newForm.jsp" %>
	</div>
	
</body>
