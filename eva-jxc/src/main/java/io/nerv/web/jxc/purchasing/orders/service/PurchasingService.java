package io.nerv.web.jxc.purchasing.orders.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.nerv.core.mvc.service.StdBaseService;
import io.nerv.web.jxc.inventory.accbook.service.StockService;
import io.nerv.web.jxc.purchasing.orders.mapper.PurchasingOrderLineMapper;
import io.nerv.web.jxc.purchasing.orders.mapper.PurchasingOrderMapper;
import io.nerv.web.jxc.purchasing.orders.entity.PurchasingOrderEntity;
import io.nerv.web.jxc.purchasing.orders.vo.PurchasingOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:59
 */
@Service
public class PurchasingService extends StdBaseService<PurchasingOrderMapper, PurchasingOrderEntity> {
    @Autowired
    private PurchasingOrderLineMapper lineMapper;

    @Autowired
    private StockService stockService;

    @Override
    public List<PurchasingOrderEntity> list(PurchasingOrderEntity purchasingOrderEntity){
        System.out.println(" 1------------->");
        return null;
    }

    /**
     * 查询入库明细
     * @param pageNo
     * @return
     */
    public IPage<PurchasingOrderEntity> listPage(PurchasingOrderVO purchasingOrderVO, Integer pageNo){
        if (null == pageNo) {
            pageNo = 1;
        }
        Page pagination = new Page();
        pagination.setCurrent(pageNo);

        return this.mapper.listInstock(pagination, purchasingOrderVO);
    }
    /**
     * 新增/编辑采购入库单
     * @param instock
     */
    public void save(PurchasingOrderEntity instock) {
        boolean isNew = StrUtil.isBlank(instock.getId());
        this.merge(instock);
        String mainId = instock.getId();

        //instock.getLine().forEach(item ->{
        //    if (isNew) {
        //        item.setMainId(mainId);
        //        item.setBuyamount(NumberUtil.mul(item.getBuynum(), item.getBuyprice()));
        //        this.lineMapper.insert(item);
        //    } else {
        //        this.lineMapper.updateById(item);
        //    }
        //    // 更新库存
        //    StockEntity stockEntity = new StockEntity();
        //    stockEntity.setOnhand(item.getBuynum());
        //    stockEntity.setAmount(item.getBuyamount());
        //    stockEntity.setGoodsId(item.getGoodsId());
        //
        //    BeanUtil.copyProperties(instock, stockEntity);
        //    stockEntity.setId(null);
        //
        //    stockService.changeOnhandNum(stockEntity);
        //});
    }
}
