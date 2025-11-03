package org.pkaq.core.mvc.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.pkaq.core.properties.EvaConfig;

/**
 * @author PKAQ
 */
@Data
public class PageBo implements Bo {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 30;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 1")
    private Integer pageNo;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小值为 1")
    @Max(value = 100, message = "每页条数最大值为 100")
    private Integer pageSize;

    public PageBo(EvaConfig config) {
        this.pageSize = config.getPage().getSize() != null ? config.getPage().getSize() : DEFAULT_PAGE_SIZE;
    }

    public PageBo() {
        this.pageNo = DEFAULT_PAGE_NO;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }
}
