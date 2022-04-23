package me.sungbin.hellojpa;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : Address
 * author : rovert
 * date : 2022/04/23
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/23       rovert         최초 생성
 */

@Embeddable
public class Address {

    private String city;

    private String street;

    @Column(name = "ZIPCODE")
    private String zipcode;

    public Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
