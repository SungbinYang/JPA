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
        int b = a;
        
        b = 20;

        System.out.println("a = " + a);
        System.out.println("b = " + b);

        Integer c = 10;
        Integer d = c;

        System.out.println("c = " + c);
        System.out.println("d = " + d);

    }
}
