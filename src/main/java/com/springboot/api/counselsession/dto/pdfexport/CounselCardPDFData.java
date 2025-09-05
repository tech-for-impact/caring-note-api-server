package com.springboot.api.counselsession.dto.pdfexport;

import java.util.List;
import java.util.Optional;

import com.springboot.api.counselcard.entity.CounselCard;

import lombok.Builder;
import lombok.Getter;

/**
 * PDF 내보내기를 위한 상담 카드 데이터 DTO
 */
@Getter
@Builder
public class CounselCardPDFData {

    // 기본 정보 (상담 목적 및 특이사항)
    private String counselPurpose;
    private String counselNote;

    // 건강 정보
    private List<String> allergies;
    private String allergyNote;
    private List<String> diseases;
    private String diseaseHistoryNote;
    private String mainInconvenienceNote;
    private List<String> medicationSideEffects;
    private String medicationSideEffectNote;

    // 생활 정보
    private String smokingStatus;
    private String smokingNote;
    private String drinkingAmount;
    private String drinkingNote;
    private String exerciseType;
    private String exerciseNote;
    private String medicationManagementMethod;
    private String medicationManagementNote;
    private String nutritionStatus;
    private String nutritionNote;

    // 자립생활 정보
    private String communicationMethod;
    private String communicationNote;
    private String evacuationMethod;
    private String evacuationNote;
    private String walkingMethod;
    private String walkingNote;

    // 카드 상태
    private String cardRecordStatus;

    /**
     * CounselCard 엔티티로부터 PDF용 데이터를 생성
     */
    public static CounselCardPDFData from(CounselCard counselCard) {
        return CounselCardPDFData.builder()
                // 기본 정보
                .counselPurpose(Optional.ofNullable(counselCard.getCounselPurposeAndNote())
                        .map(info -> formatCounselPurposeList(info.getCounselPurpose()))
                        .orElse(null))
                .counselNote(Optional.ofNullable(counselCard.getCounselPurposeAndNote())
                        .map(info -> info.getSignificantNote())
                        .orElse(null))

                // 건강 정보 - 알레르기
                .allergies(Optional.ofNullable(counselCard.getAllergy())
                        .map(allergy -> allergy.getIsAllergic() != null && allergy.getIsAllergic() ? 
                                List.of("알레르기 있음") : List.of("알레르기 없음"))
                        .orElse(null))
                .allergyNote(Optional.ofNullable(counselCard.getAllergy())
                        .map(allergy -> allergy.getAllergyNote())
                        .orElse(null))

                // 건강 정보 - 질병
                .diseases(Optional.ofNullable(counselCard.getDiseaseInfo())
                        .map(diseaseInfo -> diseaseInfo.getDiseases() != null ? 
                                diseaseInfo.getDiseases().stream().map(Enum::name).toList() : null)
                        .orElse(null))
                .diseaseHistoryNote(Optional.ofNullable(counselCard.getDiseaseInfo())
                        .map(diseaseInfo -> diseaseInfo.getHistoryNote())
                        .orElse(null))
                .mainInconvenienceNote(Optional.ofNullable(counselCard.getDiseaseInfo())
                        .map(diseaseInfo -> diseaseInfo.getMainInconvenienceNote())
                        .orElse(null))

                // 건강 정보 - 약물 부작용
                .medicationSideEffects(Optional.ofNullable(counselCard.getMedicationSideEffect())
                        .map(sideEffect -> sideEffect.getIsMedicationSideEffect() != null && sideEffect.getIsMedicationSideEffect() ? 
                                List.of("약물 부작용 있음") : List.of("약물 부작용 없음"))
                        .orElse(null))
                .medicationSideEffectNote(Optional.ofNullable(counselCard.getMedicationSideEffect())
                        .map(sideEffect -> combineMedicationSideEffectNotes(sideEffect.getSuspectedMedicationNote(), sideEffect.getSymptomsNote()))
                        .orElse(null))

                // 생활 정보 - 흡연
                .smokingStatus(Optional.ofNullable(counselCard.getSmoking())
                        .map(smoking -> smoking.getSmokingAmount() != null ? smoking.getSmokingAmount().name() : null)
                        .orElse(null))
                .smokingNote(Optional.ofNullable(counselCard.getSmoking())
                        .map(smoking -> smoking.getSmokingPeriodNote())
                        .orElse(null))

                // 생활 정보 - 음주
                .drinkingAmount(Optional.ofNullable(counselCard.getDrinking())
                        .map(drinking -> drinking.getDrinkingAmount() != null ? drinking.getDrinkingAmount().name() : null)
                        .orElse(null))
                .drinkingNote(null) // Drinking entity에 note 필드가 없음

                // 생활 정보 - 운동
                .exerciseType(Optional.ofNullable(counselCard.getExercise())
                        .map(exercise -> exercise.getExercisePattern() != null ? exercise.getExercisePattern().name() : null)
                        .orElse(null))
                .exerciseNote(Optional.ofNullable(counselCard.getExercise())
                        .map(exercise -> exercise.getExerciseNote())
                        .orElse(null))

                // 생활 정보 - 복약 관리
                .medicationManagementMethod(Optional.ofNullable(counselCard.getMedicationManagement())
                        .map(management -> management.getIsAlone() != null ? 
                                (management.getIsAlone() ? "혼자 관리" : "도움 필요") : null)
                        .orElse(null))
                .medicationManagementNote(Optional.ofNullable(counselCard.getMedicationManagement())
                        .map(management -> management.getHouseMateNote())
                        .orElse(null))

                // 생활 정보 - 영양
                .nutritionStatus(Optional.ofNullable(counselCard.getNutrition())
                        .map(nutrition -> nutrition.getMealPattern() != null ? 
                                nutrition.getMealPattern().name() : null)
                        .orElse(null))
                .nutritionNote(Optional.ofNullable(counselCard.getNutrition())
                        .map(nutrition -> nutrition.getNutritionNote())
                        .orElse(null))

                // 자립생활 정보 - 의사소통
                .communicationMethod(Optional.ofNullable(counselCard.getCommunication())
                        .map(communication -> communication.getCommunications() != null ? 
                                communication.getCommunications().name() : null)
                        .orElse(null))
                .communicationNote(formatCommunicationInfo(counselCard.getCommunication()))

                // 자립생활 정보 - 대피
                .evacuationMethod(Optional.ofNullable(counselCard.getEvacuation())
                        .map(evacuation -> formatEvacuationMethods(evacuation.getEvacuations()))
                        .orElse(null))
                .evacuationNote(Optional.ofNullable(counselCard.getEvacuation())
                        .map(evacuation -> evacuation.getEvacuationNote())
                        .orElse(null))

                // 자립생활 정보 - 보행
                .walkingMethod(Optional.ofNullable(counselCard.getWalking())
                        .map(walking -> formatWalkingMethods(walking.getWalkingMethods()))
                        .orElse(null))
                .walkingNote(Optional.ofNullable(counselCard.getWalking())
                        .map(walking -> walking.getWalkingNote())
                        .orElse(null))

                // 카드 상태
                .cardRecordStatus(Optional.ofNullable(counselCard.getCardRecordStatus())
                        .map(status -> status.name())
                        .orElse(null))

                .build();
    }

