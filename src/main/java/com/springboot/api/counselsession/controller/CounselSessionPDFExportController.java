package com.springboot.api.counselsession.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.api.counselsession.entity.CounselSession;
import com.springboot.api.counselsession.repository.CounselSessionRepository;
import com.springboot.api.counselsession.service.CounselSessionPDFExportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상담 세션 PDF 내보내기 컨트롤러
 */
@RestController
@RequestMapping("/api/counsel-sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CounselSession PDF Export", description = "상담 세션 PDF 내보내기 API")
public class CounselSessionPDFExportController {

    private final CounselSessionPDFExportService counselSessionPDFExportService;
    private final CounselSessionRepository counselSessionRepository;

    /**
     * 상담 세션 데이터를 PDF로 내보내기
     * 
     * @param counselSessionId 상담 세션 ID
     * @return PDF 파일을 담은 ResponseEntity
     */
    @Operation(
        summary = "상담 세션 PDF 내보내기",
        description = "지정된 상담 세션의 모든 관련 데이터(세션 정보, 중재기록, AI 분석 결과, 상담 카드)를 종합하여 PDF 보고서로 내보냅니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "PDF 파일 생성 성공"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "해당 상담 세션을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "PDF 생성 중 서버 오류 발생"
        )
    })
    @GetMapping("/{counselSessionId}/export/pdf")
    public ResponseEntity<ByteArrayResource> exportCounselSessionToPDF(
            @Parameter(description = "상담 세션 ID", required = true)
            @PathVariable String counselSessionId) {

        try {
            log.info("상담 세션 PDF 내보내기 요청 - 세션 ID: {}", counselSessionId);

            // 상담세션 정보 조회 (파일명 생성용)
            CounselSession counselSession = counselSessionRepository.findById(counselSessionId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 상담 세션을 찾을 수 없습니다."));

            // PDF 생성
            ByteArrayResource pdfResource = counselSessionPDFExportService
                    .exportCounselSessionToPDF(counselSessionId);

            // 파일명 생성 (한글 파일명을 안전하게 인코딩)
            String counseleeName = counselSession.getCounselee() != null ? 
                    counselSession.getCounselee().getName() : "미지정";
            String counselDate = counselSession.getScheduledStartDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = String.format("복약상담_%s_%s.pdf", counseleeName, counselDate);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20"); // 공백 처리

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            log.info("상담 세션 PDF 내보내기 성공 - 세션 ID: {}, 파일명: {}, 파일 크기: {} bytes", 
                    counselSessionId, fileName, pdfResource.contentLength());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfResource.contentLength())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfResource);

        } catch (Exception e) {
            log.error("상담 세션 PDF 내보내기 실패 - 세션 ID: {}", counselSessionId, e);
            throw e; // GlobalExceptionHandler에서 처리
        }
    }

    /**
     * PDF 내보내기 가능 여부 확인
     * 
     * @param counselSessionId 상담 세션 ID
     * @return PDF 내보내기 가능 여부
     */
    @Operation(
        summary = "PDF 내보내기 가능 여부 확인",
        description = "해당 상담 세션이 존재하는지 확인하여 PDF 내보내기가 가능한지 여부를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "확인 완료 (가능/불가능)"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "해당 상담 세션을 찾을 수 없음"
        )
    })
    @GetMapping("/{counselSessionId}/export/pdf/available")
    public ResponseEntity<PDFExportAvailabilityResponse> checkPDFExportAvailability(
            @Parameter(description = "상담 세션 ID", required = true)
            @PathVariable String counselSessionId) {

        try {
            log.debug("PDF 내보내기 가능 여부 확인 요청 - 세션 ID: {}", counselSessionId);

            // 서비스에서 데이터 존재 여부 확인 (실제 PDF 생성은 하지 않음)
            boolean available = counselSessionPDFExportService.checkDataAvailability(counselSessionId);

            PDFExportAvailabilityResponse response = new PDFExportAvailabilityResponse(
                    available, 
                    available ? "PDF 내보내기 가능합니다." : "데이터가 부족하여 PDF 내보내기가 제한됩니다."
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("PDF 내보내기 가능 여부 확인 실패 - 세션 ID: {}", counselSessionId, e);
            throw e;
        }
    }

    /**
     * PDF 내보내기 가능 여부 응답 DTO
     */
    public record PDFExportAvailabilityResponse(
            boolean available,
            String message
    ) {}
}
