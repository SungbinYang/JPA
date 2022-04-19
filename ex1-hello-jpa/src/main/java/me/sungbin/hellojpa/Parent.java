package me.sungbin.hellojpa;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Parent
 * author : rovert
 * date : 2022/04/19
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/19       rovert         최초 생성
 */

@Entity
public class Parent {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> childList = new ArrayList<>();

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

    public List<Child> getChildList() {
        return childList;
    }

    public void setChildList(List<Child> childList) {
        this.childList = childList;
    }

    public void addChild(Child child) {
        childList.add(child);
        child.setParent(this);
    }
}
