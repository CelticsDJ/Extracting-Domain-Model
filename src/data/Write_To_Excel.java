package data;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import processing.ExtractRelations_includingChains;

import java.io.FileOutputStream;
import java.util.Iterator;

public class Write_To_Excel {

    public static void Write_to_Excel(String filename) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("iTrust");

        Integer req_id = 1;
        int rownum = 0;

        sheet.createRow(0);
        for(int i = 0; i < 8; ++i) {
            sheet.getRow(0).createCell(i);
        }
        sheet.getRow(0).getCell(0).setCellValue("Req_ID");
        sheet.getRow(0).getCell(1).setCellValue("Source_Concept");
        sheet.getRow(0).getCell(2).setCellValue("Target_Concept");
        sheet.getRow(0).getCell(3).setCellValue("Association_Content");
        sheet.getRow(0).getCell(4).setCellValue("Relation_Type");
        sheet.getRow(0).getCell(5).setCellValue("Evaluation_1");
        sheet.getRow(0).getCell(6).setCellValue("Evaluation_2");
        sheet.getRow(0).getCell(7).setCellValue("Note");

        while(req_id <= ExtractRelations_includingChains.hashmap_requirmenets_Relations.size()) {
            for (Requirement_Relations req_relations : ExtractRelations_includingChains.hashmap_requirmenets_Relations) {
                if (!req_relations.Req_Id.equals("R" + req_id.toString())) {
                    continue;
                }
                req_id++;

                try {
                    Iterator it = req_relations.relations.iterator();
                    while (it.hasNext()) {

                        Object obj = it.next();
                        Concept_Relation rel = (Concept_Relation) obj;
                        if (!rel.getDuplicateStatus()) {

                            HSSFRow row = sheet.createRow(++rownum);
                            for (int i = 0; i < 5; ++i) {
                                row.createCell(i);
                            }

                            row.getCell(0).setCellValue(req_relations.Req_Id);
                            row.getCell(1).setCellValue(rel.getSource().name);
                            row.getCell(2).setCellValue(rel.getTarget().name);
                            row.getCell(4).setCellValue(rel.rel_type.toString());

                            if(obj.getClass().toString().contains("Association_Relation")) {
                                Association_Relation rel2 = (Association_Relation) rel;
                                row.getCell(3).setCellValue(rel2.rel_name);
                            }
                        }
                    }
                }catch (Exception e) {
                    System.out.println(req_id);
                }
            }
        }

        for(Row row : sheet) {
            if(row.getCell(0).getStringCellValue().equals("")) {
                sheet.removeRow(row);
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(filename);
            workbook.write(outputStream);
            outputStream.flush();
        }catch (Exception e) {
            System.out.println(e);
        }
    }
}
