package org.pkaq.core.mvc.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author PKAQ
 */
@Data
public class IdCodeBo implements Bo {
    private Long id;

    @NotBlank(message = "{4007}")
    private String code;
}
