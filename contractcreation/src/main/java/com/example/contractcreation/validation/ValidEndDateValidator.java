package com.example.contractcreation.validation;

import com.example.contractcreation.model.Contract;
import com.example.contractcreation.model.Project;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ValidEndDateValidator implements ConstraintValidator<ValidEndDate, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {

        if (obj == null) {
            return true;
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (obj instanceof Contract contract) {
            startDate = contract.getStartDate();
            endDate = contract.getEndDate();
        }
        else if (obj instanceof Project project) {
            startDate = project.getStartDate();
            endDate = project.getEndDate();
        }

        if (startDate == null || endDate == null) {
            return true;
        }

        if (!endDate.isAfter(startDate)) {

            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate(
                            context.getDefaultConstraintMessageTemplate()
                    )
                    .addPropertyNode("endDate")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}