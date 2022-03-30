> 참고
* [김영한님 인프런 강의 - 자바 ORM 표준 JPA 프로그래밍 - 기본편](https://www.inflearn.com/course/ORM-JPA-Basic/dashboard)
* [자바 ORM 표준 JPA 프로그래밍](http://www.yes24.com/Product/Goods/19040233)

## JPA가 무엇인가?
- 순수 JDBC 등록
  과거에는 객체를 DB에 저장하려면 아래처럼 복잡한 JDBC API와 SQL을 한땀한땀 작성해야한다.

```java
//글쓰기 메소드
	public int write(String title, String userID, String content) {
		String sql = "insert into board values(?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, getNext());
			pstmt.setString(2, title);
			pstmt.setString(3, userID);
			pstmt.setString(4, getDate());
			pstmt.setString(5, content);
			pstmt.setInt(6, 1); //글의 유효번호
			return pstmt.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return -1; //데이터베이스 오류
	}
```

- JDBC Template, MyBatis
  그 후에 위의 코드의 복잡성을 줄이고자 SQL Mapper라는것이 나옴으로 개발코드는 줄었다.
  하지만 SQL을 문자열로 type safe하지 않게 일일이 적어야했다.

``` java
@Repository
public class BoardDao {
	private final String LIST_BOARDS = "select seq, title, left(regdate,16) regdate, id, writer, cnt from board order by seq desc limit ?, ?";
	private final String SEARCH_BOARDS = "select seq, title, left(regdate,16) regdate, id, writer, cnt from board "
			+ "where match(title) against(:keyword) or match(content) against(:keyword) order by seq desc limit :offset, :count";

/**
	 * 게시글 목록 조회
	 * 
	 * @param page  페이지
	 * @param count 갯수
*/
public List<Board> listBoards(Search search) {
		return jdbcTemplate.query(LIST_BOARDS, BOARD_ROW_MAPPER,
				search.getOffset(), search.getCount());
	}
}
```

- JPA
1. JPA가 나옴으로 개발생산성 향상 및 개발 속도 및 유지 보수 측면에서 많이 뛰어났다.
2. JPA가 나온 이후 SQL문 조차도 작성할 필요가 없어졌다.
3. 단, 학습곡선이 너무 높다.

``` java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    private String title; // 제목

    private String content; // 내용

    private String writer; // 작성자

    private int hits; // 조회 수
}
```

- JPA 실무에서 어려운 이유?
1. 객체와 테이블을 딱 올바르게 매팡하고 설계하는 방법을 잘 모른다.
2. 예제와 달리 실무에서는 수십개의 복잡한 객체와 테이블로 구성되어 있다.
3. JPA의 내부 동작 방식을 이해하지 못하고 사용
4. JPA가 어떤 SQL을 만들어내고 언제 만들어내는지를 이해가 힘듬

## 마무리

> 첫 강의를 듣고나서 많이 깨달은 점들이 있었다. 현재, 나는 JPA가 뭔지는 알지만 정확하게
어떻게 동작하는지를 파악을 못하였다. 그 점에 대한 반성을 하면서 이번 강의를 통해 어떻게
JPA가 동작하는지를 알고 상태관리등 많은것을 학습하고 내것으로 해야겠다는 다짐을 하게된
계기가 되었다.

## 도입
과거에 우리의 애플리케이션은 객체지향언어(Java, Scala, ...)로 만들어져있었고, 그렇게 많이들 운영을 해왔다.
그리고 DB쪽에서는 RDB(MySQL, Oracle, MariaDB, PostgreSQL), NoSQL, File등
여러가지가 있었지만 그중에서 RDB를 사용자들이 많이 사용하기 시작했다. 그리고 어느덧 데이터를 저장해서
운영하는 애플리케이션이 트렌트가 되게 되었고 우리 객체를 DB에 저장하자는 생각이 나오게 되었다.
그리고 현재는 이런 코드로 짠 객체를 RDB에 관리를 하는 시대가 오게 되었다. 하지만 여기서 문제사항이 발생하게 되었다. 바로, [앞전 포스트](https://velog.io/@roberts/JPA-%EA%B0%9C%EC%9A%94)에서 이야기 했듯이 우리가 SQL을 일일이 type safe하지 못하게 적어야 하는 문제사항이 발생하게 되었다.

## SQL 중심적인 개발의 문제점
- 무한 반복, 지루한 코드
  - CRUD
  - INSERT INTO...
  - UPDATE...
  - SELECT...
  - DELETE...
  - 자바 객체를 SQL로..
  - SQL을 자바 객체로...
- 객체 CRUD

``` java
public class Member {
  private String memberId;
  private String name;
   ...
}
```

``` sql
INSERT INTO MEMBER(MEMBER_ID, NAME) VALUES 
SELECT MEMBER_ID, NAME FROM MEMBER M
UPDATE MEMBER SET ...
```

- 객체 CRUD 필드 추가시,
  우리가 위와 같이 객체를 만들고 쿼리도 작성을 하였다. 하지만 갑자기 PM분이
  컬럼을 추가해달라는 상황이 올때 어떻게 대처를 해야하는가?
  당연히, 자바에 속성을 추가해주고 작성했던 sql문에 추가된 속성을 넣어줘야 하는 상황이 발생한다.
  결국, 객체지향적으로 코드를 작성할 시, SQL에 의존적인 개발을 피하기는 어렵다.

## 패러다임의 불일치 (객체 vs RDB)
그러면 여기서 잠시 들수 있는 생각이, SQL에 의존적인 개발을 하지말고, 그냥 절차지향으로 짜면 안될까?
상관은 없지만, 객체지향의 수 많은 이점을 포기하게되는 셈이다.

>  ‘객체 지향 프로그래밍은 추상화, 캡슐화, 정보은닉, 상속, 다형성 등 시스템의 복잡성을 제어할 수 있는
다양한 장치들을 제공한다.
form. 어느 객체지향 개발자

그럼 객체지향은 유지하되, RDB를 사용하지 말고 다른 DB를 사용하면 어떨까?

![](https://images.velog.io/images/roberts/post/e4c27769-5f78-4bd2-a61d-b5c3598e3f64/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%206.03.16.png)

위의 이미지처럼 객체를 영구보관하는 DB는 RDB를 말고도 다른 DB들도 있지만, 결론은 RDB가 우리가 사용하기에 편하고
다른거에 비해 쉽게 사용도 가능하다.

> ex. File로 예를 들면 수많은 객체를 File로 저장하면 용량이 어마어마해지며 감당이 안될것 같다.

결국, 현실적인 대안은 관계형 데이터베이스이다.

그럼 객체를 관계형 데이터베이스에 저장을 할려면 어떤 공정이 들어갈까?

![](https://images.velog.io/images/roberts/post/740e9c64-9f25-4219-ba4d-6ba980efb3ba/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%206.06.42.png)

위의 사진처럼 객체를 SQL로 변환하는 converter가 존재해야한다. 그럼 이 converter는 누가 해주는가?
바로 SQL Mapper나 개발자 본인이 직접해야한다. 정말 어마무시한 공정일것이다.

그럼 어떤 차이점이 있길래 객체를 SQL로 변환작업을 해줘야하는가?
* 상속
* 연관관계
* 데이터 타입
* 데이터 식별 방법

위와 같은 곳에서 차이점을 발견할 수 있다.

### 상속

상속같은 경우에는 아래 그림처럼 객체는 상속관계가 존재하지만 SQL은 그 비스무리한 것이 존재하지만
너무 비효율적인 부분으로 없다고해도 무방할것이다.

![](https://images.velog.io/images/roberts/post/0349d099-dd43-4f2f-8515-a039ffb7c35e/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%206.10.48.png)

예를 들면 Book을 저장하려고 하면, Insert query문을 item테이블과 book테이블 2군데에 다 해줘야하는 비효율적인 부분이 있다. 또한, book 조회시, 각각의 테이블에 따른 조인 SQL문을 작성해야하며, 각각의 객체를 생성해서 그 데이터들을 넣어줘야한다. 즉, 이래서 DB에 저장할 객체에는 상속관계를 쓰지 않는다.

자바 컬렉션에 저장 및 조회할 경우는 어떤가?

``` java
Book book = list.get(bookId);

Item item = list.get(bookId);
```

위처럼 간단하게 사용이 가능하며, 부모타입으로 조회 후, 다형성까지 활용이 가능하다.

### 연관관계
- 객체는 참조를 사용 : ``` member.getTeam(); (단방향 참조) ```
- 테이블은 외래 키를 사용 ``` JOIN ON M.TEAM_ID = T.TEAM_ID (양방향 참조) ```

![](https://images.velog.io/images/roberts/post/d287bbfc-d5fa-4e74-ac57-e76008166781/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%206.17.25.png)

## 모델링
우리가 일반적인 모델링을 할 때, DB 테이블을 보고 모델링을 진행한다. 예시로는 아래와 같다.

``` java
class Member {
  String id; //MEMBER_ID 컬럼 사용
  Long teamId; //TEAM_ID FK 컬럼 사용 //**
  String username;//USERNAME 컬럼 사용
}
```

``` java
class Team {
  Long id; //TEAM_ID PK 사용
  String name; //NAME 컬럼 사용
}
```

여기서 뭔가 좀 더 객체다워질려면

``` java
class Member {
  String id; //MEMBER_ID 컬럼 사용
  Team team; // 참조로 연관관계를 맺는다.//**
  String username;//USERNAME 컬럼 사용
  
  Team getTema() {
  	return team;
  }
}
```

``` java
class Team {
  Long id; //TEAM_ID PK 사용
  String name; //NAME 컬럼 사용
}
```

여기서 SQL로 변환할때 Team부분은 member.getTeam().getId()로 team_id를 찾아 변환해서 넣어야한다.

여기서 조회를 할 때, 다음과 같이 이루어질수 있다.

```sql
SELECT M.*, T.* FROM MEMBER M
JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
```

```java
public Member find(String memberId) {
  //SQL 실행 ...
  Member member = new Member();
  //데이터베이스에서 조회한 회원 관련 정보를 모두 입력
  Team team = new Team();
  //데이터베이스에서 조회한 팀 관련 정보를 모두 입력
  //회원과 팀 관계 설정
  member.setTeam(team); //**
  return member;
}
```

### 객체 그래프 탐색
객체는 자유롭게 객체 그래프를 탐색할 수 있어야 한다.

![](https://images.velog.io/images/roberts/post/065e76b2-ba63-43d2-bde9-50d7a197a9c0/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%206.24.21.png)

탐색이라는 의미는 객체의 체이닝메소드로 접근이 가능해야한다.

하지만 처음 실행하는 SQL에 따라 탐색 범위가 결정된다.

``` sql
SELECT M.*, T.* FROM MEMBER M
JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
```

```java
member.getTeam(); //OK 
member.getOrder(); //null
```

여기서 member.getOrder()가 null인 이유는 sql 조회할때 order 테이블은 조회하지 않았기 때문이다.

또한 엔티티 신뢰문제도 발생할 수 있다.

``` java
class MemberService {
...
  public void process() {
  Member member = memberDAO.find(memberId);
  member.getTeam(); //???
  member.getOrder().getDelivery(); // ???
  }
}
```

여기서 membetDAO.find() 메소드에서 어떤 쿼리가 발생했는지 확인해봐야 하는 귀찮음이 있다.
그래서 getTeam(), getOrder().getDelicery()가 null인지 아닌지를 이 코드만 보고 확인할 수 없다.

그러면 아예 모든 객체를 미리 로딩하면 안될까?

먼저 정답은 그럴수 없다. 상황에 따라 동일한 회원조회 메서드를 여러벌 생성하는 방법이 유일하다.

```java
memberDAO.getMember(); //Member만 조회 

memberDAO.getMemberWithTeam();//Member와 Team 조회
 
//Member,Order,Delivery
memberDAO.getMemberWithOrderWithDelivery();
```

즉, 진정한 의미의 계층 분할은 어렵다. 또한, 객체답게 모델링 할수록 매핑 작업만 늘어난다.
그럼 여기서 이 문제들을 해결할수 없을까?
또한 객체를 자바 컬렉션에 저장 하듯이 DB에 저장할 수는 없을까?

해결방안은 JPA가 키를 가지고 있다. JPA에 관련된 사항은 다음 포스트에 게시하도록 하겠다.

## 마무리
> 나는 이 강의 듣기 전에 JPA사용방법에 대해서만 아는데 급급했던것 같았는데 이래서 JPA가 도입이 되었고 왜 필요했는지를 이 강의를 듣고 많은 깨달음을 얻게 된 계기가 되었다.

## JPA?
- Java Persistence API
- 자바 진영의 **ORM** 기술 표준

## ORM?
- Object-relational mapping (객체 관계 매핑)
- 객체는 객체대로 설계
- 관계형 데이터베이스는 관계형 데이터베이스대로 설계
- ORM 프레임워크가 중간에서 매핑
- 대중적인 언어에는 대부분 ORM 기술이 존재
- ex) TypeORM..

## JPA는 애플리케이션과 JDBC 사이에서 동작

![](https://images.velog.io/images/roberts/post/5aaf9bb9-2fe2-45b7-bd55-dbee24ac88e7/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%208.03.00.png)

- JPA 동작 - 저장
  * Entity 분석
  * INSERT SQL 생성
  * JDBC API를 사용하여 DB에 SQL 보냄
  * 패러다임 불일치 해결
- JPA 동작 - 조회
  * DAO쪽에서 pk값을 넘김
  * JPA가 SELECT SQL 생성
  * JDBC API 사용
  * DB쪽에 SQL 보냄
  * DB쪽에서 결과를 반환하면 ResultSet 매핑
  * 그 ResultSet Entity Object를 DAO에 넘김
  * 패러다임 불일치 해결
## JPA 소개

![](https://images.velog.io/images/roberts/post/c6c79bd6-f037-4c33-90b1-4f9a208503b7/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%208.07.47.png)

과거에도 ORM기술은 있었다. 바로 EJB라는 것인데, 문제점은 너무 아마추어적이었다.
인터페이스 구현도 많고 느리고 기본동작에도 오류가 많았었다. 그래서 어느 한 SI 개발자가
화가나서 만든 오픈소스가 하이버네이트고 이 하이버네이트를 기반으로 그대로 받아들여서 만든 자바표준이 JPA다.
또한 EJB에 이상한 컨테이너도 있었는데 너무 복잡하여 이것을 좀더 간편하게 오픈소스를 시킨 개발자가 있었는데 그 개발자 이름은 Spring의 창시자 로드존슨이고 그 컨테이너가 Spring이 되었다.

> 여담이지만, EJB덕분에 JPA와 Spring이 생겼으니 감사히 여겨야 할것 같다. :)

## JPA는 표준 명세
- JPA는 인테페이스의 모음
- JPA 2.1 표준 명세를 구현한 3가지 구현체
- Hibernate, EclipseLink, DataNucleus

![](https://images.velog.io/images/roberts/post/626cbd24-f661-4160-94ee-a32735795b7e/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-27%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%208.12.00.png)

## JPA 버전
- JPA 1.0(JSR 220) 2006년 : 초기 버전. 복합 키와 연관관계 기능이 부족
- JPA 2.0(JSR 317) 2009년 : 대부분의 ORM 기능을 포함, JPA Criteria 추가
- JPA 2.1(JSR 338) 2013년 : 스토어드 프로시저 접근, 컨버터(Converter), 엔티 티 그래프 기능이 추가

## 왜 JPA를 사용해야 하는가?
- SQL 중심적인 개발에서 객체 중심으로 개발
- 생산성
- 유지보수
- 패러다임의 불일치 해결
- 성능
- 데이터 접근 추상화와 벤더 독립성
- 표준

### 생산성
- 저장: jpa.persist(member)
- 조회: Member member = jpa.find(memberId)
- 수정: member.setName(“변경할 이름”)
- 삭제: jpa.remove(member)

### 유지보수
- 기존: 필드 변경시 모든 SQL 수정
- JPA: 필드만 추가하면 되며, SQL은 JPA가 처리한다.

### JPA와 패러다임의 불일치 해결
- JPA와 상속 - 저장
  * 개발자: jpa.persist(book);
- JPA와 상속 - 조회
  * 개발자: Book book = jpa.find(Book.class, bookId);
### JPA와 연관관계, 객체 그래프 탐색
- 연관관계 저장

``` java
member.setTeam(team);
jpa.persist(member);
```

- 객체 그래프 탐색

``` java
Member member = jpa.find(Member.class, memberId);
Team team = member.getTeam(); 
```

### 신뢰할수 있는 엔티티, 계층

``` java
class MemberService {
...
  public void process() {
    Member member = memberDAO.find(memberId);
    member.getTeam(); //자유로운 객체 그래프 탐색
    member.getOrder().getDelivery();
    }
  }
```

### JPA와 비교하기
- 동일한 트랜잭션에서 조회한 엔티티는 같음을 보장

## JPA의 성능 최적화 기능
- 1차 캐시와 동일성(identity) 보장
  * 같은 트랜잭션 안에서는 같은 엔티티를 반환 - 약간의 조회 성능 향상
  * DB Isolation Level이 Read Commit이어도 애플리케이션에서 Repeatable Read 보장

``` java
String memberId = "100";
Member m1 = jpa.find(Member.class, memberId); //SQL
Member m2 = jpa.find(Member.class, memberId); //캐시
println(m1 == m2) //true
```
- 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)
  * 트랜잭션을 커밋할 때까지 INSERT SQL을 모음
  * JDBC BATCH SQL 기능을 사용해서 한번에 SQL 전송
  * UPDATE, DELETE로 인한 로우(ROW)락 시간 최소화
  * 트랜잭션 커밋 시 UPDATE, DELETE SQL 실행하고, 바로 커밋

``` java
transaction.begin(); // [트랜잭션] 시작
 
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);
//여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.
//커밋하는 순간 데이터베이스에 INSERT SQL을 모아서 보낸다.
transaction.commit(); // [트랜잭션] 커밋
```

``` java
transaction.begin(); // [트랜잭션] 시작
 
changeMember(memberA);
deleteMember(memberB);
비즈니스_로직_수행(); //비즈니스 로직 수행 동안 DB 로우 락이 걸리지 않는다.
//커밋하는 순간 데이터베이스에 UPDATE, DELETE SQL을 보낸다.
transaction.commit(); // [트랜잭션] 커밋
```

- 지연 로딩(Lazy Loading)
  * 지연로딩:객체가실제사용될때로딩
  * 즉시 로딩: JOIN SQL로 한번에 연관된 객체까지 미리 조회

> ORM은 객체와 RDB 두 기둥위에 있는 기술이다.

## 마무리

> JPA를 쓰면 뭐가 편해지고 어떤 기능들이 있는지 많은 것을 느꼈다. 조금은 많은 양이여서
내용이 벅차긴 했지만 하나하나 다시 새긴다는 마음으로 공부를 해나가야겠다.

## H2 데이터베이스 설치와 실행
- http://www.h2database.com/
- 최고의 실습용 DB
- 가볍다.(1.5M)
- 웹용 쿼리툴 제공
- MySQL, Oracle 데이터베이스 시뮬레이션 기능
- 시퀀스, AUTO INCREMENT 기능 지원

## 메이븐 소개
- https://maven.apache.org/
- 자바 라이브러리, 빌드 관리
- 라이브러리 자동 다운로드 및 의존성 관리
- 최근에는 그래들(Gradle)이 점점 유명

## 프로젝트 생성
- 자바 8 이상(17 권장)
- 메이븐 설정
  * groupId: 패키지 이름
  * artifactId: ex1-hello-jpa
  * version: 1.0.0
- 라이브러리 추가 - pom.xml

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>me.sungbin</groupId>
    <artifactId>ex1-hello-jpa</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>5.6.7.Final</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.4.200</version>
        </dependency>
    </dependencies>

</project>
```

## JPA 설정하기 - persistence.xml
- JPA 설정 파일
- resources/META-INF/persistence.xml 위치
- persistence-unit name으로 이름 지정 :: 그냥 이름이라 생각하자!
- javax.persistence로 시작: JPA 표준 속성
- hibernate로 시작: 하이버네이트 전용 속성
- persistence.xml

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
<persistence-unit name="hello">
<properties>
<!-- 필수 속성 -->
<property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
<property name="javax.persistence.jdbc.user" value="sa"/>
<property name="javax.persistence.jdbc.password" value=""/>
<property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
<property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
<!-- 옵션 -->
<property name="hibernate.show_sql" value="true"/>
<property name="hibernate.format_sql" value="true"/>
<property name="hibernate.use_sql_comments" value="true"/>
<!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
</properties>
</persistence-unit>
</persistence>
```

## 데이터베이스 방언
- JPA는 특정 데이터베이스에 종속 X (Dialect를 이용하여 proxy pattern 이용?)
- 각각의 데이터베이스가 제공하는 SQL 문법과 함수는 조금씩 다름
  * 가변 문자: MySQL은 VARCHAR, Oracle은 VARCHAR2
  * 문자열을 자르는 함수: SQL 표준은 SUBSTRING(), Oracle은 SUBSTR()
  * 페이징: MySQL은 LIMIT , Oracle은 ROWNUM
- 방언: SQL 표준을 지키지 않는 특정 데이터베이스만의 고유한 기능

![](https://images.velog.io/images/roberts/post/7d3864f8-ec59-4ff9-8dfe-8777667051df/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-28%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%2010.16.19.png)

- hibernate.dialect 속성에 지정
  * H2 : org.hibernate.dialect.H2Dialect
  * Oracle 10g : org.hibernate.dialect.Oracle10gDialect
  * MySQL : org.hibernate.dialect.MySQL5InnoDBDialect
- 하이버네이트는 40가지 이상의 데이터베이스 방언 지원

## JPA 구동 방식

![](https://images.velog.io/images/roberts/post/d05a451f-834f-470e-a64c-5661304899d3/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-28%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%2011.19.46.png)

## 객체와 테이블을 생성하고 매핑하기
- @Entity: JPA가 관리할 객체
- @Id: 데이터베이스 PK와 매핑

``` java
@Entity
public class Member {

    @Id
    private Long id;

    private String name;

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
}
```

``` sql
reate table Member (
	 id bigint not null,
 	name varchar(255),
 	primary key (id)
);
```

## ⚠️ 주의
- 엔티티 매니저 팩토리는 하나만 생성해서 애플리케이션 전체에 서 공유
- 엔티티 매니저는 쓰레드간에 공유X (사용하고 버려야 한다).
- **JPA의 모든 데이터 변경은 트랜잭션 안에서 실행**

## JPQL
- JPA를 사용하면 엔티티 객체를 중심으로 개발
- 문제는 검색 쿼리
- 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검 색 조건이 포함된 SQL이 필요
- JPA는 SQL을 추상화한 JPQL이라는 객체지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 데이터베이스 테이블을 대상으로 쿼리
- 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X
- JPQL을 한마디로 정의하면 객체 지향 SQL

## 후기
> 이제까지 JPA라는 신셰계에 JPQL이라는 기능까지 알아보았다. 처음에는 xml이라는것으로 설정하는것이
번거로워서 실무에선 이 xml 설정파일이 얼마나 복잡할까 생각을 했다. 또한 entitymanager로 만든 객체를
기능 당 try ~ catch로 사용할려면 얼마나 코드가 길어질까 생각을 해서 구글링을 통해 어떻게 개발을 하는지
찾아보니 spring data jpa라는것이 알아서 다 해주는것 같았다. 이 부분은 나중에 학습을하고 지금은 jpa가 어떻게
동작하는지를 중점으로 좀 더 공부를 해야겠다.

## JPA에서 가장 중요한 2가지
- 객체와 관계형 데이터베이스 매핑하기 (Object Relational Mapping)
- **영속성 컨텍스트**

![](https://images.velog.io/images/roberts/post/edd137e8-a2dc-405f-a173-abc4e6616f8e/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%209.22.08.png)

## 영속성 컨텍스트
- JPA를 이해하는데 가장 중요한 용어
- “엔티티를 영구 저장하는 환경”이라는 뜻
- EntityManager.persist(entity);
  * 사실 DB에 저장하는것이 아니라 영속성 컨텍스트를 통해 엔티티를 영속화한다.
  * DB에 저장하는것이 아니라 엔테테를 영속성 컨텍스트라는데 저장한다.
- 영속성 컨텍스트는 논리적인 개념
- 눈에 보이지 않는다.
- 엔티티 매니저를 통해서 영속성 컨텍스트에 접근
  * 엔티티 매니저 생성 시, 그 안에 영속성 컨텍스트 1:1 관계 생성

## J2SE 환경
- 엔티티 매니저와 영속성 컨텍스트가 1:1 관계

![](https://images.velog.io/images/roberts/post/57b3f27f-b2b9-4d7f-8ca9-af8d1b1da4d2/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%209.25.53.png)

## 엔티티의 생명주기
- 비영속 (new/transient)
  * 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태
- 영속 (managed)
  * 영속성 컨텍스트에 관리되는 상태
- 준영속 (detached)
  * 영속성 컨텍스트에 저장되었다가 분리된 상태
- 삭제 (removed)
  * 삭제된 상태

![](https://images.velog.io/images/roberts/post/71663f11-5c61-4330-a6ae-5064169f9760/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%209.28.34.png)

## 비영속
- 객체를 생성한 상태

![](https://images.velog.io/images/roberts/post/a7a004f9-b047-4a7b-9736-b35fb27f97d1/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%209.29.37.png)

``` java
//객체를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");
```

## 영속

![](https://images.velog.io/images/roberts/post/8da19752-fdc1-47e5-a338-bbcd1d076137/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%209.30.29.png)

``` java
//객체를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername(“회원1”);
EntityManager em = emf.createEntityManager();
em.getTransaction().begin();
 //객체를 저장한 상태(영속)
em.persist(member);
```

## 준영속, 삭제

``` java
 //회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
 em.detach(member);
 
  //객체를 삭제한 상태(삭제)
 em.remove(member);
```

## 영속성 컨텍스트 이점
- 1차 캐시
- 동일성(identity) 보장
- 트랜잭션을 지원하는 쓰기 지연
- 변경 감지(Dirty Checking)
- 지연 로딩(Lazy Loading)

## 후기
> 처음에 단순히 persist만 하면 무조건 저장되는줄 알았던 개념을 다시 잡게되어 좋은 시간이었다.
또한 상태관리는 들어봤지만 어느게 어느 상태인지 살짝 분간이 안 간 상태여서 그 개념들을 조금 다시
잡는 시간이 되었다.

## 엔티티 조회, 1차 캐시

![](https://images.velog.io/images/roberts/post/87d38632-46c2-414a-9e3c-e39b69162a59/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%2010.26.44.png)

``` java
//엔티티를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");
//엔티티를 영속
em.persist(member); 
```

- 1차 캐시에서 조회

``` java
 Member member = new Member();
 member.setId("member1");
 member.setUsername("회원1");
 //1차 캐시에 저장됨
 em.persist(member);
 //1차 캐시에서 조회
 Member findMember = em.find(Member.class, "member1");
```

![](https://images.velog.io/images/roberts/post/df2c6907-f1a8-42e9-b6ba-b5d79cdb3ded/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%2010.28.28.png)

- 데이터베이스에서 조회

``` java
Member findMember2 = em.find(Member.class, "member2");
```

![](https://images.velog.io/images/roberts/post/5de476a5-4845-4932-ae15-d0b191889b4b/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%2010.29.37.png)

## 영속 엔티티의 동일성 보장

``` java
Member a = em.find(Member.class, "member1");
Member b = em.find(Member.class, "member1");
System.out.println(a == b); //동일성 비교 true
```

- 1차 캐시로 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭 션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공

## 엔티티 등록 트랜잭션을 지원하는 쓰기 지연

``` java
EntityManager em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
//엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다.
transaction.begin(); // [트랜잭션] 시작
em.persist(memberA);
em.persist(memberB);
//여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.
//커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다.
transaction.commit(); // [트랜잭션] 커밋
```

![](https://images.velog.io/images/roberts/post/a823aab1-51d9-48ba-b3cb-a117f9399a71/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%2010.31.25.png)

![](https://images.velog.io/images/roberts/post/970fb0d7-7dfa-445f-ab79-c754202fde36/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA%202022-03-29%20%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE%2010.31.59.png)

## 엔티티 수정 (Dirty Checking)

``` java
EntityManager em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
transaction.begin(); // [트랜잭션] 시작
// 영속 엔티티 조회
Member memberA = em.find(Member.class, "memberA");
// 영속 엔티티 데이터 수정
memberA.setUsername("hi");
memberA.setAge(10);
//em.update(member) 이런 코드가 있어야 하지 않을까?
transaction.commit(); // [트랜잭션] 커밋
```

- 왜 수정 쿼리시, persist를 호출하지 않을까?
  * JPA의 dirty checking 때문이다.
  * 비밀은 영속성 컨텍스트 안에 있다.
  * 트랜젝션 커밋 시에, flush()가 호출되는데 그 후에, 엔티티와 스냅샷을 비교한다.
  * 여기서, 스냅샷은 최초로 읽어온 시점, 그 시점을 스냅샷으로 떠온다.
  * 일일이 비교 후, 바뀐 부분을 update query 생성 후, 쓰기 지연 SQL 저장소에 넣어둔다.
  * DB 반영 후 Commit

## 엔티티 삭제

``` java
//삭제 대상 엔티티 조회
Member memberA = em.find(Member.class, “memberA");
em.remove(memberA); //엔티티 삭제
```

## 후기
> 영속성 컨텍스트의 하이라이트 엔티티 수정에서 발하는것 같다.
수정 부분을 보면서 persist()를 따로 호출도 안했는데 쿼리가 날라가고 반영이 되었다는게
많이 신기하였다.

## 플러시
- 영속성 컨텍스트의 변경내용을 데이터베이스에 반영
- 보통 DB 트랜잭션이 커밋될때, flush 발생
- DB와 영속성 컨텍스트의 상태를 같게한다.

## 플러시 발생
- 변경 감지
- 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
- 쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송 (등록, 수정, 삭제 쿼리)
- flush가 발생되면 1차캐시는 날라가지 않고 유지된다.

## 영속성 컨텍스트를 플러시 하는 방법
- em.flush() - 직접 호출
- 트랜잭션 커밋 - 플러시 자동 호출
- JPQL 쿼리 실행 - 플러시 자동 호출

## JPQL 쿼리 실행 시, 플러시가 자동으로 호출되는 이유

``` java
 em.persist(memberA);
em.persist(memberB);
em.persist(memberC);
//중간에 JPQL 실행
query = em.createQuery("select m from Member m", Member.class);
List<Member> members= query.getResultList();
```

- 위의 코드처럼 작성될때, 플러시가 자동으로 호출 안되면 쿼리결과는 비어있을것이다.

## 플러시 모드 옵션

``` java
em.setFlushMode(FlushModeType.COMMIT)
```

- FlushModeType.AUTO : 커밋이나 쿼리를 실행할 때, 플러시 (기본 값)
- FlushModeType.COMMIT : 커밋할때만 플러시

## 결론
- 플러시는 영속성 컨텍스트를 비우지 않음
- 영속성 컨텍스트의 변경내용을 데이터베이스에 동기화
- 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화 하면 됨

## 준영속 상태
- 영속 -> 준영속
- 영속 상태의 엔티티가 영속성 컨텍스트에서 분리(detached)
- 영속성 컨텍스트가 제공하는 기능을 사용 못함

## 준영속 상태로 만드는 방법
- em.detach(entity) : 특정 엔티티만 준영속 상태로 전환
- em.clear() : 영속성 컨텍스트를 완전히 초기화
- em.close() : 영속성 컨텍스트를 종료

## 엔티티 매핑 소개
- 객체와 테이블 매핑 : @Entity, @Table
- 필드와 컬럼 매핑 : @Column
- 기본 키 매핑 : @Id
- 연관관계 매핑 : @ManyToOne, @JoinColumn

## @Entity
- @Entity가 붙은 클래스는 JPA가 관리, 엔티티라 한다.
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 필수
- 주의
  * 기본 생성자 필수 (파라미터가 없는 public 또는 protected 생성자)
    * JPA가 동적으로 리플렉션이나 다양한 기술들을써서 proxing하기 위해서 기본생성자 필수
    * 일단은 그냥 스펙상이다라고 생각!
  * final 클래스, enum, interface, inner 클래스 사용 x
  * 저장할 필드에 final 사용 x

## @Entity 속성 정리
- 속성 : name
  * JPA에서 사용할 엔티티 이름을 지정한다.
  * 기본값: 클래스 이름을 그대로 사용 (ex: Member)
  * 같은 클래스 이름이 없으면 가급적 기본값을 사용한다.

## @Table
- @Table은 엔티티와 매핑할 테이블 지정

|속성|기능|기본값|
|------|---|---|
|name|매핑할 테이블 이름|엔티티 이름을 사용|
|catalog|데이터베이스 catalog 매핑||
|schema|데이터베이스 schema 매핑||
|uniqueConstraints (DDL)|DDL 생성 시에 유니크 제약 조건 생성||

## 데이터베이스 스키마 자동 생성
- DDL을 애플리케이션 실행 시점에 자동 생성
- 테이블 중심 -> 객체 중심
- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
- 이렇게 생성된 DDL은 개발 장비에서만 사용
- 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용

## 데이터베이스 스키마 자동 생성 - 속성
- hibernate.hbm2ddl.auto

|옵션|설명|
|------|---|
|create|기존테이블 삭제 후 다시 생성 (DROP + CREATE)|
|create-drop|create와 같으나 종료시점에 테이블 DROP|
|update|변경분만 반영(운영DB에는 사용하면 안됨)|
|validate|엔티티와 테이블이 정상 매핑되었는지만 확인|
|none|사용하지 않음|

## 데이터베이스 스키마 자동 생성 - 주의
- 운영 장비에는 절대 create, create-drop, update 사용하면 안된다.
- 개발 초기 단계는 create 또는 update
- 테스트 서버는 update 또는 validate
- 스테이징과 운영 서버는 validate 또는 none

## DDL 생성 기능
- 제약조건 추가: 회원 이름은 필수, 10자 초과X
  * @Column(nullable = false, length = 10)
- 유니크 제약조건 추가
  * @Table(uniqueConstraints = {@UniqueConstraint( name = "NAME_AGE_UNIQUE", columnNames = {"NAME", "AGE"} )})
- DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고 JPA의 실행 로직에는 영향을 주지 않는다.

## 후기
> 오늘 들은 수업에서 가장 큰 교훈은 가급적이면 테스트나 운영서버에서 validate를 쓰지 말고
아예 none 처리를 하자!
update도 잘못쓰다가 테이블 락이 걸려 서비스를 중단해야하는 경우도 있다.