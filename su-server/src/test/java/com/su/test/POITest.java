package com.su.test;


import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class POITest {
    @TempDir
    Path tempDir;

    @Test
    public void write() throws IOException {
        Path file = tempDir.resolve("info.xlsx");
        writeWorkbook(file);
    }

    @Test
    public void read() throws IOException {
        Path file = tempDir.resolve("info.xlsx");
        writeWorkbook(file);

        try (InputStream in = Files.newInputStream(file); XSSFWorkbook excel = new XSSFWorkbook(in)) {
            XSSFSheet sheet = excel.getSheet("info");
            for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
                XSSFRow row = sheet.getRow(i);
                for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                    row.getCell(j).toString();
                }
            }
        }
    }

    private void writeWorkbook(Path file) throws IOException {
        try (XSSFWorkbook excel = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(file)) {
            XSSFSheet sheet = excel.createSheet("info");
            XSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("姓名");
            row.createCell(1).setCellValue("性别");
            row.createCell(2).setCellValue("年龄");

            row = sheet.createRow(1);
            row.createCell(0).setCellValue("张三");
            row.createCell(1).setCellValue("男");
            row.createCell(2).setCellValue(18);

            row = sheet.createRow(2);
            row.createCell(0).setCellValue("王五");
            row.createCell(1).setCellValue("女");
            row.createCell(2).setCellValue(19);

            excel.write(out);
        }
        Files.size(file);
    }
}
