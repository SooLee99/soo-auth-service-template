package io.soo.springboot.core.support.valid

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [BirthDateValidator::class])
annotation class ValidBirthDate(
    val message: String = "생년월일이 올바르지 않습니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
)
