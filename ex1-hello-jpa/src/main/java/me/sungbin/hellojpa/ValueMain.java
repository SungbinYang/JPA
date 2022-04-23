package me.sungbin.hellojpa;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : ValueMain
 * author : rovert
 * date : 2022/04/20
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/04/20       rovert         최초 생성
 */

public class ValueMain {
    public static void main(String[] args) {
        int a = 10;
        int b = 10;

        System.out.println("a == b : " + (a == b));

        Address address1 = new Address("경기도", "시흥시", "군자동");
        Address address2 = new Address("경기도", "시흥시", "군자동");

        System.out.println("address1 == address2 : " + (address1 == address2));
        System.out.println("address1 equals address2 : " + address1.equals(address2));

    }
}
