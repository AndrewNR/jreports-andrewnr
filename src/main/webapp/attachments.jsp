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
        Name: <%= (String)attachment.getField("Name") %>,
        Description: <%= (String)attachment.getField("Description") %></li>
    <% } %>
    </ol>
<% } %>
    
<%@ include file="bottom.jsp" %>
