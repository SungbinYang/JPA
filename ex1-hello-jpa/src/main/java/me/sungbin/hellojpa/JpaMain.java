package me.sungbin.hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Set;

/**
 * packageName : me.sungbin.hellojpa
 * fileName : JpaMain
 * author : rovert
 * date : 2022/03/28
 * description :
 * ===========================================================
 * DATE 			AUTHOR			 NOTE
 * -----------------------------------------------------------
 * 2022/03/28       rovert         최초 생성
 */

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hello");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        try {
            Member member = new Member();
            member.setName("member1");
            member.setHomeAddress(new Address("경기도", "시흥시", "군자동"));

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("족발");
            member.getFavoriteFoods().add("피자");

            member.getAddressHistory().add(new AddressEntity("서울시", "시흥시", "군자동"));
            member.getAddressHistory().add(new AddressEntity("서울시", "성북구", "군자동"));

            entityManager.persist(member);

            entityManager.flush();
            entityManager.clear();

            System.out.println("============ START ===========");
            Member findMember = entityManager.find(Member.class, member.getId());

//            List<AddressEntity> addressHistory = findMember.getAddressHistory();
//
//            for (AddressEntity address : addressHistory) {
//                System.out.println("address.getCity() = " + address.getAddress().getCity());
//            }
//
//            Set<String> favoriteFoods = findMember.getFavoriteFoods();
//
//            for (String favoriteFood : favoriteFoods) {
//                System.out.println("favoriteFood = " + favoriteFood);
//            }
//
//            System.out.println("=============== UPDATE ==============");
//
////            findMember.setHomeAddress(new Address("경기도", "성남시", "판교"));
////
////            // 치킨 -> 한식
////            findMember.getFavoriteFoods().remove("치킨");
////            findMember.getFavoriteFoods().add("한식");
//
//            System.out.println("=============== ADDRESS ==============");
//            findMember.getAddressHistory().remove(new AddressEntity("서울시", "성북구", "군자동"));
//            findMember.getAddressHistory().add(new AddressEntity("서울시", "성북구", "국민대"));

            transaction.commit(); // 이때 DB에 쿼리를 날린다.
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
        }

        entityManagerFactory.close();
    }
}
