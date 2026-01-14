package io.soo.springboot.core.support.valid

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate

class BirthDateValidator : ConstraintValidator<ValidBirthDate, LocalDate?> {

    // 정책: 1900-01-01 이후만 허용 (필요 시 조정)
    private val minDate = LocalDate.of(1900, 1, 1)

    // 정책: 만 14세 이상(필요 없으면 제거 가능)
    private val minAgeYears = 14

    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true // nullable → 없으면 통과

        val today = LocalDate.now()

        // 미래일 금지
        if (value.isAfter(today)) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("생년월일은 미래 날짜로 입력할 수 없습니다.")
                .addConstraintViolation()
            return false
        }

        // 너무 과거 금지
        if (value.isBefore(minDate)) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("생년월일은 1900-01-01 이후 날짜만 입력할 수 있습니다.")
                .addConstraintViolation()
            return false
        }

        // 만 나이 제한(선택)
        val cutoff = today.minusYears(minAgeYears.toLong())
        if (value.isAfter(cutoff)) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("만 ${minAgeYears}세 이상만 가입할 수 있습니다.")
                .addConstraintViolation()
            return false
        }

        return true
    }
}
