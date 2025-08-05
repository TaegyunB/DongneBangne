package S13P11A708.backend.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PdfGenerationService {

    /**
     * HTML을 PDF로 변환
     */
    public byte[] generatePdfFromHtml(String htmlContent) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ConverterProperties converterProperties = new ConverterProperties();

            // HTML을 PDF로 변환
            HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties);

            log.info("PDF 생성 완료: {} bytes", outputStream.size());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("PDF 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("PDF 생성에 실패했습니다.", e);
        }

    }
}
