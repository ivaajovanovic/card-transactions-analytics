package rs.ac.uns.acs.nais.columnar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Globalna Bean Validation i method-level validation (@Validated na servisima).
 */
@Configuration
public class ValidationConfig {

    /**
     * Uključuje validaciju parametara/metoda kada koristimo @Validated na @Service klasama.
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    /**
     * Primary Validator bean (čita messages iz messages.properties ako postoji).
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}
