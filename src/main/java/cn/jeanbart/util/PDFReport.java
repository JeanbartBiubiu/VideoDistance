package cn.jeanbart.util;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.TextAlignment;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Map;

public class PDFReport {
    PdfWriter pdfWriter; //创建
    PdfDocument pdfDocument;//创建
    Document document;//
    PdfFont font;//创建字体

    @SneakyThrows
    public PDFReport(String path) {
        String fileName = new File(path).getName() + "_report.pdf";
        path = new File(path).getParentFile().getPath() + "\\" + fileName;
        pdfWriter = new PdfWriter(path);
        pdfDocument = new PdfDocument(pdfWriter);
        font = getChineseFont();
        document = new Document(pdfDocument).setFont(font);
    }

    //第一页记录两视频文件信息和相关参数
    @SneakyThrows
    public void init(String title, String path1, String path2, Map<String, String> option) {
        String md51 = "", md52 = "";
        try {
            md51 = DigestUtils.md5Hex(new FileInputStream(path1));
            md52 = DigestUtils.md5Hex(new FileInputStream(path2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //相关参数
        document.add(new Paragraph(title).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("开始检测时间:" + LocalDateTime.now()));
        document.add(new Paragraph("视频1的MD5值：" + md51));
        document.add(new Paragraph("视频2的MD5值：" + md52));
        document.add(new Paragraph("视频1分辨率：" + option.getOrDefault("xy1", "未设置")
                + " 视频2分辨率：" + option.getOrDefault("xy2", "未设置")));
        document.add(new Paragraph("视频1帧率：" + option.getOrDefault("frameRate1", "未设置")
                + " 视频2帧率：" + option.getOrDefault("frameRate2", "未设置")));
        document.add(new Paragraph("视频1时长：" + option.getOrDefault("long1", "未设置")
                + " 视频2时长：" + option.getOrDefault("long2", "未设置")));
        document.add(new Paragraph("视频1开始检测位置：" + option.getOrDefault("startIndex1", "未设置")
                + " 视频2开始检测位置：" + option.getOrDefault("startIndex2", "未设置")));
        document.add(new Paragraph("视频裁切位置：" + md51));
        //document.close();
    }

    //三张图，两个时间戳
    public void addPage(byte[] byte1, String timeTemp1, byte[] byte2, String timeTemp2, byte[] byte3) {
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));//另起一页
        Image image1 = new Image(ImageDataFactory.create(byte1));
        //image.setHeight(225);//200pt，比A4的800多pt的三分之一再小一些
        image1.setAutoScaleWidth(true);
        Image image2 = new Image(ImageDataFactory.create(byte2));
        image2.setAutoScaleWidth(true);
        Image image3 = new Image(ImageDataFactory.create(byte3));
        image3.setAutoScaleWidth(true);
        document.add(new Paragraph().setTextAlignment(TextAlignment.CENTER).add(image1));
        document.add(new Paragraph().setTextAlignment(TextAlignment.CENTER).add(timeTemp1));
        if(image2 == null){
            document.add(new Paragraph("以上片段被删减"));
        }else {
            document.add(new Paragraph().setTextAlignment(TextAlignment.CENTER).add(image2));
            document.add(new Paragraph().setTextAlignment(TextAlignment.CENTER).add(timeTemp2));
        }
        document.add(new Paragraph().setTextAlignment(TextAlignment.CENTER).add(image3));
    }

    public void addParagraph(String s) {
        document.add(new Paragraph(s));
    }

    public void close() {
        document.close();
    }

    /**
     * 设置字体
     * itext7 中不支持中文字体的，需要自己设置，解决中文字体不显示的问题
     *
     * @return
     */
    public PdfFont getChineseFont() {
        PdfFont font;
        try {
            font = PdfFontFactory.createFont(this.getClass().getClassLoader().getResource("NotoSansCJKtc-Regular.otf").getPath(), PdfEncodings.IDENTITY_H, false);
            return font;
        } catch (IOException e) {
            System.out.println("字体设置失败:" + e);
        }
        return null;
    }

}
