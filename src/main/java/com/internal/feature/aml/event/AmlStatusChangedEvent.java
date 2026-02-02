package com.internal.feature.aml.event;

import com.internal.feature.aml.dto.response.AmlStatusDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AmlStatusChangedEvent extends ApplicationEvent {
    private final AmlStatusDto amlStatusDto;

    public AmlStatusChangedEvent(Object source, AmlStatusDto amlStatusDto) {
        super(source);
        this.amlStatusDto = amlStatusDto;
    }
}
