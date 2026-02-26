package az.edu.itbrains.SmartScore.services.impls;
import az.edu.itbrains.SmartScore.services.PdfService;
import org.apache.pdfbox.Loader; // В 3-й версии используем Loader
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;


@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    @Override
    public String extractText(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return "Xəta: Fayl tapılmadı - " + filePath;
        }
        try(PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return "Xəta: PDF oxunarkən problem yarandı: " + e.getMessage();
        }
    }
}
