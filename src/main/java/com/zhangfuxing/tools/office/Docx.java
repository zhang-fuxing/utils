package com.zhangfuxing.tools.office;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/26
 * @email zhangfuxing1010@163.com
 */
public class Docx {
    public static void main(String[] args) throws IOException, Docx4JException {
        File file1 = new File("H:\\temp\\test\\1.docx");
        File file2 = new File("H:\\temp\\test\\2.docx");
        File file3 = new File("H:\\temp\\test\\3.docx");
        WordprocessingMLPackage pkg1 = WordprocessingMLPackage.load(new FileInputStream(file1));
        WordprocessingMLPackage pkg2 = WordprocessingMLPackage.load(new FileInputStream(file2));
        WordprocessingMLPackage pkg3 = WordprocessingMLPackage.load(new FileInputStream(file3));
        MainDocumentPart documentPart1 = pkg1.getMainDocumentPart();
        MainDocumentPart documentPart2 = pkg2.getMainDocumentPart();
        MainDocumentPart documentPart3 = pkg3.getMainDocumentPart();
        documentPart1.addParagraphOfText("");
        documentPart1.getContent().addAll(documentPart2.getContent());
        documentPart1.addParagraphOfText("");
        documentPart1.getContent().addAll(documentPart3.getContent());

    }

}
