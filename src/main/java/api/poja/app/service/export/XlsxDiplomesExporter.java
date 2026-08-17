package api.poja.app.service.export;

import api.poja.app.endpoint.rest.model.DiplomeDto;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class XlsxDiplomesExporter {

  public File generer(List<DiplomeDto> diplomes, Integer annee) {
    try {
      var file = File.createTempFile("diplomes-promo-" + annee + "-", ".xlsx");
      try (var workbook = new XSSFWorkbook();
          var out = new FileOutputStream(file)) {
        Sheet sheet = workbook.createSheet("Diplomes " + annee);
        writeRow(sheet, 0, "Rang", "STD", "Nom", "Prenom", "Moyenne");
        int i = 1;
        for (DiplomeDto d : diplomes) {
          writeRow(
              sheet,
              i++,
              String.valueOf(d.rang()),
              d.std(),
              d.nom(),
              d.prenom(),
              d.moyenneGenerale().toString());
        }
        workbook.write(out);
        return file;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeRow(Sheet sheet, int rowIndex, String... values) {
    Row row = sheet.createRow(rowIndex);
    for (int i = 0; i < values.length; i++) {
      Cell cell = row.createCell(i);
      cell.setCellValue(values[i]);
    }
  }
}
