package io.nerv.web.jxc.purchasing.order.service;

import io.nerv.core.mvc.service.jpa.StdBaseService;
import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrder;
import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrderLine;
import io.nerv.web.jxc.purchasing.order.repository.PurchasingRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchasingServices extends StdBaseService<PurchasingRepo, PurchasingOrder> {

    public void test(){
        PurchasingOrder purchasingOrder = new PurchasingOrder();
        purchasingOrder.setId("1159455441961488384");
        purchasingOrder.setCode("AAAAAAAAAAAAAAAAA 改");

        List<PurchasingOrderLine> list = new ArrayList<>();

        PurchasingOrderLine purchasingOrderLineA = new PurchasingOrderLine();
        purchasingOrderLineA.setBarcode("XXXXXX 改");
        purchasingOrderLineA.setCategory("0005");

        PurchasingOrderLine purchasingOrderLineB = new PurchasingOrderLine();
        purchasingOrderLineB.setBarcode("YYYYYYYY");
        purchasingOrderLineB.setCategory("0006");

        list.add(purchasingOrderLineA);
//        list.add(purchasingOrderLineB);

        purchasingOrder.setLine(list);

        this.repository.save(purchasingOrder);

        System.out.println(this.selectCount());
    }
}
