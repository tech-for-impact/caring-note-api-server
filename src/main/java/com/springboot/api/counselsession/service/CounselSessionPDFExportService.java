package com.springboot.api.counselsession.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.springboot.api.common.exception.NoContentException;
import com.springboot.api.common.util.AiResponseParseUtil;
import com.springboot.api.counselcard.entity.CounselCard;
import com.springboot.api.counselcard.repository.CounselCardRepository;
import com.springboot.api.counselsession.dto.pdfexport.CounselSessionPDFExportDTO;
import com.springboot.api.counselsession.entity.AICounselSummary;
import com.springboot.api.counselsession.entity.CounselSession;
import com.springboot.api.counselsession.entity.MedicationCounsel;
import com.springboot.api.counselsession.repository.AICounselSummaryRepository;
import com.springboot.api.counselsession.repository.CounselSessionRepository;
import com.springboot.api.counselsession.repository.MedicationCounselRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상담 세션 PDF 내보내기 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CounselSessionPDFExportService {

    private final CounselSessionRepository counselSessionRepository;
    private final MedicationCounselRepository medicationCounselRepository;
    private final AICounselSummaryRepository aiCounselSummaryRepository;
    private final CounselCardRepository counselCardRepository;
    private final AiResponseParseUtil aiResponseParseUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    /**
     * 상담 세션의 종합 데이터를 PDF로 내보내기
     */
    @Transactional(readOnly = true)
    public ByteArrayResource exportCounselSessionToPDF(String counselSessionId) {
        // 모든 관련 데이터 수집
        CounselSessionPDFExportDTO exportData = collectCounselSessionData(counselSessionId);
        
        // HTML 생성
        String htmlContent = generateHTML(exportData);
        
        // PDF 생성
        byte[] pdfBytes = convertHtmlToPdf(htmlContent);
        
        return new ByteArrayResource(pdfBytes);
    }

    /**
     * 상담 세션과 관련된 모든 데이터를 수집하여 DTO로 반환
     */
    private CounselSessionPDFExportDTO collectCounselSessionData(String counselSessionId) {
        // 상담 세션 조회 (필수)
        CounselSession counselSession = counselSessionRepository.findById(counselSessionId)
                .orElseThrow(() -> new NoContentException("해당 상담 세션을 찾을 수 없습니다."));

        // 중재기록 조회 (선택적)
        Optional<MedicationCounsel> medicationCounsel = medicationCounselRepository
                .findByCounselSessionId(counselSessionId);

        // AI 상담 요약 조회 (선택적)
        Optional<AICounselSummary> aiCounselSummary = aiCounselSummaryRepository
                .findByCounselSessionId(counselSessionId);

        // AI 분석 결과 텍스트 파싱 (선택적)
        Optional<String> parsedTaResult = aiCounselSummary
                .map(AICounselSummary::getTaResult)
                .flatMap(aiResponseParseUtil::extractAnalysedTextSafely);

        // 상담 카드 조회 (선택적)
        Optional<CounselCard> counselCard = counselCardRepository
                .findCounselCardByCounselSessionId(counselSessionId);

        return CounselSessionPDFExportDTO.from(
                counselSession,
                medicationCounsel,
                aiCounselSummary,
                counselCard,
                parsedTaResult
        );
    }

    /**
     * PDF용 HTML 콘텐츠 생성
     */
    private String generateHTML(CounselSessionPDFExportDTO data) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html lang='ko'>")
            .append("<head>")
            .append("<meta charset='UTF-8'>")
            .append("<title>상담 세션 보고서</title>")
            .append("<style>")
            .append(getCSSStyles())
            .append("</style>")
            .append("</head>")
            .append("<body>");

        // 헤더
        html.append(generateHeader(data));
        
        // 상담 세션 기본 정보
        html.append(generateSessionInfo(data));
        
        // 중재기록 정보
        if (data.getCounselRecord() != null) {
            html.append(generateMedicationCounselInfo(data));
        }
        
        // AI 분석 결과
        if (data.getParsedTaResult() != null) {
            html.append(generateAISummaryInfo(data));
        }
        
        // 상담 카드 정보
        if (data.getCounselCardData() != null) {
            html.append(generateCounselCardInfo(data));
        }

        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * CSS 스타일 정의
     */
    private String getCSSStyles() {
        return """
            body { 
                font-family: 'Malgun Gothic', Arial, sans-serif; 
                margin: 20px; 
                line-height: 1.6; 
                color: #333;
            }
            h1 { 
                text-align: center; 
                color: #2c3e50; 
                border-bottom: 3px solid #3498db; 
                padding-bottom: 10px; 
                margin-bottom: 30px;
            }
            h2 { 
                color: #34495e; 
                border-left: 4px solid #3498db; 
                padding-left: 10px; 
                margin-top: 25px; 
                margin-bottom: 15px;
            }
            h3 { 
                color: #2c3e50; 
                margin-top: 20px; 
                margin-bottom: 10px;
            }
            .info-section { 
                margin-bottom: 25px; 
                padding: 15px; 
                background-color: #f8f9fa; 
                border-radius: 5px; 
                border-left: 4px solid #3498db;
            }
            .info-row { 
                display: flex; 
                margin-bottom: 8px; 
                align-items: flex-start;
            }
            .info-label { 
                font-weight: bold; 
                min-width: 120px; 
                color: #2c3e50;
            }
            .info-value { 
                flex: 1; 
                word-wrap: break-word;
            }
            .card-grid { 
                display: grid; 
                grid-template-columns: 1fr 1fr; 
                gap: 15px; 
                margin-top: 15px;
            }
            .card-item { 
                background-color: #ffffff; 
                padding: 12px; 
                border: 1px solid #dee2e6; 
                border-radius: 4px;
            }
            .text-content { 
                background-color: #ffffff; 
                padding: 15px; 
                border: 1px solid #dee2e6; 
                border-radius: 4px; 
                white-space: pre-wrap; 
                margin-top: 10px;
            }
            ul { 
                margin: 0; 
                padding-left: 20px;
            }
            .date-info { 
                text-align: center; 
                color: #7f8c8d; 
                margin-bottom: 20px; 
                font-style: italic;
            }
            .gray-line {
                height: 1px;
                background-color: #bdc3c7;
                margin: 15px 0;
                border: none;
            }
            """;
    }

    /**
     * 헤더 생성
     */
    private String generateHeader(CounselSessionPDFExportDTO data) {
        StringBuilder header = new StringBuilder();
        
        // 내담자 이름과 날짜로 제목 구성
        String counseleeName = data.getCounseleeName() != null ? data.getCounseleeName() : "미지정";
        String sessionDate = data.getScheduledStartDateTime() != null 
                ? data.getScheduledStartDateTime().format(DATE_FORMATTER)
                : "미정";
        
        String title = counseleeName + "_" + sessionDate;
        header.append("<h1>").append(title).append("</h1>");
        
        // 회색 라인 추가
        header.append("<hr class='gray-line'>");
        
        header.append("<div class='date-info'>")
              .append("보고서 생성일: ").append(java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER))
              .append(" | 상담일: ").append(sessionDate)
              .append("</div>");
        
        return header.toString();
    }

    /**
     * 상담 세션 기본 정보 생성
     */
    private String generateSessionInfo(CounselSessionPDFExportDTO data) {
        StringBuilder info = new StringBuilder();
        info.append("<div class='info-section'>")
            .append("<h2>상담 세션 정보</h2>");
        
        info.append(createInfoRow("세션 ID", data.getCounselSessionId()))
            .append(createInfoRow("회차", data.getSessionNumber() != null ? data.getSessionNumber() + "회차" : "미정"))
            .append(createInfoRow("내담자명", data.getCounseleeName()))
            .append(createInfoRow("상담사명", data.getCounselorName()))
            .append(createInfoRow("상담 상태", translateStatus(data.getSessionStatus())))
            .append(createInfoRow("예정 시간", formatDateTime(data.getScheduledStartDateTime())))
            .append(createInfoRow("실제 시작 시간", formatDateTime(data.getStartDateTime())))
            .append(createInfoRow("실제 종료 시간", formatDateTime(data.getEndDateTime())));
        
        info.append("</div>");
        return info.toString();
    }

    /**
     * 중재기록 정보 생성
     */
    private String generateMedicationCounselInfo(CounselSessionPDFExportDTO data) {
        StringBuilder info = new StringBuilder();
        info.append("<div class='info-section'>")
            .append("<h2>중재기록</h2>");
        
        if (data.getCounselRecord() != null && !data.getCounselRecord().trim().isEmpty()) {
            info.append("<div class='text-content'>")
                .append(escapeHtml(data.getCounselRecord()))
                .append("</div>");
        } else {
            info.append("<div class='text-content'>중재기록이 작성되지 않았습니다.</div>");
        }
        
        info.append("</div>");
        return info.toString();
    }

    /**
     * AI 분석 결과 정보 생성
     */
    private String generateAISummaryInfo(CounselSessionPDFExportDTO data) {
        StringBuilder info = new StringBuilder();
        info.append("<div class='info-section'>")
            .append("<h2>AI 상담 분석 결과</h2>");
        
        info.append(createInfoRow("분석 상태", translateAIStatus(data.getAiSummaryStatus())));
        
        if (data.getSpeakers() != null && !data.getSpeakers().isEmpty()) {
            info.append(createInfoRow("화자 목록", String.join(", ", data.getSpeakers())));
        }
        
        if (data.getParsedTaResult() != null && !data.getParsedTaResult().trim().isEmpty()) {
            info.append("<h3>분석 내용</h3>")
                .append("<div class='text-content'>")
                .append(escapeHtml(data.getParsedTaResult()))
                .append("</div>");
        } else {
            info.append("<div class='text-content'>AI 분석 결과가 없습니다.</div>");
        }
        
        info.append("</div>");
        return info.toString();
    }

    /**
     * 상담 카드 정보 생성
     */
    private String generateCounselCardInfo(CounselSessionPDFExportDTO data) {
        StringBuilder info = new StringBuilder();
        var cardData = data.getCounselCardData();
        
        info.append("<div class='info-section'>")
            .append("<h2>상담 카드 정보</h2>");
        
        info.append(createInfoRow("카드 상태", translateCardStatus(cardData.getCardRecordStatus())));
        
        // 기본 정보
        if (cardData.getCounselPurpose() != null || cardData.getCounselNote() != null) {
            info.append("<h3>기본 정보</h3>")
                .append("<div class='card-grid'>")
                .append(createCardItem("상담 목적", cardData.getCounselPurpose()))
                .append(createCardItem("특이사항", cardData.getCounselNote()))
                .append("</div>");
        }
        
        // 건강 정보
        info.append("<h3>건강 정보</h3>")
            .append("<div class='card-grid'>")
            .append(createCardItem("알레르기", formatList(cardData.getAllergies())))
            .append(createCardItem("알레르기 특이사항", cardData.getAllergyNote()))
            .append(createCardItem("질병 정보", formatList(cardData.getDiseases())))
            .append(createCardItem("병력 특이사항", cardData.getDiseaseHistoryNote()))
            .append(createCardItem("주요 불편사항", cardData.getMainInconvenienceNote()))
            .append(createCardItem("약물 부작용", formatList(cardData.getMedicationSideEffects())))
            .append("</div>");
        
        // 생활 정보
        info.append("<h3>생활 정보</h3>")
            .append("<div class='card-grid'>")
            .append(createCardItem("흡연 상태", cardData.getSmokingStatus()))
            .append(createCardItem("흡연 특이사항", cardData.getSmokingNote()))
            .append(createCardItem("음주량", cardData.getDrinkingAmount()))
            .append(createCardItem("음주 특이사항", cardData.getDrinkingNote()))
            .append(createCardItem("운동 종류", cardData.getExerciseType()))
            .append(createCardItem("운동 특이사항", cardData.getExerciseNote()))
            .append(createCardItem("복약 관리 방법", cardData.getMedicationManagementMethod()))
            .append(createCardItem("복약 관리 특이사항", cardData.getMedicationManagementNote()))
            .append(createCardItem("영양 상태", cardData.getNutritionStatus()))
            .append(createCardItem("영양 특이사항", cardData.getNutritionNote()))
            .append("</div>");
        
        // 자립생활 정보
        info.append("<h3>자립생활 정보</h3>")
            .append("<div class='card-grid'>")
            .append(createCardItem("의사소통 방법", cardData.getCommunicationMethod()))
            .append(createCardItem("의사소통 특이사항", cardData.getCommunicationNote()))
            .append(createCardItem("대피 방법", cardData.getEvacuationMethod()))
            .append(createCardItem("대피 특이사항", cardData.getEvacuationNote()))
            .append(createCardItem("보행 방법", cardData.getWalkingMethod()))
            .append(createCardItem("보행 특이사항", cardData.getWalkingNote()))
            .append("</div>");
        
        info.append("</div>");
        return info.toString();
    }

    /**
     * 정보 행 생성 헬퍼 메서드
     */
    private String createInfoRow(String label, String value) {
        return "<div class='info-row'>"
                + "<div class='info-label'>" + label + ":</div>"
                + "<div class='info-value'>" + (value != null ? escapeHtml(value) : "미제공") + "</div>"
                + "</div>";
    }

    /**
     * 카드 아이템 생성 헬퍼 메서드
     */
    private String createCardItem(String label, String value) {
        return "<div class='card-item'>"
                + "<strong>" + label + ":</strong><br>"
                + (value != null && !value.trim().isEmpty() ? escapeHtml(value) : "미제공")
                + "</div>";
    }

    /**
     * 리스트를 문자열로 포맷
     */
    private String formatList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return String.join(", ", items);
    }

    /**
     * 날짜시간 포맷
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "미정";
    }

    /**
     * 상태 번역
     */
    private String translateStatus(String status) {
        if (status == null) return "미정";
        return switch (status) {
            case "SCHEDULED" -> "예정";
            case "IN_PROGRESS" -> "진행중";
            case "COMPLETED" -> "완료";
            case "CANCELED" -> "취소";
            default -> status;
        };
    }

    /**
     * AI 상태 번역
     */
    private String translateAIStatus(String status) {
        if (status == null) return "미제공";
        return switch (status) {
            case "STT_PROGRESS" -> "음성 변환 중";
            case "STT_COMPLETE" -> "음성 변환 완료";
            case "GPT_PROGRESS" -> "AI 분석 중";
            case "GPT_COMPLETE" -> "AI 분석 완료";
            case "GPT_FAILED" -> "AI 분석 실패";
            default -> status;
        };
    }

    /**
     * 카드 상태 번역
     */
    private String translateCardStatus(String status) {
        if (status == null) return "미정";
        return switch (status) {
            case "NOT_STARTED" -> "시작 안함";
            case "IN_PROGRESS" -> "진행중";
            case "COMPLETED" -> "완료";
            default -> status;
        };
    }

    /**
     * HTML 이스케이프
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }

    /**
     * HTML을 PDF로 변환
     */
    private byte[] convertHtmlToPdf(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter pdfWriter = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A4);
            
            ConverterProperties converterProperties = new ConverterProperties();
            
            HtmlConverter.convertToPdf(htmlContent, pdfDocument, converterProperties);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("PDF 생성 중 오류 발생", e);
            throw new RuntimeException("PDF 생성에 실패했습니다.", e);
        }
    }

    /**
     * PDF 내보내기를 위한 데이터 가용성 확인
     */
    @Transactional(readOnly = true)
    public boolean checkDataAvailability(String counselSessionId) {
        try {
            // 상담 세션이 존재하는지 확인 (필수)
            // 기본적으로 상담 세션만 있어도 PDF 생성 가능
            // 다른 데이터들(중재기록, AI 분석, 상담카드)은 선택적
            return counselSessionRepository.existsById(counselSessionId);
            
        } catch (Exception e) {
            log.error("데이터 가용성 확인 중 오류 발생 - 세션 ID: {}", counselSessionId, e);
            return false;
        }
    }
}
