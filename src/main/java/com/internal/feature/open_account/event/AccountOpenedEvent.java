package com.internal.feature.open_account.event;

import com.internal.feature.open_account.dto.OpenAccountContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AccountOpenedEvent extends ApplicationEvent {
    private final OpenAccountContext context;

    public AccountOpenedEvent(Object source, OpenAccountContext context) {
        super(source);
        this.context = context;
    }
}
