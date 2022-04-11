package me.sungbin.hellojpa;

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
            // 저장
            Team team = new Team();
            team.setName("TeamA");
//            team.getMembers().add(member); // 잘못된 연관관계 매핑 :: 주인쪽에 맺어줘야한다.
            entityManager.persist(team);

            Member member = new Member();
            member.setName("member1");
            member.setTeam(team);
            entityManager.persist(member);

            entityManager.flush();
            entityManager.clear();

            team.addMember(member);

            Team findTeam = entityManager.find(Team.class, team.getId()); // 1차 캐시
            List<Member> members = findTeam.getMembers();

            System.out.println("====================");
            for (Member m : members) {
                System.out.println("m.getName() = " + m.getName());
            }
            System.out.println("====================");

            transaction.commit(); // 이때 DB에 쿼리를 날린다.
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            entityManager.close();
        }

        entityManagerFactory.close();
    }
}
