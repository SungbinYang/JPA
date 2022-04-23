package me.sungbin.hellojpa;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

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

    public String getStreet() {
        return street;
    }

    public String getZipcode() {
        return zipcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipcode, address.zipcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, street, zipcode);
    }
}
