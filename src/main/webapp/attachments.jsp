<%@ page import="com.sforce.soap.partner.sobject.SObject"%>

<%  
  SObject[] attachments = (SObject[])request.getAttribute("attachments");
%>

<%@ include file="top.jsp" %>

<p><a href="/home">Home</a></p>

<h3>Attachments from Salesforce.com, found for parentId = '<%=request.getParameter("parentId")%>'</h3>

<% if (attachments.length > 0) { %>
    <ol>
    <% for (SObject attachment : attachments) { %>
        <li>Id: <%=(String)attachment.getField("Id")%>,
        Name: <%= (String)attachment.getField("Name") %>.
        <a href="/attachments/viewContent?id=<%=(String)attachment.getField("Id")%>" target="_blank">view content</a></li>
    <% } %>
    </ol>
<% } %>
    
<%@ include file="bottom.jsp" %>
