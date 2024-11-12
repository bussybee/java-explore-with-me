package ru.practicum.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.repository.CategoryRepository;

@Component
public class UniqueCatNameValidator implements ConstraintValidator<UniqueCategoryName, String> {
    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return !categoryRepository.existsByName(name);
    }
}
