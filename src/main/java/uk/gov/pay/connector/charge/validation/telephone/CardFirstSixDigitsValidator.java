package uk.gov.pay.connector.charge.validation.telephone;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CardFirstSixDigitsValidator implements ConstraintValidator<ValidCardFirstSixDigits, String> {

    private Pattern pattern = Pattern.compile("\\d{6}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        
        return pattern.matcher(value).matches();
    }
}
