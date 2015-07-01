package org.andrewnr.oauth;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            if (pathInfo == null) {
                listAttachments(req, resp);
            } else if (pathInfo.endsWith("/showContent")) {
                showContent(req, resp);
            }
        } catch (ServletException e) {
            log.severe("Servlet exception=" + e.toString());
        } catch (Exception e) {
            log.severe("Query exception=" + e.toString());
        }
    }

    private void listAttachments(HttpServletRequest req, HttpServletResponse resp) throws ConnectionException,
            ServletException, IOException {
        String parentId = req.getParameter("parentId");
        if (parentId == null) {
            throw new RuntimeException("No request parameter found: parentId");
        }
        SObject[] attachments = queryAttachments(parentId);
        req.setAttribute(ATTR_KEY_ATTACHMENTS, attachments);
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/attachments.jsp");
        dispatcher.forward(req, resp);
    }

    private void showContent(HttpServletRequest req, HttpServletResponse resp) throws ConnectionException, IOException {
        String attachmentId = req.getParameter("id");
        if (attachmentId != null && !attachmentId.isEmpty()) {
            SObject[] attachments = (SObject[]) req.getAttribute(ATTR_KEY_ATTACHMENTS);
            if (attachments == null) {
                attachments = queryAttachments(attachmentId);
            }
            SObject attachmentObj = findAttachmentById(attachments, attachmentId);
            printAttachmentBodyToResponse(attachmentObj, resp);
        }

    }

    private void printAttachmentBodyToResponse(SObject attachmentObj, HttpServletResponse resp) throws IOException {
        if (resp != null) {
            if (attachmentObj != null) {
                ServletOutputStream out = resp.getOutputStream();
                
                byte[] bodyData = (byte[]) attachmentObj.getField("Body");
                InputStream is = new ByteArrayInputStream(bodyData);
                InputStreamReader reader = new InputStreamReader(is);
                try {
                    int ln = 0;
                    char[] chars = new char[1024];
                    while((ln = reader.read(chars)) > 0) {
                        out.print(JRStringUtil.xmlEncode(new String(chars, 0, ln)));
                    }
                } finally {
                    reader.close();
                    is.close();
                    out.flush();
                }
            } else {
                throw new RuntimeException("No Attachment found to display");
            }
        }
    }

    private SObject findAttachmentById(SObject[] attachments, String attachmentId) {
        SObject result = null;
        if (attachments != null) {
            for (SObject attachment : attachments) {
                if (attachment.getId() == attachmentId) {
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
