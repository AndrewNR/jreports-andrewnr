package org.andrewnr.oauth;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.util.JRStringUtil;

import org.andrewnr.oauth.service.ConnectionManager;
import org.apache.commons.lang.StringUtils;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

@SuppressWarnings("serial")
public class AttachmentServlet extends HttpServlet {

    private static final String ATTR_KEY_ATTACHMENTS = "attachments";
    protected static final Logger log = Logger.getLogger(AccountServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        log.info("pathInfo: " + pathInfo);
        try {
            if (pathInfo != null && pathInfo.endsWith("/viewContent")) {
                viewContent(req, resp);
                return;
            }
            
            //default action
            listAttachments(req, resp);
        } catch (ServletException e) {
            log.severe("Servlet exception=" + e.toString());
        } catch (Exception e) {
            log.severe("Query exception=" + e.toString());
        }
    }

    private void listAttachments(HttpServletRequest req, HttpServletResponse resp) throws ConnectionException,
            ServletException, IOException {
        log.info("----> listAttachments() action");
        String parentId = req.getParameter("parentId");
        log.info("parentId: " + parentId);
        if (parentId == null) {
            throw new RuntimeException("No request parameter found: parentId");
        }
        SObject[] attachments = queryAttachments(parentId);
        req.setAttribute(ATTR_KEY_ATTACHMENTS, attachments);
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/attachments.jsp");
        dispatcher.forward(req, resp);
    }

    private void viewContent(HttpServletRequest req, HttpServletResponse resp) throws ConnectionException, IOException {
        log.info("----> viewContent() action");
        String attachmentId = req.getParameter("id");
        log.info("attachmentId: " + attachmentId);
        if (attachmentId != null && !attachmentId.isEmpty()) {
            SObject[] attachments = (SObject[]) req.getAttribute(ATTR_KEY_ATTACHMENTS);
            if (attachments == null) {
                log.info("No attachments found in attribute, queryAttachments...");
                attachments = queryAttachments(attachmentId);
                req.setAttribute(ATTR_KEY_ATTACHMENTS, attachments);
            }
            log.info(attachments != null ? "attachments.length: " + attachments.length : "attachments is null");
            SObject attachmentObj = findAttachmentById(attachments, attachmentId);
            log.info("attachmentObj != null ? " + (attachmentObj != null));
            printAttachmentBodyToResponse(attachmentObj, resp);
        }

    }

    private void printAttachmentBodyToResponse(SObject attachmentObj, HttpServletResponse resp) throws IOException {
        if (resp != null) {
            if (attachmentObj == null) {
                throw new RuntimeException("No Attachment found to display");
            }
            
            ServletOutputStream out = resp.getOutputStream();
            
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Attachment Content preview</title>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../stylesheet.css\" title=\"Style\">");
            out.println("</head>");
            
            out.println("<body bgcolor=\"white\">");

            out.println("<span class=\"bold\">Attachment content preview for Id: " + attachmentObj.getId() + "</span>");

            String bodyData = (String) attachmentObj.getField("Body");
            log.info(bodyData != null ? "Attachment.Body.length: " + bodyData.length() : "Attachment.Body is null");
            InputStream is = new ByteArrayInputStream(bodyData.getBytes(StandardCharsets.UTF_8));
            InputStreamReader reader = new InputStreamReader(is);
            try {
                out.println("<pre id='content'>");
                int ln = 0;
                char[] chars = new char[1024];
                while((ln = reader.read(chars)) > 0) {
                    out.print(JRStringUtil.xmlEncode(new String(chars, 0, ln)));
                }
                out.println("</pre>");
            }
            finally {
                reader.close();
                is.close();
                
                out.println("</body>");
                out.println("</html>");
                out.flush();
            }
        }
    }

    private SObject findAttachmentById(SObject[] attachments, String attachmentId) {
        SObject result = null;
        if (attachments != null && attachmentId != null) {
            for (SObject attachment : attachments) {
                String id = attachment.getId();
                if (id != null && (id.equalsIgnoreCase(attachmentId) || id.startsWith(attachmentId))) {
                    result = attachment;
                    break;
                }
            }
        }
        return result;
    }

    private SObject[] queryAttachments(String parentId) throws ConnectionException {
        PartnerConnection connection = ConnectionManager.getConnectionManager().getConnection();
        Set<String> neededFields = new HashSet<String>(Arrays.asList("Id", "Name", "BodyLength", "Description", "Body",
                "ContentType", "LastModifiedDate", "LastModifiedById", "IsPrivate", "IsDeleted", "ParentId"));
        List<String> availableFields = new ArrayList<String>(neededFields);
        log.info("availableFields: " + availableFields.toString());
        String availableFieldsQueryClause = StringUtils.join(availableFields, ", ");
        String soqlQuery = new StringBuilder().append("select ").append(availableFieldsQueryClause).append(" ")
                .append("from Attachment ").append("where id ='").append(parentId).append("' or ParentId = '")
                .append(parentId).append("' ").append("limit 500 ").toString();
        log.info("SOQLQuery: " + soqlQuery);
        QueryResult results = connection.query(soqlQuery);
        SObject[] records = results.getRecords();
        return records;
    }

}
