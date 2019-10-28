package io.nerv.web.jxc.purchasing.order.repository;

import io.nerv.web.jxc.purchasing.order.domain.PurchasingOrder;
import io.nerv.web.jxc.purchasing.order.vo.PurchasingVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * JPA接口
 */
@Repository
public interface PurchasingRepo extends JpaRepository<PurchasingOrder, String> {
    /**
     * 根据code和id计数
     * @param code
     * @param id
     * @return
     */
    int countByCodeAndIdNot(String code, String id);

    /**
     * 根据code计数
     * @param code
     * @return
     */
    int countByCode(String code);

    /**
     * 主子表联查返回列表VO
     * @return
     */
    @Query(value =  "select " +
                    " id, code, order_Date, stock, purchasing_Type, purchaser_Nm, supplier_Nm, NAME, category,gmt_create,gmt_modify,create_by,modify_by " +
                    " from " +
                    " jxc_purchasing_order,jxc_purchasing_line " +
                    "where " +
                    "id = main_id " +
                    "order by gmt_create desc", nativeQuery = true)
    Page<PurchasingVo> findAllPurchasingWithLine(Pageable pageable);
}
