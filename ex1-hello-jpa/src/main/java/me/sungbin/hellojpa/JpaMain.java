package me.sungbin.hellojpa;

import org.hibernate.Hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

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
            Team team = new Team();
            team.setName("teamA");
            entityManager.persist(team);

            Team team2 = new Team();
            team2.setName("teamB");
            entityManager.persist(team2);

            Member member1 = new Member();
            member1.setName("member1");
            member1.setTeam(team);
            entityManager.persist(member1);

            Member member2 = new Member();
            member2.setName("member2");
            member2.setTeam(team2);
            entityManager.persist(member2);

            entityManager.flush();
            entityManager.clear();

//            Member m1 = entityManager.find(Member.class, member1.getId());

            // SQL: select * from Member;
            // SQL: select * from Team where TEAM_ID = xxx;
            List<Member> members = entityManager.createQuery("select m from Member m join fetch m.team", Member.class)
                    .getResultList();

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
