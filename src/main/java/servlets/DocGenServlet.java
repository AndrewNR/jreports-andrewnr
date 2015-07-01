package servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.andrewnr.oauth.AccountServlet;
import org.apache.poi.util.IOUtils;

import datasource.WebappDataSource;
import net.sf.jasperreports.engine.JRConstants;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;

public class DocGenServlet extends HttpServlet {

    private static final long serialVersionUID = JRConstants.SERIAL_VERSION_UID;
    
    protected static final Logger log = Logger.getLogger(AccountServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO: write generated report file byte[] array to response outputStream.
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>JasperReports - DocGen Servlet</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../stylesheet.css\" title=\"Style\">");
        out.println("</head>");
        
        out.println("<body bgcolor=\"white\">");

        out.println("<span class=\"bold\">STUB: this servlet will generate resulting file from JasperReport using SFDC data.</span>");

        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            log.info("pathInfo: " + pathInfo);
            if (pathInfo != null && pathInfo.endsWith("/processStream")) {
                processStream(req, resp);
            }
        } catch (Exception e) {
            log.severe("Exception while processing: " + e.getMessage());
        }
    }
    
    private void processStream(HttpServletRequest req, HttpServletResponse resp) throws IOException, JRException {
        resp.setContentType("text/html");
        ServletOutputStream out = resp.getOutputStream();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>DocGen Servlet - processStream action</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../stylesheet.css\" title=\"Style\">");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        
        InputStream jrxmlInputStream = null;
        try {
            jrxmlInputStream = new BufferedInputStream(req.getInputStream());
            JasperReport compiledReport = JasperCompileManager.compileReport(jrxmlInputStream);
            JasperPrint fillReport = JasperFillManager.fillReport(compiledReport, getReportParams(), getReportDataSource());
            JasperExportManager.exportReportToPdfStream(fillReport, resp.getOutputStream());
        } finally {
            IOUtils.closeQuietly(jrxmlInputStream);
        }
        
        out.println("</body>");
        out.println("</html>");
        out.flush();
    }

    private void exportReportToHtml(OutputStream filledReportOutputStream, HttpServletRequest req, HttpServletResponse resp) throws IOException, JRException {
        HtmlExporter exporter = new HtmlExporter();
        req.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, filledReportOutputStream);
        exporter.setExporterInput(new SimpleExporterInput( outputToInputStream(filledReportOutputStream) ));
        SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(resp.getOutputStream());
        output.setImageHandler(new WebHtmlResourceHandler("image?image={0}"));
        exporter.setExporterOutput(output);
        exporter.exportReport();
    }

    private OutputStream compileReport(InputStream reportInputStream) throws IOException, JRException {
        // #1 - compile report (jrxml -> jasper)
        JasperDesign jasperDesign = JRXmlLoader.load(reportInputStream);
        JasperReport compileReport = JasperCompileManager.compileReport(jasperDesign);
        OutputStream compiledReportOutputStream = compileReportToStream(reportInputStream);
        log.info("Report compiled to OutputStream successfully");
        return compiledReportOutputStream;
    }

    private OutputStream fillReport(OutputStream compiledReportOutputStream) throws JRException {
     // #2 - fill report (JasperReport stream -> JasperPrint object)
        Map<String, Object> parameters = getReportParams();
        JRDataSource dataSource = getReportDataSource();
        OutputStream filledReportOutputStream = fillReportToStream(compiledReportOutputStream, parameters, dataSource);
        log.info("Report filled to OutputStream successfully");
        return filledReportOutputStream;
    }

    public static void checks() {
        try {
            // #1 - compile report (jrxml -> jasper)
            OutputStream compiledReportOutputStream = compileReportToStream(createReportSourceInputStream());
            
            // #2 - fill report (JasperReport stream -> JasperPrint object)
            Map<String, Object> parameters = getReportParams();
            JRDataSource dataSource = getReportDataSource();
            OutputStream filledReportOutputStream = fillReportToStream(compiledReportOutputStream, parameters, dataSource);
            
            // #3 - export report as document (JasperPrint -> byte[] selectedFormatReportFile)
            Map<String, Object> exportParams = getExportParams();
            OutputStream reportFileOutputStream = exportReportToStream(filledReportOutputStream, exportParams);
            
            //// TODO: IMPORTANT: close all needed streams correctly!
            
            
            
            processReportFileOutputStream(reportFileOutputStream);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    private static void processReportFileOutputStream(OutputStream reportFileOutputStream) {
        // TODO Auto-generated method stub
        // TODO: respond to client with report file
    }

    private static OutputStream exportReportToStream(OutputStream filledReportOutputStream,
            Map<String, Object> exportParams) throws JRException {
        InputStream fileldReportInputStream = outputToInputStream(filledReportOutputStream);
        OutputStream reportFileOutputStream = new BufferedOutputStream(new ByteArrayOutputStream());
        JasperExportManager.exportReportToPdfStream(fileldReportInputStream, reportFileOutputStream);
        return reportFileOutputStream;
    }

    private static Map<String, Object> getExportParams() {
        // TODO Auto-generated method stub
        return null;
    }

    private static OutputStream fillReportToStream(OutputStream compiledReportOutputStream,
            Map<String, Object> parameters, JRDataSource dataSource) throws JRException {
        InputStream compiledReportInputStream = outputToInputStream(compiledReportOutputStream);
        OutputStream filledReportOutputStream = new BufferedOutputStream(new ByteArrayOutputStream(256));
        JasperFillManager.fillReportToStream(compiledReportInputStream, filledReportOutputStream, parameters, dataSource);
        return filledReportOutputStream;
    }

    private static InputStream outputToInputStream(OutputStream compiledReportOutputStream) {
        // TODO Auto-generated method stub
        return null;
    }

    private static OutputStream compileReportToStream(InputStream reportSourceInputStream) throws JRException {
        OutputStream compiledReportStream = null;
        if (reportSourceInputStream != null) {
            compiledReportStream = new BufferedOutputStream(new ByteArrayOutputStream());
            JasperCompileManager.compileReportToStream(reportSourceInputStream, compiledReportStream);
        }
        return compiledReportStream;
    }

    private static JRDataSource getReportDataSource() {
        return new WebappDataSource();
    }

    private static Map<String, Object> getReportParams() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ReportTitle", "Address Report");
        return parameters;
    }

    private static ByteArrayInputStream createReportSourceInputStream() {
        // TODO Auto-generated method stub
        byte[] buf = new byte[2048];
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(buf);
        return sourceStream;
    }


}
