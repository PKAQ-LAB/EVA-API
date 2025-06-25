package org.pkaq.core.mvc.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdCodeBo implements Bo {
    @NotBlank(message = "{4007}")
    private Long id;

    @NotBlank(message = "{4007}")
    private String code;
}
