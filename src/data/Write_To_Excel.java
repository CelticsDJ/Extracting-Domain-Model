package data;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import processing.ExtractRelations_includingChains;

public class Write_To_Excel {

    public static void Write_to_Excel(String filename) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("iTrust");

        Integer req_id = 1;
        while(req_id <= ExtractRelations_includingChains.hashmap_requirmenets_Relations.size()) {
            for (Requirement_Relations req_relations : ExtractRelations_includingChains.hashmap_requirmenets_Relations) {
                if (!req_relations.Req_Id.equals("R" + req_id.toString())) {
                    continue;
                }
                req_id++;


            }
        }
    }

}
