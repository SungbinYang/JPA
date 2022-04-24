package me.sungbin.jpashop.domain;

import javax.persistence.*;

/**
 * packageName : me.sungbin.jpashop.domain
 * fileName : Delivery
 * author : rovert
 * date : 2022/04/14
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/14       rovert         최초 생성
 */

@Entity
public class Delivery extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    private DeliveryStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
}
