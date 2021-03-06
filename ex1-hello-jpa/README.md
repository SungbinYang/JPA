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

## 요구사항 추가
1. 회원은일반회원과관리자로구분해야한다.
2. 회원가입일과수정일이있어야한다.
3. 회원을설명할수있는필드가있어야한다.이필드는길이제 한이 없다.

``` java
@Entity
public class Member {

    @Id
    private Long id;

    @Column(name = "name")
    private String username;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob
    private String description;

    public Member() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
```

## 매핑 어노테이션 정리
- 테이블이나 엔티티 매핑은 별거 없어보이지만, 필드와 컬럼은 경우가 다양하다.
- hibernate.hbm2ddl.auto

|어노테이션|설명|
|------|---|
|@Column|컬럼 매핑|
|@Temporal|날짜 타입 매핑 (DATE, TIME, TIMESTAMP 이 중에 1개를 value값으로 지정해야한다.)|
|@Enumerated|enum 타입 매핑|
|@Lob|문자 타입형으로 선언한 경우 DB에 CLOB, 그 외엔 BLOB|
|@Transient|특정 필드를 컬럼에 매핑하지 않음(매핑 무시)|

## @Column

|속성|설명|기본 값|
|------|---|---|
|name|필드와 매핑할 테이블의 컬럼 이름|객체의 필드 이름|
|insertable,<br />updatable|등록, 변경 가능 여부, FALSE로 둘 시, 절대 등록, 수정 안됨|TRUE|
|nullable(DDL)|null 값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다. 자주 사용되는 속성!||
|unique(DDL)|자주 사용되지 않는다. 이유는 자동생성 쿼리에 unique 제약조건을 건 컬럼 이름이 난수값으로 바꿔주기 때문에 나중에 판별하기 힘들다. 그래서 @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제 약조건을 걸 때 사용한다. ||
|columnDefinition (DDL)|데이터베이스 컬럼 정보를 직접 줄 수 있다. <br /> ex) varchar(100) default 'EMPTY'|필드의 자바 타입과 방언 정보를 사용해|
|length(DDL)|문자 길이 제약조건, String 타입에만 사용한다.|255|
|precision, scale(DDL)|BigDecimal 타입에서 사용한다(BigInteger도 사용할 수 있다). precision은 소수점을 포함한 전체 자 릿수를, scale은 소수의 자릿수 다. 참고로 double, float 타입에는 적용되지 않는다. 아주 큰 숫자나 정 밀한 소수를 다루어야 할 때만 사용한다.|precision=19, scale=2|

## @Enumerated
- 자바 enum 타입을 매핑할 때 사용
- **주의⚠️ ORDINAL 사용X**

|속성|설명|기본 값|
|------|---|---|
|value|• EnumType.ORDINAL: enum 순서를 데이터베이스에 저장 <br />• EnumType.STRING: enum 이름을 데이터베이스에 저장|EnumType.ORDINAL|

## @Temporal
- 날짜 타입(java.util.Date, java.util.Calendar)을 매핑할 때 사용
- 참고: LocalDate, LocalDateTime을 사용할 때는 생략 가능(최신 하이버네이트 지원)

|속성|설명|기본 값|
|------|---|---|
|value|•TemporalType.DATE: 날짜, 데이터베이스 date 타입과 매핑<br />(예: 2013–10–11) <br />•TemporalType.TIME: 시간, 데이터베이스 time 타입과 매핑<br />(예: 11:11:11) <br /> •TemporalType.TIMESTAMP: 날짜와 시간, 데이터베이스 <br /> timestamp 타입과 매핑(예: 2013–10–11 11:11:11)||

## @Lob
- 데이터베이스 BLOB, CLOB 타입과 매핑
- @Lob에는 지정할 수 있는 속성이 없다.
- 매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 매핑
  - CLOB: String, char[], java.sql.CLOB
  - BLOB: byte[], java.sql. BLOB

## @Transient
- 필드 매핑X
- 데이터베이스에 저장X, 조회X
- 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용

``` java
@Transient
private Integer temp;
```

## 기본 키 매핑 어노테이션
- @Id
- @GeneratedValue

``` java
@Id @GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```

## 기본 키 매핑 방법
- 직접 할당: @Id만 사용
- 자동 생성(@GeneratedValue)
  - IDENTITY: 데이터베이스에 위임, MYSQL
  - SEQUENCE: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
    - @SequenceGenerator 필요
  - TABLE: 키 생성용 테이블 사용, 모든 DB에서 사용
    - @TableGenerator 필요
  - AUTO: 방언에 따라 자동 지정, 기본값

## 직접 할당
- @Id 사용

> 보통은 @Id와 @GenerateValue를 같이 사용한다.

## IDENTITY 전략

### 특징
- 이 전략은 PK가 INSERT Query를 날린 후, PK 값이 결정된다. 하지만 영속성 컨텍스트에는 PK값이
  무조건 있어야한다, 그래서 JPA는 1차 캐시 맵에 넣을 key 값이 없으니 넣을 방법이 없다.
  결국, JPA는 이 경우만, persist() 시, INSERT Query를 날려준다.
- 버퍼링 기능 불가
- persist() 하는 시점에 id를 조회 할 수 있다. (select 쿼리는 안 날라간다.)
- 기본 키 생성을 데이터베이스에 위임
- 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용
  (예: MySQL의 AUTO_ INCREMENT)
- JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행
- AUTO_ INCREMENT는 데이터베이스에 INSERT SQL을 실행한 이후에 ID 값을 알 수 있음
- IDENTITY 전략은 em.persist() 시점에 즉시 INSERT SQL 실행 하고 DB에서 식별자를 조회

### 매핑

``` java
@Entity
public class Member {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
}
```

## SEQUENCE 전략

### 특징
- 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트(예: 오라클 시퀀스)
- 오라클, PostgreSQL, DB2, H2 데이터베이스에서 사용
- 네트워크를 왔다 갔다해서(next call ~) 성능상 떨어지는건 아닌가?
  * JPA의 initValue, allocationSize 옵션으로 성능최적화가 가능하다.

> pk는 타입은 왠만해서 Long 타입으로 지정하자!

