## 요구사항 분석
- 회원은 상품을 주문할 수 있다.
- 주문 시 여러 종류의 상품을 선택할 수 있다.

## 도메인 모델 분석
- 회원과 주문의 관계: 회원은 여러 번 주문할 수 있다. (일대다)
- 주문과상품의관계:주문할때여러상품을선택할수있다.반 대로 같은 상품도 여러 번 주문될 수 있다. 주문상품 이라는 모델 을 만들어서 다대다 관계를 일다대, 다대일 관계로 풀어냄

![](https://velog.velcdn.com/images/roberts/post/c43158a0-7c40-4966-b7f4-33482a915e34/image.png)

## 테이블 설계

![](https://velog.velcdn.com/images/roberts/post/7635dfa4-39b2-49ed-bbc3-fcaf70205966/image.png)

## 엔티티 설계와 매핑

![](https://velog.velcdn.com/images/roberts/post/46a7419e-0a9c-4649-ab6c-8443c96d74f2/image.png)

## 데이터 중심 설계의 문제점
- 현재 방식은 객체 설계를 테이블 설계에 맞춘 방식
- 테이블의 외래키를 객체에 그대로 가져옴
- 객체 그래프 탐색이 불가능
- 참조가 없으므로 UML도 잘못됨

> 위의 ERD와 UML은 너무 객체지향스럽지가 않고 객체그래프탐색이 전혀 안되어져 있다.
그래서 결국 우리는 테이블간 연관관계 매핑에 대해 학습을 하고 이 문제점을 해결해야할것이다.

## 테이블 구조
- 테이블 구조는 이전과 같다.

![](https://velog.velcdn.com/images/roberts/post/03559177-e1e9-4299-84df-ab58bbc2008f/image.png)

## 객체 구조
- 참조를 사용하도록 변경

![](https://velog.velcdn.com/images/roberts/post/af118d12-b211-40d9-a328-d544a13c8fef/image.png)

> 최대한 단방향으로 해결하자!

## 배송, 카테고리 추가 - 엔티티
- 주문과 배송은 1:1 (@OneToOne)
- 상품과 카테고리는 N:M (@ManyToOne)

![](https://velog.velcdn.com/images/roberts/post/6a5a0961-f4e2-4cd7-a5e2-9f0bebaffa66/image.png)

## 배송, 카테고리 추가 - ERD

![](https://velog.velcdn.com/images/roberts/post/68ed07d5-7e3f-4d79-907c-6927aad400b1/image.png)

## 배송, 카테고리 추가 - 엔티티 상세

![](https://velog.velcdn.com/images/roberts/post/da42634f-5441-4b9e-8820-68682e7aa9eb/image.png)

## N:M 관계는 1:N, N:1로
- 테이블의 N:M 관계는 중간 테이블을 이용해서 1:N, N:1
- 실전에서는 중간 테이블이 단순하지 않다.
- @ManyToMany는 제약: 필드 추가X, 엔티티 테이블 불일치
- 실전에서는 @ManyToMany 사용X

## @JoinColumn
- 외래 키를 매핑할 때 사용

|속성|설명|기본값|
|------|---|---|
|name|매핑할 외래 키 이름|필드명 + _ + 참조하는 테 이블의 기본 키 컬럼명|
|referencedColumnName|외래 키가 참조하는 대상 테이블의 컬럼명|참조하는 테이블의 기본 키 컬럼명|
|foreignKey(DDL)|외래 키 제약조건을 직접 지정할 수 있다. <br> 이 속성은 테이블을 생성할 때만 사용한다.||
|unique <br>nullable insertable <br> updatable <br> columnDefinition <br> table|@Column의 속성과 같다.||

## @ManyToOne - 주요 속성
- 다대일 관계 매핑

|속성|설명|기본값|
|------|---|---|
|optional|false로 설정하면 연관된 엔티티가 항상 있어야 한다.|TRUE|
|fetch|글로벌 페치 전략을 설정한다.|@ManyToOne=FetchType.EAGER <br> @OneToMany=FetchType.LAZY|
|cascade|영속성 전이 기능을 사용한다.||
|targetEntity|연관된 엔티티의 타입 정보를 설정한다. 이 기능은 거 의 사용하지 않는다. 컬렉션을 사용해도 제네릭으로 타 입 정보를 알 수 있다.||

## @OneToMany - 주요 속성
- 다대일 관계 매핑

|속성|설명|기본값|
|------|---|---|
|mappedBy|연관관계의 주인 필드를 선택한다.||
|fetch|글로벌 페치 전략을 설정한다.|@ManyToOne=FetchType.EAGER <br> @OneToMany=FetchType.LAZY|
|cascade|영속성 전이 기능을 사용한다.||
|targetEntity|연관된 엔티티의 타입 정보를 설정한다. 이 기능은 거 의 사용하지 않는다. 컬렉션을 사용해도 제네릭으로 타 입 정보를 알 수 있다.||


## 요구사항 추가
- 상품의 종류는 음반, 도서, 영화가 있고 이후 더 확장될수 있다.
- 모든 데이터는 등록일과 수정일이 있어야 한다.

## 도메인 모델

![](https://velog.velcdn.com/images/roberts/post/7cd2d6b7-72da-44f7-923e-b3deab0d754c/image.png)

![](https://velog.velcdn.com/images/roberts/post/871c6eb3-3d78-4287-8735-77d98c5875bf/image.png)

![](https://velog.velcdn.com/images/roberts/post/c28fdab7-8a3b-45b3-b8f5-e0ab33f9a80e/image.png)

## 글로벌 페치 전략 설정
- 모든 연관관계를 지연 로딩으로
- @ManyToOne, @OneToOne은 기본이 즉시 로딩이므로 지연 로딩으로 변경

## 영속성 전이 설정
- Order -> Delivery를 영속성 전이 ALL로 설정
- Order -> OrderItem을 영속성 전이 ALL로 설정

> 뭔가 예제를 하면서 복습한다는 느낌이 조금씩 상기되는것이 많은 도움이 되었다.
처음에 @OneToOne관계에서 왜 Delivery에는 Order부분에 영속성 전이 ALL로 안하지?
그런 생각이 들었지만 연관관계 주인은 Order에 있다는걸 알고 이해가 되었다.