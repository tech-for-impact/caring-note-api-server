package com.springboot.api.counselsession.dto.pdfexport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.springboot.api.counselcard.entity.CounselCard;
import com.springboot.api.counselsession.entity.AICounselSummary;
import com.springboot.api.counselsession.entity.CounselSession;
import com.springboot.api.counselsession.entity.MedicationCounsel;

import lombok.Builder;
import lombok.Getter;

/**
 * PDF 내보내기를 위한 종합 데이터 DTO
 * CounselSession, MedicationCounsel, AICounselSummary, CounselCard의 데이터를 결합
 */
@Getter
@Builder
public class CounselSessionPDFExportDTO {

    // CounselSession 정보
    private final String counselSessionId;
    private final LocalDateTime scheduledStartDateTime;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Integer sessionNumber;
    private final String counseleeId;
    private final String counseleeName;
    private final String counselorId;
    private final String counselorName;
    private final String sessionStatus;

    // MedicationCounsel 정보
    private final String medicationCounselId;
    private final String counselRecord;

    // AICounselSummary 정보
    private final String aiCounselSummaryId;
    private final JsonNode sttResult;
    private final JsonNode taResult;
    private final String parsedTaResult; // 파싱된 텍스트 형태의 AI 분석 결과
    private final List<String> speakers;
    private final String aiSummaryStatus;

    // CounselCard 정보
    private final String counselCardId;
    private final CounselCardPDFData counselCardData;

    /**
     * 엔티티들로부터 DTO를 생성하는 팩토리 메서드
     */
    public static CounselSessionPDFExportDTO from(
            CounselSession counselSession,
            Optional<MedicationCounsel> medicationCounsel,
            Optional<AICounselSummary> aiCounselSummary,
            Optional<CounselCard> counselCard,
            Optional<String> parsedTaResult) {

        return CounselSessionPDFExportDTO.builder()
                // CounselSession 정보
                .counselSessionId(counselSession.getId())
                .scheduledStartDateTime(counselSession.getScheduledStartDateTime())
                .startDateTime(counselSession.getStartDateTime())
                .endDateTime(counselSession.getEndDateTime())
                .sessionNumber(counselSession.getSessionNumber())
                .counseleeId(Optional.ofNullable(counselSession.getCounselee())
                        .map(counselee -> counselee.getId())
                        .orElse(null))
                .counseleeName(Optional.ofNullable(counselSession.getCounselee())
                        .map(counselee -> counselee.getName())
                        .orElse("미지정"))
                .counselorId(Optional.ofNullable(counselSession.getCounselor())
                        .map(counselor -> counselor.getId())
                        .orElse(null))
                .counselorName(Optional.ofNullable(counselSession.getCounselor())
                        .map(counselor -> counselor.getName())
                        .orElse("미지정"))
                .sessionStatus(counselSession.getStatus().name())

                // MedicationCounsel 정보
                .medicationCounselId(medicationCounsel.map(MedicationCounsel::getId).orElse(null))
                .counselRecord(medicationCounsel.map(MedicationCounsel::getCounselRecord).orElse(null))

                // AICounselSummary 정보
                .aiCounselSummaryId(aiCounselSummary.map(AICounselSummary::getId).orElse(null))
                .sttResult(aiCounselSummary.map(AICounselSummary::getSttResult).orElse(null))
                .taResult(aiCounselSummary.map(AICounselSummary::getTaResult).orElse(null))
                .parsedTaResult(parsedTaResult.orElse(null))
                .speakers(aiCounselSummary.map(AICounselSummary::getSpeakers).orElse(null))
                .aiSummaryStatus(aiCounselSummary
                        .map(summary -> summary.getAiCounselSummaryStatus().name())
                        .orElse(null))

                // CounselCard 정보
                .counselCardId(counselCard.map(CounselCard::getId).orElse(null))
                .counselCardData(counselCard.map(CounselCardPDFData::from).orElse(null))

                .build();
    }
}
