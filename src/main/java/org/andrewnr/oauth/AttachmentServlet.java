package org.andrewnr.oauth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.andrewnr.oauth.AccountServlet;
import org.andrewnr.oauth.service.ConnectionManager;

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
        String soqlQuery = new StringBuilder()
                .append("select a.Id, a.Name, a.Description, a.ContentType, a.BodyLength, a.Body,")
                .append("a.SystemModstamp, a.ParentId, a.OwnerId,")
                .append("a.LastModifiedDate, a.LastModifiedById,")
                .append("a.CreatedDate, a.CreatedById,")
                .append("a.IsPrivate, a.IsDeleted")
                .append("from Attachment a ")
                .append("where a.id ='").append(parentId).append("' or a.ParentId = '").append(parentId).append("'")
                .append("limit 500")
                .toString();
        QueryResult results = connection.query(soqlQuery);
        SObject[] records = results.getRecords();
        return records;
    }
}
