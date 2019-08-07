package io.nerv.web.jxc.purchasing.order.repository;

import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * JPA接口
 */
@Repository
public interface PurchasingRepo extends JpaRepository<PurchasingOrder, String> {
}
