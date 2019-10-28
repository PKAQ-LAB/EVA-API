package io.nerv.web.jxc.purchasing.order.service;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.mvc.service.jpa.StdBaseService;
import io.nerv.core.mvc.vo.PageVo;
import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrder;
import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrderLine;
import io.nerv.web.jxc.purchasing.order.repository.PurchasingOrderRepo;
import io.nerv.web.jxc.purchasing.order.repository.PurchasingRepo;
import io.nerv.web.jxc.purchasing.order.vo.PurchasingVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchasingServices extends StdBaseService<PurchasingRepo, PurchasingOrder> {
    private PurchasingOrderRepo purchasingOrderRepo;

    /**
     * 查询code是否存在
     * @param code
     * @return
     */
    public boolean checkCode(String code, String id){
        if (StrUtil.isBlank(id)){
            return this.repository.countByCode(code) > 0;
        } else {
            return this.repository.countByCodeAndIdNot(code, id) > 0;
        }
    }

    /**
     * 按分页查询
     * @param page      当前页码
     * @param size      分页条数
     * @return          分页模型类
     */
    public PageVo<PurchasingVo> listAll(PurchasingOrder purchasingOrder, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<PurchasingVo> result = this.repository.findAllPurchasingWithLine(pageable);

        return new PageVo<PurchasingVo>().setCurrent(result.getNumber() + 1)
                                         .setSize(result.getSize())
                                         .setTotal(result.getTotalElements())
                                         .setRecords(result.getContent());
    }

    /**
     *
     * @param domains
     */
    @Override
    public void delete(List<PurchasingOrder> domains){
        List<PurchasingOrderLine> lineList = new ArrayList<>(domains.size());

        for (PurchasingOrder domain : domains) {
            PurchasingOrderLine prl = new PurchasingOrderLine();
            prl.setMainId(domain.getId());
            lineList.add(prl);
        }
        // 删除子表
        this.purchasingOrderRepo.deleteInBatch(lineList);

        this.repository.deleteInBatch(domains);
    }
}
