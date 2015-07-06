package org.andrewnr.oauth;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.xml.bind.DatatypeConverter;

import net.sf.jasperreports.engine.util.JRStringUtil;

import org.andrewnr.oauth.service.ConnectionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
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
            
            if (pathInfo != null && pathInfo.endsWith("/runReport")) {
                runReport(req, resp);
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
            byte[] bodyBytes = queryAttachmentBodyBytes(attachmentObj);
            printAttachmentBodyToResponse(bodyBytes, resp);
        }
    }
    
    private void runReport(HttpServletRequest req, HttpServletResponse resp) throws ConnectionException, IOException {
        log.info("----> runReport() action");
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
            byte[] bodyBytes = queryAttachmentBodyBytes(attachmentObj);
            sendReportToDocGen(bodyBytes, req, resp);
        }
    }
    
    private void sendReportToDocGen(byte[] bodyBytes, HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException {
        log.info("----> sendReportToDocGen() start");
        if (bodyBytes != null && req != null) {
            OutputStream conOutput = null;
            HttpURLConnection con = null;
            try {
                String docGenProcessStreamUrl = new StringBuilder("https://").append(req.getServerName()).append("/docGen/processStream").toString();
                log.info("URL: " + docGenProcessStreamUrl);
                
                log.info("Building HttpURLConnection...");
                con = (HttpURLConnection) new URL(docGenProcessStreamUrl).openConnection();
                con.setRequestMethod("POST");
                con.setReadTimeout(30*1000);
                con.setDoOutput(true);
                con.setDoInput(true);
                con.connect();
                
                // send request
                log.info("Sending request to HttpURLConnection...");
                conOutput = con.getOutputStream();
                conOutput.write(bodyBytes);
                log.info("attachment bodyBytes written to URLConnection...");
                conOutput.flush();
                log.info("URLConnection outputStream flushed...");
                
                
                // get response
                log.info("Getting response from HttpURLConnection...");
                InputStream is = con.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                ServletOutputStream out = resp.getOutputStream();
                String line;
                while ((line = rd.readLine()) != null) {
                    out.println(line);
                }
                rd.close();
                out.flush();
            } finally {
                IOUtils.closeQuietly(conOutput);
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        log.info("----> sendReportToDocGen() complete");
    }

    private void printAttachmentBodyToResponse(byte[] bodyBytes, HttpServletResponse resp) throws IOException {
        if (resp != null) {
            if (bodyBytes == null) {
                throw new RuntimeException("No Attachment Body found to display");
            }
            
            log.info("Attachment.Body.length: " + bodyBytes.length);
            
            resp.setContentType("text/html");
            ServletOutputStream out = resp.getOutputStream();
            
            InputStream is = null;
            InputStreamReader reader = null;
            try {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Attachment Content preview</title>");
                out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../stylesheet.css\" title=\"Style\">");
                out.println("</head>");
                out.println("<body bgcolor=\"white\">");
                out.println("<span class=\"bold\">Attachment content preview</span>");
                is = new ByteArrayInputStream(bodyBytes);
                reader = new InputStreamReader(is);
                out.println("<pre id='content'>");
                int ln = 0;
                char[] chars = new char[1024];
                while((ln = reader.read(chars)) > 0) {
                    out.print(JRStringUtil.xmlEncode(new String(chars, 0, ln)));
                }
                out.println("</pre>");
            }
            finally {
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(is);
                out.println("</body>");
                out.println("</html>");
                out.flush();
            }
        }
    }

    private static byte[] queryAttachmentBodyBytes(SObject attachmentObj) throws ConnectionException {
        log.info("----> queryAttachmentBodyBytes() start");
        byte[] bodyData = null;
        log.info("attachmentObj != null ? = " + (attachmentObj != null));
        if (attachmentObj != null) {
            String attachmentId = attachmentObj.getId();
            PartnerConnection connection = ConnectionManager.getConnectionManager().getConnection();
            SObject[] attachments = connection.retrieve("Id, Name, Body", "Attachment", new String[] { attachmentId });
            log.info("queriedAttachments != null ? = " + (attachments != null));
            if (attachments != null && attachments.length > 0) {
                log.info("# of attachments found for attachmentId='" + attachmentId + "': " + attachments.length);
                bodyData = extractBodyBytes((String) attachments[0].getField("Body"));
            }
        }
        log.info("----> queryAttachmentBodyBytes() end");
        return bodyData;
    }

    private static byte[] extractBodyBytes(String bodyDataBase64) {
        log.info("attachmentBody(base64 encoded): " + bodyDataBase64);
        return (bodyDataBase64 != null) ? DatatypeConverter.parseBase64Binary(bodyDataBase64) : null;
    }

    private static SObject findAttachmentById(SObject[] attachments, String attachmentId) {
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

    private static SObject[] queryAttachments(String parentId) throws ConnectionException {
        PartnerConnection connection = ConnectionManager.getConnectionManager().getConnection();
        
        /*
         * Avoid querying Body field: you can't receive the Body field for multiple records in a single query() call.
         * If your query returns the Body field, your client application must ensure
         * that only one row with one Attachment is returned; otherwise, an error occurs.
         * A more effective approach is to return IDs (but not Attachment records in the Body field)
         * from a query() call and then pass them into retrieve() calls that return the Body field.
        */
        Set<String> neededFields = new HashSet<String>(Arrays.asList("Id", "Name", "BodyLength", "Description",
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

    private static void doCreateAttachment() {
        String parentId = "";
        String name = "";
        String contentType = "jasper";
        byte[] bodyBytes = new byte[0];
        
        SObject myNewAttachment = buildAttachment(parentId, name, contentType, bodyBytes);
        insertAttachment(myNewAttachment);

    }

    private static Boolean insertAttachment(SObject attachment) {
        Boolean result = false;
        if (attachment != null) {
            try {
                PartnerConnection connection = ConnectionManager.getConnectionManager().getConnection();
                SaveResult[] saveResults = connection.create(new SObject[] { attachment });
                result = true;
                for (SaveResult saveResult : saveResults) {
                    if (!saveResult.isSuccess()) {
                        result = false;
                    }
                }
            } catch (ConnectionException e) {
                log.severe("Error while inserting attachment: " + e.getMessage());
            }
        }
        return result;
    }

    private static SObject buildAttachment(String parentId, String name, String contentType, byte[] bodyBytes) {
        // TODO Auto-generated method stub
        return null;
    }
}