### 매핑

``` java
@Entity
@SequenceGenerator(
name = "MEMBER_SEQ_GENERATOR",
sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
initialValue = 1, allocationSize = 1)
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE,
  private Long id;
}
```

### @SequenceGenerator
- 주의: allocationSize 기본값 = 50

|속성|기능|기본 값|
|------|---|---|
|name|식별자 생성기 이름|필수|
|sequenceName|데이터베이스에 등록되어 있는 시퀀스 이름|hibernate_sequence|
|initialValue|DDL 생성 시에만 사용됨, 시퀀스 DDL을 생성할 때 처음 1 시작하는 수를 지정한다.|1|
|allocationSize|시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용됨) <br /> 데이터베이스 시퀀스 값이 하나씩 증가하도록 설정되어 있으면 이 값 을 반드시 1로 설정해야 한다|50 (50 ~ 100이 적정 수준)|
|catalog, schema|데이터베이스 catalog, schema 이름||

- call next value 2번 호출된다!
  * 그 이유는 처음 DB_SEQ = 1 (Dummy 호출)을 하는데 이상하다 감지하여
    그 다음 한번 더 호출 (DB_SEQ = 51)이 된다.
  * 그리고 50번이 될때 까지 메모리에서 가져오고 그것을 넘어가면 50 증가시킨다.
  * 쿼리 호출을 최소화 할수 있다.

## TABLE 전략
- 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉 내내는 전략
- 장점: 모든 데이터베이스에 적용 가능
- 단점: 성능

### 매핑

``` sql
create table MY_SEQUENCES (
  sequence_name varchar(255) not null,
  next_val bigint,
  primary key ( sequence_name )
)
```

``` java
@Entity
@TableGenerator(
name = "MEMBER_SEQ_GENERATOR",
table = "MY_SEQUENCES",
pkColumnValue = “MEMBER_SEQ", allocationSize = 1)
public class Member {
@Id
@GeneratedValue(strategy = GenerationType.TABLE,
generator = "MEMBER_SEQ_GENERATOR")
  private Long id;
}
```

### @TableGenerator - 속성

|속성|기능|기본 값|
|------|---|---|
|name|식별자 생성기 이름|필수|
|table|키생성 테이블명|hibernate_sequences|
|pkColumnName|시퀀스 컬럼명|sequence_name|
|valueColumnName|시퀀스 값 컬럼명|next_val|
|pkColumnValue|키로 사용할 값 이름|엔티티 이름|
|initialValue|초기 값, 마지막으로 생성된 값이 기준이다.|0|
|allocationSize|시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용됨)|50|
|catalog, schema|데이터베이스 catalog, schema 이름||
|uniqueConstraints(DDL)|유니크 제약 조건을 지정할 수 있다.||

## 권장하는 식별자 전략
- 기본 키 제약 조건 : null 아님, 유일, 변하면 안된다.
- 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대체키를 사용하자.
- 예를 들어, 주민등록번호는 기본 키로 적적하지 않다.
- 권장: Long형 + 대체키 + 키 생성전략 사용

## 목표
- 객체와 테이블 연관관계의 차이를 이해
- 객체의 참조와 테이블의 외래 키를 매핑
- 용어 이해
  * 방향(Direction): 단방향, 양방향
  * 다중성(Multiplicity): 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
  * 연관관계의 주인(Owner): 객체 양방향 연관관계는 관리 주인이 필요

## 연관관계가 필요한 이유

> ‘객체지향 설계의 목표는 자율적인 객체들의 협력 공동체를 만드는 것이다.’
–조영호(객체지향의 사실과 오해)

### 예제 시나리오
- 회원과 팀이 있다.
- 회원은 하나의 팀에만 소속될 수 있다.
- 회원과 팀은 다대일 관계다.

### 객체를 테이블에 맞추어 모델링 (연관관계가 없는 객체)

