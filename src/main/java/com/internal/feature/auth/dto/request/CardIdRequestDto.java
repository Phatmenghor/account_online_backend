package com.internal.feature.auth.dto.request;


import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CardIdRequestDto {

    @NotNull(message = "Card ID cannot be null")
    @Size(min = 4, message = "Id card must have at least 4 characters")
    private String cardId;
}
