package io.nerv.web.jxc.purchasing.order.repository;

import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA接口
 */
@Repository
public interface PurchasingOrderRepo extends JpaRepository<PurchasingOrderLine, String> {
}
