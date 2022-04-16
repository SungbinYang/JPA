package me.sungbin.hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
            Movie movie = new Movie();
            movie.setDirector("A");
            movie.setActor("BBB");
            movie.setName("Harry Potter");
            movie.setPrice(10000);

            entityManager.persist(movie);

            entityManager.flush();
            entityManager.clear();

            Item item = entityManager.find(Item.class, movie.getId());
            System.out.println("findMovie = " + item.getName());

            transaction.commit(); // 이때 DB에 쿼리를 날린다.
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            entityManager.close();
        }

        entityManagerFactory.close();
    }
}
