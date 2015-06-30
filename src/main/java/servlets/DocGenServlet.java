package servlets;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import net.sf.jasperreports.engine.JRConstants;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;

public class DocGenServlet extends HttpServlet {

    private static final long serialVersionUID = JRConstants.SERIAL_VERSION_UID;
    
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
        // TODO Auto-generated method stub
        return null;
    }

    private static Map<String, Object> getReportParams() {
        // TODO Auto-generated method stub
        return null;
    }

    private static ByteArrayInputStream createReportSourceInputStream() {
        // TODO Auto-generated method stub
        byte[] buf = new byte[256];
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(buf);
        return sourceStream;
    }


}
