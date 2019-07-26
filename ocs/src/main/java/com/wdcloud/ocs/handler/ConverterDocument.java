package com.wdcloud.ocs.handler;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.PaperFormat;
import com.sun.star.view.XPrintable;

public class ConverterDocument extends StreamOpenOfficeDocumentConverter {
    private String fileType;
    private Integer colWidth;

    public ConverterDocument(OpenOfficeConnection connection, String fileType, Integer colWidth) {
        super(connection);
        this.colWidth = colWidth;
        this.fileType = fileType;
    }

    public final static Size A4, A3, A2, A1;
    public final static Size B4, B3, B2, B1;
    public final static Size KaoqinReport;

    static {
        A4 = new Size(21000, 29700);
        A3 = new Size(29700, 42000);
        A2 = new Size(42000, 59400);
        A1 = new Size(60000, 90000);

        B4 = new Size(25000, 35300);
        B3 = new Size(35300, 50000);
        B2 = new Size(50000, 70700);
        B1 = new Size(70700, 100000);

        KaoqinReport = new Size(42000, 54300);
    }

    /*
     * XComponent:xCalcComponent
     * 
     * @seecom.artofsolving.jodconverter.openoffice.converter.
     * AbstractOpenOfficeDocumentConverter
     * #refreshDocument(com.sun.star.lang.XComponent)
     */
    @Override
    protected void refreshDocument(XComponent document) {
        super.refreshDocument(document);

        // The default paper format and orientation is A4 and portrait. To
        // change paper orientation
        // re set page size
        XPrintable xPrintable = (XPrintable) UnoRuntime.queryInterface(XPrintable.class, document);
        PropertyValue[] printerDesc = new PropertyValue[3];
        printerDesc[0] = new PropertyValue();
        printerDesc[0].Name = "PaperFormat";
        printerDesc[0].Value = PaperFormat.USER;

        // Paper Size
        printerDesc[1] = new PropertyValue();
        printerDesc[1].Name = "PaperSize";
        if ("xls".equals(fileType) || "xlsx".equals(fileType) || "XLS".equals(fileType) || "XLSX".equals(fileType)) {
            if (colWidth <= 21000) {
                printerDesc[1].Value = A3;
            } else if (colWidth > 21000 && colWidth <= 29700) {
                printerDesc[1].Value = A2;
            } else if (colWidth > 29700 && colWidth <= 60000) {
                printerDesc[1].Value = A1;
            } else {
                printerDesc[1].Value = A3;
            }
        } else {
            printerDesc[1].Value = A4;
        }
        printerDesc[2] = new PropertyValue();
        printerDesc[2].Name = "Pages";
        printerDesc[2].Value = 2;
        try {
            xPrintable.setPrinter(printerDesc);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}