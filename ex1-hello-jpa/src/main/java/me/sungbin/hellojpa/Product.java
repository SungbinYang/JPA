package me.sungbin.hellojpa;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Product
 * author : rovert
 * date : 2022/04/14
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/14       rovert         최초 생성
 */

@Entity
public class Product {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "product")
    private List<MemberProduct> memberProducts = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
