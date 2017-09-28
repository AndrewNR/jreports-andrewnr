<%@ include file="top.jsp" %>
<%
String prevURI = (String) session.getAttribute("prevURI");
%>

<h2>Not logged in</h2>

<p>You are either not logged in, or the login expired.<br/>
Please <a href="<%=prevURI%>">try again</a>, or return to <a href="/home">home page</a>.</p>

<%@ include file="bottom.jsp" %>