package me.sungbin.hellojpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Member
 * author : rovert
 * date : 2022/03/28
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/03/28       rovert         최초 생성
 */

@Entity
@Table(name = "MBR")
public class Member {

    @Id
    private Long id;

    private String name;

    public Member() {
    }

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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
