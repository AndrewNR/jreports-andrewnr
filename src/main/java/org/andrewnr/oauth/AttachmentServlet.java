package org.andrewnr.oauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.andrewnr.oauth.service.ConnectionManager;
import org.apache.commons.lang.StringUtils;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

@SuppressWarnings("serial")
public class AttachmentServlet extends HttpServlet {

    protected static final Logger log = Logger.getLogger(AccountServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String parentId = req.getParameter("parentId");
        try {
            SObject[] attachments = queryAttachments(parentId);
            req.setAttribute("attachments", attachments);
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/attachments.jsp");
            dispatcher.forward(req, resp);
        } catch (ServletException e) {
            log.severe("Servlet exception=" + e.toString());
        } catch (Exception e) {
            log.severe("Query exception=" + e.toString());
        }
    }

    private SObject[] queryAttachments(String parentId) throws ConnectionException {
        PartnerConnection connection = ConnectionManager.getConnectionManager().getConnection();
        Set<String> neededFields = new HashSet<String>( Arrays.asList(
        		"Id", "Name", "BodyLength", "Description"
        		, "Body"
        		, "ContentType", "LastModifiedDate", "LastModifiedById"
        		, "IsPrivate", "IsDeleted", "ParentId"
		) );
        List<String> availableFields = new ArrayList<String>(neededFields);
        log.info("availableFields: " + availableFields.toString());
        String availableFieldsQueryClause = StringUtils.join(availableFields, ", ");
        String soqlQuery = new StringBuilder()
                .append("select ")
            	.append(availableFieldsQueryClause).append(" ")
                .append("from Attachment ")
                .append("where id ='").append(parentId).append("' or ParentId = '").append(parentId).append("' ")
                .append("limit 500 ")
                .toString();
        log.info("SOQLQuery: " + soqlQuery);
        QueryResult results = connection.query(soqlQuery);
        SObject[] records = results.getRecords();
        return records;
    }

}