    /**
     * 상담 목적 리스트를 문자열로 변환
     */
    private static String formatCounselPurposeList(List<?> purposes) {
        if (purposes == null || purposes.isEmpty()) {
            return null;
        }
        return purposes.stream()
                .map(Object::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    /**
     * 의사소통 정보를 종합적으로 포맷
     */
    private static String formatCommunicationInfo(com.springboot.api.counselcard.entity.information.independentlife.Communication communication) {
        if (communication == null) {
            return null;
        }
        
        StringBuilder info = new StringBuilder();
        
        if (communication.getSights() != null && !communication.getSights().isEmpty()) {
            info.append("시력: ").append(communication.getSights().stream()
                    .map(Object::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
        }
        
        if (communication.getHearings() != null && !communication.getHearings().isEmpty()) {
            if (info.length() > 0) info.append(" / ");
            info.append("청력: ").append(communication.getHearings().stream()
                    .map(Object::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
        }
        
        if (communication.getUsingKoreans() != null && !communication.getUsingKoreans().isEmpty()) {
            if (info.length() > 0) info.append(" / ");
            info.append("한국어 사용: ").append(communication.getUsingKoreans().stream()
                    .map(Object::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
        }
        
        return info.length() > 0 ? info.toString() : null;
    }

    /**
     * 대피 방법 리스트를 문자열로 변환
     */
    private static String formatEvacuationMethods(List<?> methods) {
        if (methods == null || methods.isEmpty()) {
            return null;
        }
        return methods.stream()
                .map(Object::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    /**
     * 보행 방법 리스트를 문자열로 변환
     */
    private static String formatWalkingMethods(List<?> methods) {
        if (methods == null || methods.isEmpty()) {
            return null;
        }
        return methods.stream()
                .map(Object::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    /**
     * 약물 부작용 노트들을 결합
     */
    private static String combineMedicationSideEffectNotes(String suspectedMedicationNote, String symptomsNote) {
        StringBuilder combined = new StringBuilder();
        
        if (suspectedMedicationNote != null && !suspectedMedicationNote.trim().isEmpty()) {
            combined.append("의심 약물: ").append(suspectedMedicationNote);
        }
        
        if (symptomsNote != null && !symptomsNote.trim().isEmpty()) {
            if (combined.length() > 0) {
                combined.append(" / ");
            }
            combined.append("증상: ").append(symptomsNote);
        }
        
        return combined.length() > 0 ? combined.toString() : null;
    }
}
