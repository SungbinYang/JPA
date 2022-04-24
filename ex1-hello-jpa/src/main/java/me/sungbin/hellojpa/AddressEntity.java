package me.sungbin.hellojpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : AddressEntity
 * author : rovert
 * date : 2022/04/24
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/24       rovert         최초 생성
 */

@Entity
@Table(name = "ADDRESS")
public class AddressEntity {

    @Id @GeneratedValue
    private Long id;

    private Address address;

    public AddressEntity(String city, String street, String zipcode) {
        this.address = new Address(city, street, zipcode);
    }

    public AddressEntity(Address address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
