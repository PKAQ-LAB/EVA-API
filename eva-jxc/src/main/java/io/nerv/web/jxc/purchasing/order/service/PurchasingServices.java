package io.nerv.web.jxc.purchasing.order.service;

import io.nerv.core.mvc.service.jpa.StdBaseService;
import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrder;
import io.nerv.web.jxc.purchasing.order.repository.PurchasingRepo;
import org.springframework.stereotype.Service;

@Service
public class PurchasingServices extends StdBaseService<PurchasingRepo, PurchasingOrder> {
}
