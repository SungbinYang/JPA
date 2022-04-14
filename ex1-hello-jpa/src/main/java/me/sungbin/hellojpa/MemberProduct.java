package me.sungbin.hellojpa;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : MemberProduct
 * author : rovert
 * date : 2022/04/14
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/14       rovert         최초 생성
 */

@Entity
public class MemberProduct {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    private int count;

    private int price;

    private LocalDateTime orderDateTime;
}
