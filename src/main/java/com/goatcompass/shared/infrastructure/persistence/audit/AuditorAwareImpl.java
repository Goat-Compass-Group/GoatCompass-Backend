package com.goatcompass.shared.infrastructure.persistence.audit;

import com.goatcompass.shared.infrastructure.security.SecurityContextHelper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component("auditorAware")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private final SecurityContextHelper helper;

    @Override
    public Optional<String> getCurrentAuditor() {
        return helper.getCurrentUserId().map(Object::toString);
    }
}