![](https://velog.velcdn.com/images/roberts/post/eacfb7e5-d173-42bf-8faa-bdaa18fc768c/image.png)

``` java
@Entity
public class Member {

@Id @GeneratedValue
private Long id;

@Column(name = "USERNAME")
private String name;

@Column(name = "TEAM_ID")
private Long teamId; ...
}
```

``` java
@Entity
public class Team {
	@Id @GeneratedValue
    private Long id;
    
    private String name; ...
}
```

``` java
//팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeamId(team.getId());
em.persist(member);
```

``` java
//조회
Member findMember = em.find(Member.class, member.getId());
//연관관계가 없음
Team findTeam = em.find(Team.class, team.getId());
```

### 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다.
- 테이블은 외래 키로 조인을 사용해서 연관된 테이블을 찾는다.
- 객체는 참조를 사용하여 연관된 객체를 찾는다.
- 테이블과 객체 사이에는 이런 큰 간격이 있다.

## 단방향 연관관계
- 객체 지향 모델링 (객체 연관관계 사용)

![](https://velog.velcdn.com/images/roberts/post/24cbea6a-5a4d-44d1-a2a7-4f6ee01c38c8/image.png)

``` java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;
 }
```

### 객체지향 모델링

![](https://velog.velcdn.com/images/roberts/post/e4571742-493f-4a82-9832-64f883dfaa77/image.png)

#### 연관관계 저장

``` java
//팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team); //단방향 연관관계 설정, 참조 저장
em.persist(member);
```

#### 참조로 연관관계 조회 - 객체 그래프 탐색

``` java
//조회
Member findMember = em.find(Member.class, member.getId());
//참조를 사용해서 연관관계 조회
Team findTeam = findMember.getTeam();
```

#### 연관관계 수정

``` java
// 새로운 팀B
Team teamB = new Team();
teamB.setName("TeamB");
em.persist(teamB);

// 회원1에 새로운 팀B 설정
member.setTeam(teamB);
```

## 후기
> 처음에는 조인 쿼리도 헷갈리고 이제까지 배웠던 내용들이 혼재되서 복습의 중요성을 깨달았다.
물론 매시간 수업 후 복습은 하지만, 완벽히 내것으로 되기가 힘든것 같다. 전에 배웠던 것들과
조인 쿼리를 좀 더 공부를 다져야겠다.

## 양방향 매핑

![](https://velog.velcdn.com/images/roberts/post/d12be085-ad31-458d-bdb1-2a46202ece57/image.png)

- member entity는 단방향과 동일, team entity는 컬렉션 추가

``` java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
```

``` java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

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

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
 }
```

> 사실 객체에서 양방향 연관관계는 없다. 단방향 관계 2개가 존재

> 테이블은 단방향이든 양방향이든 동일하다. 그 이유는 member는 FK로 PK를 조인하면 되고
팀은 PK와 FK를 조인하면 되기 때문이다.

## 연관관계의 주인과 mappedBy
- mappedBy = JPA의 멘탈붕괴 난이도
- mappedBy는 처음 이해하기 어렵다.
- 객체와 테이블간에 연관관계를 맺는 차이를 이해해야 한다.
- 여기서 mappedBy는 1:N 매핑에서 나랑 어떤 변수랑 관계를 맺었는지 그 변수명을 값으로 적어준다.

## 객체와 테이블이 관계를 맺는 차이
- 객체 연관관계 = 2개
  * 회원 -> 팀 연관관계 1개(단방향)
  * 팀 -> 회원 연관관계 1개(단방향)
- 테이블 연관관계 = 1개
  * 회원 <-> 팀의 연관관계 1개(양방향)

## 객체의 양방향 관계
- 객체의 양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단 뱡향 관계 2개다.
- 객체를 양방향으로 참조하려면 단방향 연관관계를 2개 만들어야 한다.
- A -> B (a.getB())
- B -> A (b.getA())

## 테이블의 양방향 연관관계
- 테이블은 외래 키 하나로 두 테이블의 연관관계를 관리
- MEMBER.TEAM_ID 외래 키 하나로 양방향 연관관계 가짐 (양쪽으로 조인할 수 있다.)

``` sql
SELECT *
 FROM MEMBER M
 JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
```

``` sql
 SELECT *
 FROM TEAM T
 JOIN MEMBER M ON T.TEAM_ID = M.TEAM_ID
```

## 둘 중 하나로 외래 키를 관리해야 한다.

![](https://velog.velcdn.com/images/roberts/post/a248bfa7-489a-4102-a9c5-5b571dbcf99c/image.png)

## 연관관계 주인
- TEAM_ID를 변경할 때 Member의 team을 수정해야 할까? 아니면 Team의 members를 수정해야할가?
- 해결책은 연관관계 주인을 찾는 것이다.
- 양방향 매핑 규칙
  * 객체의 두 관계중 하나를 연관관계의 주인으로 지정
  * 연관관계 주인만이 외래키를 관리 (등록, 수정)
  * 주인이 아닌 쪽은 읽기만 가능
  * 주인은 mappedBy 속성 사용하지 않는다.
  * 주인이 아니면 mappedBy 속성으로 주인 지정

## 누구를 주인으로?
- 결론은 많은 쪽으로 주인을 정한다.
- 외래키가 있는 곳이 주인이다.
- 여기서 주인은 Member.team

> 그렇게 하지 않고 반대로 하면 team.member를 바꾸면 member쪽에서 update 쿼리가 나가는데
엄청 헷갈리기 시작하고 성능적인 이슈가 있다.

## 양방향 매핑시 가장 많이 하는 실수
- 연관관계의 주인에 값을 입력하지 않음

``` java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setName("member1");

//역방향(주인이 아닌 방향)만 연관관계 설정
team.getMembers().add(member);
em.persist(member);
```

|ID|USERNAME|TEAM_ID|
|------|---|---|
|1|member1|null|

- 양방향 매핑시 연관관계의 주인에 값을 입력해야 한다.

``` java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setName("member1");

team.getMembers().add(member); //연관관계의 주인에 값 설정
member.setTeam(team); //**
em.persist(member);
```
|ID|USERNAME|TEAM_ID|
|------|---|---|
|1|member1|2|

## 양방향 연관관계 주의
- 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자
- 연관관계 편의 메소드를 생성하자
- 양방향 매핑시에 무한 루프를 조심하자
  * 예: toString(), lombok, JSON 생성 라이브러리

> 롬복에서 왠만해서 toStrong 만들지 말라

> JSON 생성 라이브러리는 Controller에서 만드는데 그때 반환값을 엔티티로 하지 말고 DTO로 하자
이유는 첫째, StackOverflow 둘째, 엔티티는 충분히 변경이 가능한데 엔티티를 반환하면 api spec이
변경된다.

## 정리
- 단방향 매핑만으로도 이미 연관관계 매핑은 완료
- 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
- JPQL에서 역방향으로 탐색할 일이 많음
- 단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 됨 (테이블에 영향을 주지 않음)
- 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
- 연관관계의 주인은 외래 키의 위치를 기준으로 정해야함

> 꿀팁은 처음 설계시에 전부 단방향 매핑으로 끝내라! 그리고 application 개발시에 필요 시, 추가하자!

## 연관관계 매핑시 고려사항 3가지
- 다중성
- 단방향, 양방향
- 연관관계 주인

## 다중성
- 다대일: @ManyToOne
- 일대다: @OneToMany
- 일대일: @OneToOne
- 다대다: @ManyToMany

> 다중성이 헷갈려서 어느것을 쓸지 모를때는 반대로 생각해보자!
ex. 팀 - 회원 생각 안나면 회원 - 팀 관계를 생각해보자!

> 이 다중성 어노테이션들은 DB와 매핑하기 위함으로 DB관점에서의 다중성을 고려해야한다.
참고로, @ManyToMany는 실무에서 자주 쓰이지는 않는다.

## 단방향, 양방향
- 테이블
  * 외래키 하나로 양쪽 조인 가능
  * 사실 방향이라는 개념이 없음
- 객체
  * 참조용 필드가 있는 쪽으로만 참조 가능
  * 한쪽만 참조하면 단방향
  * 양쪽이 서로 참조하면 양방향 (사실 양방향 관계는 없고 단방향 2개이다.)

## 연관관계의 주인
- 테이블은 외래키 하나로 두 테이블의 연관관계 맺음
- 객체 양방향 관계는 A->B B->A처럼 참조가 2개이다.
- 객체 양방향 관계는 참조가 두군데 있다. 둘 중 테이블의 외래키를 관리할 곳을 지정해야한다.
- 연관관계의 주인: 외래키를 관리하는 참조
- 주인의 반대편: 외래키에 영향을 주지 않음. 단순조회만 가능하다.

## 다대일 단방향

![](https://velog.velcdn.com/images/roberts/post/43333d3f-86bf-4a3e-be43-b3c2c16fda3d/image.png)

- 다대일 단방향 정리
  * 가장 많이 사용하는 관계
  * 다대일의 반대는 일대다
- 다대일의 반대는 일대다

## 다대일 양방향

![](https://velog.velcdn.com/images/roberts/post/23be1a1f-a3c3-4212-a905-e23681c44daa/image.png)

- 외래키가 있는 쪽이 연관관계 주인
- 양쪽을 서로 참조하도록 개발

## 일대다 단방향

![](https://velog.velcdn.com/images/roberts/post/35ee0316-2f77-400a-8ab9-8340ef5ced42/image.png)

### 정리
- 일대다 단방향은 일대다(1:N)에서 일(1)이 연관관계의 주인
- 테이블 일대다 관계는 항상 다쪽에 외래키가 있음
- 객체와 테이블의 차이 때문에 반대편 테이블의 외래 키를 관리하는 특이한 구조
- @JoinColumn을 꼭 사용해야 함. 그렇지 않으면 조인 테이블 방식을 사용함(중간에 테이블을 하나 추가함)
- 일대다 단방향 매핑의 단점
  * 엔티티가 관리하는 외래 키가 다른 테이블에 있음
  * 연관관계 관리를 위해 추가로 UPDATE SQL 실행
- 일대다 단방향 매핑보다는 다대일 양방향 매핑을 사용하자

## 일대다 양방향

![](https://velog.velcdn.com/images/roberts/post/4011de3d-1a82-446b-a127-61d93ecec318/image.png)

### 정리
- 이런 매핑은 공식적으로 존재X
- @JoinColumn(insertable=false, updatable=false)
- 읽기 전용 필드를 사용해서 양방향 처럼 사용하는 방법
- 다대일 양방향을 사용하자

> 결론은 일대다 관계는 별로 권장하지 않는다. 왜냐하면 다른 테이블 update 쿼리를 보고 헷갈릴수 있기 때문이다.

## 일대일 관계
- 일대일 관계는 그 반대도 일대일
- 주테이블이나대상테이블중에외래키선택가능
  * 주 테이블에 외래 키
  * 대상 테이블에 외래 키
- 외래 키에 데이터베이스 유니크(UNI) 제약조건 추가

## 일대일: 주 테이블에 외래 키 단방향

![](https://velog.velcdn.com/images/roberts/post/b5de66ed-31fc-41c2-9165-9a30ab8486ab/image.png)

### 정리
- 다대일(@ManyToOne) 단방향 매핑과 유사

## 일대일: 주 테이블에 외래 키 양방향

![](https://velog.velcdn.com/images/roberts/post/589556c6-51d5-4123-9c6a-11e4cdfe2a2c/image.png)

### 정리
- 다대일 양방향 매핑처럼 외래키 있는 곳이 연관관계 주인
- 반대편은 mappedBy 적용

## 일대일: 대상 테이블에 외래 키 단방향

![](https://velog.velcdn.com/images/roberts/post/8854696f-f1f1-4188-9f79-005460c0055b/image.png)

### 정리
- 단방향 관계는 JPA 지원X
- 양방향 관계는 지원

## 일대일: 대상 테이블에 외래 키 양방향

![](https://velog.velcdn.com/images/roberts/post/6445381f-935d-4c5e-9c44-176962607ee7/image.png)

- 사실 일대일 주 테이블에 외래 키 양방향과 매핑 방법은 같음

## 일대일 정리
- 주테이블에외래키
  * 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래 키를 두고 대상 테이블을 찾음
  * 객체지향 개발자 선호
  * JPA 매핑 편리
  * 장점: 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
  * 단점: 값이 없으면 외래 키에 null 허용
- 대상테이블에외래키
  * 대상 테이블에 외래 키가 존재
  * 전통적인 데이터베이스 개발자 선호
  * 장점: 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지
  * 단점: 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨(프록시는 뒤에서 설명)

## 다대다
- 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
- 연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야함

![](https://velog.velcdn.com/images/roberts/post/b3393ab3-4157-4422-942f-3a1c2c536381/image.png)

- 객체는 컬렉션을 사용해서 객체 2개로 다대다 관계 가능

![](https://velog.velcdn.com/images/roberts/post/5dff74ee-0ef6-4131-b6f0-1c3dc9e2ad3d/image.png)

- @ManyToMany 사용
- @JoinTable로 연결테이블 지정
- 다대다 매핑: 단방향, 양방향 가능

## 다대다 매핑의 한계
- 편리해 보이지만 실무에서 사용X
- 연결 테이블이 단순히 연결만 하고 끝나지 않음
- 주문시간, 수량 같은 데이터가 들어올 수 있음

![](https://velog.velcdn.com/images/roberts/post/ff2b9f61-8372-489b-8604-ebd04485a2d2/image.png)

## 다대다 한계 극복
- 연결 테이블용 엔티티 추가(연결 테이블을 엔티티로 승격)
- @ManyToMany -> @OneToMany, @ManyToOne

![](https://velog.velcdn.com/images/roberts/post/4d5617ea-dfa2-4a5c-922e-574d4f2138e9/image.png)


## 상속관계 매핑
- 관계형 DB는 상속관계를 만들수가 없다.
- 슈퍼타입 서브타입 관계라는 모델링 기법이 객체 상속과 유사
- 상속관계 매핑: 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑

![](https://velog.velcdn.com/images/roberts/post/fc99d931-5692-4b1c-8abe-94caedee5b63/image.png)

- 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
  * 각각테이블로변환->조인전략
  * 통합 테이블로 변환 -> 단일 테이블 전략
  * 서브타입 테이블로 변환 -> 구현 클래스마다 테이블 전략
- 상속관계 매핑은 논리모델을 물리모델로 바꾸는 방법이다.

## 주요 어노테이션
- @Inheritance(strategy=InheritanceType.XXX)
  * JOINED: 조인 전략
  * SINGLE_TABLE: 단일 테이블 전략
  * TABLE_PER_CLASS: 구현 클래스마다 테이블 전략
- @DiscriminatorColumn(name=“DTYPE”)
- @DiscriminatorValue(“XXX”)
- 어느 전략을 쓰든 객체의 입장에서는 똑같다.

## 조인전략
- 공통된 속성을 가진 슈퍼타입 테이블을 만들고 다른 속성들을 가진 서브 테이블을 만들어서 필요하면
  조인으로 가져온다.

![](https://velog.velcdn.com/images/roberts/post/acef233b-9467-469f-ac80-39deecf5f13b/image.png)

- 정석 전략
- 장점
  * 테이블 정규화
  * 외래 키 참조 무결성 제약조건 활용가능
  * 저장공간 효율화
- 단점
  * 조회시조인을많이사용,성능저하
  * 조회 쿼리가 복잡함
  * 데이터 저장시 INSERT SQL 2번 호출

## 단일 테이블 전략
- 이 전략에서는 @DiscriminatorColumn을 꼭 추가해줘야한다.

![](https://velog.velcdn.com/images/roberts/post/8af4b437-6ac3-4909-86d9-2e7fc835100b/image.png)

- 장점
  * 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
  * 조회 쿼리가 단순함
- 단점
  * 자식 엔티티가 매핑한 컬럼은 모두 null 허용
  * 단일테이블에모든것을저장하므로테이블이커질수있다.상황에 따라서 조회 성능이 오히려 느려질 수 있다.
- 슈퍼, 서브타입으로 나누지 않고 1개의 테이블에 다 떼려 박는다.
- 조회할때 추상클래스 타입으로 조회를 하면 query가 union으로 모든 테이블을 다 조회한다.

## 구현 클래스마다 테이블 전략
- 절대 쓰면 안되는 전략

![](https://velog.velcdn.com/images/roberts/post/9cd7f098-a048-44a0-9bef-cfd93f28dcd8/image.png)

- 이 전략은 데이터베이스 설계자와 ORM 전문가 둘 다 추천X
- 장점
  * 서브 타입을 명확하게 구분해서 처리할 때 효과적
  * not null 제약조건 사용 가능
- 단점
  * 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL 필요)
  * 자식 테이블을 통합해서 쿼리하기 어려움

## @MappedSuperclass
- 공통 매핑 정보가 필요할 때 사용(id, name)

![](https://velog.velcdn.com/images/roberts/post/16d0e321-8065-418f-aeb2-4bb179e3bff8/image.png)

- 상속관계 매핑X
- 엔티티X, 테이블과 매핑X
- 부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공
- 조회, 검색 불가(em.find(BaseEntity) 불가)
- 직접 생성해서 사용할 일이 없으므로 추상 클래스 권장
- 테이블과 관계 없고, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할
- 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통 으로 적용하는 정보를 모을 때 사용
- 참고: @Entity 클래스는 엔티티나 @MappedSuperclass로 지 정한 클래스만 상속 가능

> 항상 공통된 기능을 BaseEntity로 묶어서 @MappedSuperclass로 두면 좋을것 같다.
또한 abstract 클래스로 두어서 사용함으로 나중에 find같은것에 미연의 방지를 할 수 있다.

## Member를 조회할 때 Team도 함께 조회해야 할까?

![](https://velog.velcdn.com/images/roberts/post/0bc2bd4b-fb1b-4052-a4d1-533158c4b1e5/image.png)

- 회원과 팀 함께 출력

``` java
public void printUserAndTeam(String memberId) {
  Member member = em.find(Member.class, memberId);
  Team team = member.getTeam();
  System.out.println("회원 이름: " + member.getUsername());
  System.out.println("소속팀: " + team.getName()); 
}
```

- 회원만 출력

``` java
public void printUser(String memberId) {
  Member member = em.find(Member.class, memberId);
  Team team = member.getTeam();
  System.out.println("회원 이름: " + member.getUsername());
}
```

> 위의 상황을 JPA는 프록시와 지연로딩을 이용하여 해결할수 있다.
지연로딩을 이해하려면 프록시를 먼저 이해해야한다.

## 프록시 기초
- em.find() vs em.getReference()
- em.find(): 데이터베이스를 통해서 실제 엔티티 객체 조회
- em.getReference(): 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회
  * 메소드를 호출하는 시점에는 DB쿼리가 나가지는 않지만 실제 사용하는 시점에
    쿼리가 호출된다.
  * 단, ID 조회시, 쿼리가 안나갔던 이유는 getReference()를 호출할때, 파라미터로
    ID값을 넘겨서 어떤 ID인지 알수 있기 때문이다.
  * 프록시 객체는 초기에는 껍데기에 안에는 텅텅 비워있으며 실제 reference를 가르키는
    target이 있다.
  * 초기에는 target = null이고 id값만 있는 가짜 객체를 반환한다.

![](https://velog.velcdn.com/images/roberts/post/31c209a2-e95d-44e3-82f1-86b1cf857d21/image.png)

## 프록시 특징
- 실제 클래스를 상속받아서 만들어짐 (하이버네이트가 처리)
- 실제 클래스와 겉 모양이 같다.
- 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨(이론상)

![](https://velog.velcdn.com/images/roberts/post/51bb2ecf-c29d-42eb-bc05-920f9a5080c3/image.png)

- 프록시 객체는 실제 객체의 참조를 보관
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출

## 프록시 객체의 초기화

``` java
Member member = em.getReference(Member.class, “id1”); 
member.getName();
```

![](https://velog.velcdn.com/images/roberts/post/e69016e3-f9f1-4996-aa6b-e08287b00277/image.png)

- 개발자가 getName()을 호출하면 MemberProxy가 target을 확인한다.
- target이 null이므로 JPA가 영속성 컨텍스트에 target을 요청한다. (진짜 엔티티를 가져온다.)
- 영속성 컨텍스트가 DB 조회 후, 실제 엔티티 생성한다.
- target과 실제 엔티티와 연결
- target.getName() 호출

## 프록시의 특징
- 프록시객체는 처음 사용할때 한번만 초기화
- 프록시 객체를 초기화 할 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님, 초 기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능
- 프록시 객체는 원본 엔티티를 상속받음, 따라서 타입 체크시 주의해야함 (== 비 교 실패, 대신 instance of 사용)
- 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 실제 엔티티 반환
  * 이미 멤버가 영속성 컨텍스트 1차 캐시에 있어서 굳이 프록시 객체를 만들 이유가 없다.
  * JPA에서는 같은 영속성 컨텍스트 안에서 같은 PK를 가져온 것의 == 비교는 항상 같다.
- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면 문제 발생
  (하이버네이트는 org.hibernate.LazyInitializationException 예외를 터트림)
  (실무에서 진짜 많이 보는 예외!)

## 프록시 확인
- 프록시 인스턴스의 초기화 여부 확인
  PersistenceUnitUtil.isLoaded(Object entity)
- 프록시 클래스 확인 방법
  entity.getClass().getName() 출력(..javasist.. or HibernateProxy...)
- 프록시 강제 초기화
  org.hibernate.Hibernate.initialize(entity);
- 참고: JPA 표준은 강제 초기화 없음
  강제 호출: member.getName()

## Member를 조회할 때 Team도 함께 조회해야 할까?
- 단순히 member 정보만 사용하는 비즈니스 로직
  println(member.getName());

![](https://velog.velcdn.com/images/roberts/post/380c1106-48c2-455d-9ee5-070c688ad6f6/image.png)

## 지연 로딩 LAZY을 사용해서 프록시로 조회

``` java
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;
 }
```

![](https://velog.velcdn.com/images/roberts/post/20776fef-c3df-44ba-b063-7fd01f0c7823/image.png)

- Member member = em.find(Member.class, 1L);

![](https://velog.velcdn.com/images/roberts/post/7afcf910-aa76-43cb-a160-c14f62c8fb2b/image.png)

- Team team = member.getTeam();
- team.getName(); // 실제 team을 사용하는 시점에 초기화(DB 조회)

![](https://velog.velcdn.com/images/roberts/post/f9269fdb-dcb0-4bee-ae9e-ca438aa6ae95/image.png)

## 즉시 로딩 EAGER를 사용해서 함께 조회

``` java
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}
```

![](https://velog.velcdn.com/images/roberts/post/3e483d67-b479-486c-b3de-ee8f982cf1ec/image.png)

## 즉시 로딩(EAGER), Member조회시 항상 Team도 조회

![](https://velog.velcdn.com/images/roberts/post/db64958b-b883-49b6-a12d-648dcef5ced2/image.png)

- JPA 구현체는 가능하면 조인을 사용해서 SQL 한번에 함께 조회

## 프록시와 즉시로딩 주의
- 가급적 지연 로딩만 사용(특히 실무에서)
- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
- 즉시 로딩은 JPQL에서 N+1 문제를 일으킨다.
- @ManyToOne, @OneToOne은 기본이 즉시 로딩
  -> LAZY로 설정
- @OneToMany, @ManyToMany는 기본이 지연 로딩

## 지연 로딩 활용
- Member와 Team은 자주 함께 사용 -> 즉시 로딩
- Member와 Order는 가끔 사용 -> 지연 로딩
- Order와 Product는 자주 함께 사용 -> 즉시 로딩

![](https://velog.velcdn.com/images/roberts/post/6e837608-bb4e-400e-9311-853623425b04/image.png)

## 실무
- 모든 연관관계에 지연 로딩을 사용해라!
- 실무에서 즉시 로딩을 사용하지 마라!
- JPQL fetch 조인이나, 엔티티 그래프 기능을 사용해라! (뒤에서 설명)
- 즉시 로딩은 상상하지 못한 쿼리가 나간다.

## 영속성 전이: CASCADE
- 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들도 싶을 때
- 예: 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장.

![](https://velog.velcdn.com/images/roberts/post/e73b6000-acf9-476a-a274-8305812d41f5/image.png)

## 영속성 전이: 저장

``` java
@OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
```

![](https://velog.velcdn.com/images/roberts/post/d9a85688-122d-4e5f-a16e-c6dee68bce91/image.png)

> 그럼 cascade는 언제 쓰는가? 무조건 1:N 관계일때 다 걸어야 하는가?
하나의 1이 다수의 N을 관리할 때 의미가 있지만 그 외엔 별로 소용이 없다.
하나의 게시판에 첨부파일정도는 사용이 가능하다.
소유자가 1개일때만 가능하며 다른 엔티티와 연관이 있으면 쓰지 말자!

> 쓸수 있는경우는 1과 N이 life cycle이 거의 같은 경우 + 단일 소유자 1개일때만 가능

### 주의
- 영속성 전이는 연관관계를 매핑하는 것과 아무 관련이 없음
- 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 편리함 을 제공할 뿐

## CASCADE의 종류
- ALL: 모두 적용
- PERSIST: 영속
- REMOVE: 삭제
- MERGE: 병합
- REFRESH: REFRESH
- DETACH: DETACH

> 종류는 많지만 실무에서는 ALL과 PERSIST를 가장 많이 사용한다.

## 고아 객체
- 고아 객체 제거: 부모 엔티티와 연관관계가 끊어진 자식 엔티티 를 자동으로 삭제
- orphanRemoval = true
- Parent parent1 = em.find(Parent.class, id);
  parent1.getChildren().remove(0);
  //자식 엔티티를 컬렉션에서 제거
- DELETE FROM CHILD WHERE ID=?

### 주의
- 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고 삭제하는 기능
- 참조하는 곳이 하나일 때 사용해야함!
- 특정엔티티가개인소유할때사용
- @OneToOne, @OneToMany만 가능

> 참고: 개념적으로 부모를 제거하면 자식은 고아가 된다. 따라서 고 아 객체 제거 기능을 활성화 하면, 부모를 제거할 때 자식도 함께 제거된다. 이것은 CascadeType.REMOVE처럼 동작한다.

## 영속성 전이 + 고아 객체, 생명주기
- CascadeType.ALL + orphanRemovel=true
- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
- 두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명 주기를 관리할 수 있음
- 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용

## JPA의 데이터 타입 분류
- 엔티티 타입
  * @Entity로 정의하는 객체
  * 데이터가 변해도 식별자로 지속해서 추적 가능
  * 예)회원엔티티의키나나이값을변경해도식별자로인식가능
- 값 타입
  * int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
  * 식별자가 없고 값만 있으므로 변경시 추적 불가
  * 예) 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체

## 값 타입 분류
- 기본값 타입
  * 자바 기본 타입(int, double)
  * 래퍼 클래스(Integer, Long)
  * String
- 임베디드 타입(embedded type, 복합 값 타입)
  * 어떠한 기본값 타입들을 커스텀하게 묶어 쓰기
- 컬렉션 값 타입(collection value type)
  * 자바 컬렉션에 기본값 타입을 넣을수 있는걸 말한다.

## 기본값 타입
- 예) String name, int age
- 생명주기를 엔티티의 의존
  * 예) 회원을 삭제하면 이름, 나이 필드도 함께 삭제
- 값 타입은 공유하면X
  * 예) 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안됨

> 참고: 자바의 기본 타입은 절대 공유X
* int, double 같은 기본타입(primitive type)은 절대 공유X
* 기본타입은항상값을복사함
* Integer같은 래퍼 클래스나 String 같은 특수한 클래스는 공유 가능한 객체이지만 변경X

## 임베디드 타입
- 새로운 값 타입을 직접 정의할 수 있음
- JPA는 임베디드 타입(embedded type)이라 함
- 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고도 함
- int, String과 같은 값 타입
- ex) 회원 엔티티는 이름, 근무 시작일, 근무 종료일, 주소 도시, 주소 번지, 주소 우편번호를 가진다.

![](https://velog.velcdn.com/images/roberts/post/9f4f0e4a-0303-461a-a308-f716d5bfc7cd/image.png)

- 회원 엔티티는 이름, 근무 기간, 집 주소를 가진다.

![](https://velog.velcdn.com/images/roberts/post/a9b4add2-115d-4495-acff-4838d72c51de/image.png)

> 위의 그림이 보통 객체지향적으로 잘 설계한 사진이다.

![](https://velog.velcdn.com/images/roberts/post/d5f77b80-ec20-4c37-99f7-000d091992b2/image.png)

## 임베디드 타입 사용법
- @Embeddable: 값 타입을 정의하는 곳에 표시
- @Embedded: 값 타입을 사용하는 곳에 표시
- 기본 생성자 필수
- @Embeddable과 @Embedded 둘 중 하나만 명시해도 좋지만 둘다 명시를 권장

## 임베디드 타입의 장점
- 재사용
- 높은 응집도
- Period.isWork()처럼 해당 값 타입만 사용하는 의미있는 메소드를 만들수 있음
- 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티 티에 생명주기를 의존함

## 임베디드 타입과 테이블 매핑

![](https://velog.velcdn.com/images/roberts/post/f8b1b117-0f7a-4f4e-8150-ec39ab95394a/image.png)

- 임베디드 타입은 엔티티의 값일 뿐이다.
- 임베디드 타입을 사용하기 전과 후에 매핑하는 테이블은 같다.
- 객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능
- 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래 스의 수가 더 많음
- 하지만 현업에서 정말 많이 쓰지는 않는다.

## 임베디드 타입과 연관관계

![](https://velog.velcdn.com/images/roberts/post/a346022a-79eb-49b2-864a-14e572acbf07/image.png)

## @AttributeOverride: 속성 재정의
- 한 엔티티에서 같은 값 타입을 사용하면?
- 컬럼 명이 중복됨
- @AttributeOverrides, @AttributeOverride를 사용해서 컬러 명 속성을 재정의

## 임베디드 타입과 null
- 임베디드 타입의 값이 null이면 매핑한 컬럼 값은 모두 null

## 값 타입 공유 참조
- 임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험함
- 부작용(side effect) 발생

![](https://velog.velcdn.com/images/roberts/post/d1553da8-9505-428d-9969-9bad5ca788f2/image.png)

## 값 타입 복사
- 값 타입의 실제 인스턴스인 값을 공유하는것은 위험
- 대신 값(인스턴스)를 복사해서 사용

![](https://velog.velcdn.com/images/roberts/post/3cb96e5c-d842-44b1-89eb-0ea929cb457c/image.png)

## 객체 타입의 한계
- 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용 을 피할 수 있다.
- 문제는 임베디드 타입처럼 직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입이다.
- 자바 기본 타입에 값을 대입하면 값을 복사한다.
- 객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.
- 객체의 공유 참조는 피할 수 없다.
- 기본 타입(primitive type)

``` java
int a = 10;
intb=a;//기본 타입은 값을 복사 
b = 4;
```

- 객체 타입

``` java
Address a = new Address(“Old”);
Address b = a; //객체 타입은 참조를 전달 
b. setCity(“New”)
```

## 불변 객체
- 객체 타입을 수정할 수 없게 만들면 부작용 원천 차단
- 값 타입은 불변객체로 설계해야함
- 불변객체: 생성시점 이후 절대 값을 변경 X
- 생성자로만 값을 설정하고 수정자를 만들지 않으면 됨

> 참고: Integer, String은 자바가 제공하는 대표적인 불변 객체
불변이라는 작은 제약으로 부작용이라는 큰 재앙을 막을 수 있다.

> 실무에서는 값 타입은 전부 불변객체로 만들어야 하며 만약 값을 바꾸고 싶으면
다시 생성자로 정의해야한다.

## 값 타입의 비교
- 값 타입: 인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봐 야함

``` java
int a = 10;
int b = 10;
```

``` java
Address a = new Address(“서울시”) 
Address b = new Address(“서울시”)
```

- 동일성 비교: 인스턴스의 참조 값을 비교, == 사용
- 동등성(equivalence) 비교: 인스턴스의 값을 비교, equals() 사용
- 값 타입은 a.equals(b)를 사용해서 동등성 비교를 해야 함
- 값 타입의 equals() 메소드를 적절하게 재정의(주로 모든 필드 사용)

> 사실 현업에서 equals를 쓸때가 거의 없다.

## 값 타입 컬렉션

![](https://velog.velcdn.com/images/roberts/post/f80f80e0-a0cd-4b52-92ab-14120cc03404/image.png)

- 값 타입을 하나 이상 저장할 때 사용
- @ElementCollection, @CollectionTable 사용
- 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다.
- 컬렉션을 저장하기 위한 별도의 테이블이 필요함

## 값 타입 컬렉션 사용
- 값 타입 저장 예제
  * member만 영속화해도 나머지 컬렉션 값타입이 자동으로 저장이 된다.
- 값타입조회예제
  * 값 타입 컬렉션도 지연 로딩 전략 사용
- 값 타입 수정 예제
  * 해당되는 데이터를 다 지우고 기존에 컬렉션에 남아있는 데이터를 다시 insert한다.
  * primitive type같은 경우는 update query가 나간다.

> 참고: 값 타입 컬렉션은 영속성 전에(Cascade) + 고아 객체 제 거 기능을 필수로 가진다고 볼 수 있다.

## 값 타입 컬렉션의 제약사항
- 값 타입은 엔티티와 다르게 식별자 개념이 없다.
- 값은 변경하면 추적이 어렵다.
- 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.
- 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 구성해야 함: null 입력X, 중복 저장X

## 값 타입 컬렉션 대안
- 실무에서는 상황에 따라 값 타입 컬렉션 대신에 일대다 관계를 고려
- 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용
- 영속성 전이(Cascade) + 고아 객체 제거를 사용해서 값 타입 컬 렉션 처럼 사용
- EX) AddressEntity

> 실무에서는 일대다 관계를 많이 사용한다. 값 타입 컬렉션을 쓸 때는 업데이트가 필요없을때
그 때 사용하곤 한다.

## 정리
- 엔티티 타입의 특징
  * 식별자O
  * 생명주기관리
  * 공유
- 값타입의특징
  * 식별자X
  * 생명 주기를 엔티티에 의존
  * 공유하지 않는 것이 안전(복사해서 사용)
  * 불변객체로만드는것이 안전

## JPA는 다양한 쿼리 방법을 지원
- JPQL
- JPA Criteria
- QueryDSL
- 네이티브 SQL
- JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용

## JPQL 소개
- 가장 단순한 조회 방법
  * EntityManager.find()
  * 객체 그래프 탐색(a.getB().getC())
- 나이가 18살 이상인 회원을 모두 검색하고 싶다면?

## JPQL
- JPA를 사용하면 엔티티 객체를 중심으로 개발
- 문제는 검색 쿼리
- 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검 색 조건이 포함된 SQL이 필요
- JPA는 SQL을 추상화한 JPQL이라는 객체지향 쿼리 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 데이터베이스 테이블을 대상으로 쿼리
- 테이블이 아닌 객체를 대상으로 검색하는 객체지향 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X
- JPQL을 한마디로 정의하면 객체 지향 SQL

``` java
//검색
String jpql = "select m From Member m where m.name like ‘%hello%'";

List<Member> result = em.createQuery(jpql, Member.class).getResultList();
```

> 여기서 m은 sql로 * 이거나 테이블 전체 컬럼을 나열한것을 의미하며
Member entity 자체를 의미한다.

## JPQL과 실행된 SQL

``` java
//검색
String jpql = "select m from Member m where m.age > 18";
List<Member> result = em.createQuery(jpql, Member.class).getResultList();
```

``` sql
select
     m.id as id,
     m.age as age,
     m.USERNAME as USERNAME,
     m.TEAM_ID as TEAM_ID
from Member m where m.age>18
```

## Criteria 소개

``` java
//Criteria 사용 준비
CriteriaBuilder cb = em.getCriteriaBuilder(); CriteriaQuery<Member> query = cb.createQuery(Member.class);

//루트 클래스 (조회를 시작할 클래스)
Root<Member> m = query.from(Member.class);

//쿼리 생성 
CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), “kim”)); 
List<Member> resultList = em.createQuery(cq).getResultList();
```

- JPQL을 적을 때 String 형태로 적어야함으로 type safe하지 못하며 동적쿼리를 만들기 어려움
- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- JPA 공식 기능
- **단점: 너무 복잡하고 실용성이 없다.**
- Criteria 대신에 QueryDSL 사용 권장
- 실무에서는 잘 쓰이지 않는다.

## QueryDSL 소개

``` java
//JPQL
  //select m from Member m where m.age > 18
JPAFactoryQuery query = new JPAQueryFactory(em);

QMember m = QMember.member;

List<Member> list = query.selectFrom(m).where(m.age.gt(18)).orderBy(m.name.desc()).fetch();
```

- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- 컴파일 시점에 문법 오류를 찾을 수 있음
- 동적쿼리 작성 편리함
- 단순하고 쉬움
- 실무 사용 권장

## Native SQL 소개
- JPA가 제공하는 SQL을 직접 사용하는 기능
- JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능
- 예) 오라클 CONNECT BY, 특정 DB만 사용하는 SQL 힌트
  * 특정 DB만 사용하는 SQL 힌트는 굳이 네이티브 SQL이 아니더라도 하이버네이트 자체적으로 지원

> flush는 Commit이 될때 반영되지만 예외적으로 query를 할때도 반영이된다.
그리고 JDBC를 이용해 강제로 SQL을 날릴때는 JPA와 상관없으므로 flush가 안된다.
이럴때는 강제적으로 flush()를 호출해줘야 한다.

``` java
String sql =
    “SELECT ID, AGE, TEAM_ID, NAME FROM MEMBER WHERE NAME = ‘kim’";
List<Member> resultList =
            em.createNativeQuery(sql, Member.class).getResultList();
```

## JDBC 직접 사용, SpringJdbcTemplate 등
- JPA를 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스등을 함께 사용 가능
- 단 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요
- 예) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 플러시