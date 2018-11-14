package org.pkaq.web.jxc.instock.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.pkaq.core.mvc.service.BaseService;
import org.pkaq.web.jxc.instock.entity.InstockEntity;
import org.pkaq.web.jxc.instock.mapper.InstockLineMapper;
import org.pkaq.web.jxc.instock.mapper.InstockMapper;
import org.pkaq.web.jxc.instock.vo.InstockVO;
import org.pkaq.web.jxc.stock.entity.StockEntity;
import org.pkaq.web.jxc.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 采购入库单
 * @author: S.PKAQ
 * @Datetime: 2018/4/4 7:59
 */
@Service
public class InstockService extends BaseService<InstockMapper, InstockEntity> {
    @Autowired
    private InstockLineMapper lineMapper;

    @Autowired
    private StockService stockService;

    /**
     * 查询入库明细
     * @param instockVO
     * @param pageNo
     * @return
     */
    public IPage<InstockEntity> listPage(InstockVO instockVO, Integer pageNo){
        if (null == pageNo) {
            pageNo = 1;
        }
        Page pagination = new Page();
        pagination.setCurrent(pageNo);

        return this.mapper.listInstock(pagination, instockVO);
    }
    /**
     * 新增/编辑采购入库单
     * @param instock
     */
    public void save(InstockEntity instock) {
        boolean isNew = StrUtil.isBlank(instock.getId());
        this.merge(instock);
        String mainId = instock.getId();

        instock.getLine().forEach(item ->{
            if (isNew) {
                item.setMainId(mainId);
                item.setBuyamount(NumberUtil.mul(item.getBuynum(), item.getBuyprice()));
                this.lineMapper.insert(item);
            } else {
                this.lineMapper.updateById(item);
            }
            // 更新库存
            StockEntity stockEntity = new StockEntity();
            stockEntity.setOnhand(item.getBuynum());
            stockEntity.setAmount(item.getBuyamount());
            stockEntity.setGoodsId(item.getGoodsId());

            BeanUtil.copyProperties(instock, stockEntity);
            stockEntity.setId(null);

            stockService.changeOnhandNum(stockEntity);
        });
    }
}
