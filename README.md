# java template v1

## Description
* 기술스택
    * Language: Java
    * Version: JDK 17, Gradle 8
    * Framework: Spring Boot 3.3
    * ORM: Spring Data JPA
    * DB: mysql

# .env database
```
spring.application.name=java-template
spring.jackson.time-zone=UTC
spring.config.import=optional:file:.env[.properties]
# Database
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER_NAME}
spring.datasource.password=${DB_USER_PASSWORD}
# JPA
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO}
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.use_sql_comments=true
# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.springframework.web=DEBUG
logging.level.com.cupid.qufit=DEBUG
```

## Build

```bash
$ ./graclew clean bootJar
```

# Architecture
- 기본적으로 레이어드 아키텍처를 참고하여 다음과 같은 규칙을 사용하고자 합니다.
- 각 레이어에 따라 책임과 역할을 구분하여 사용해 주세요.
----
## Presentation, Domain, Infrastructure example
### Presentation
- controller, Request DTO, Response DTO 등을 작성 한다.
    - controller
        - 주로 http api 를 사용하여, API 에 대한 spec 을 swagger 를 사용하여 상세하게 작성
            - 어떤 API 인지, 특이사항이 있는 API 일 경우 description에 작성하여 프론트와 공유
            - **request 와 response 는 반드시 "전부" 필수로 작성**
                - 자료형, 필수여부, 어떠한 값인지, null 인지 아닌지 여부
        - 하나의 URL 에는 Request 혹은 Response 가 0개 혹은 1개의 class를 가진다.
        - request 및, response dto는 재사용 하지 않는다.
            - 재사용 하고자 하는 dto는 해당 API 폴더에 dto 폴더를 새로 두어, 재사용 하는 dto class 를 따로 정의할 것
    - Request DTO
        - "요청" 에 관한 책임
        - 정적 Validation 필요 (spring-boot-starter-validation 를 활용)
            - `{Package 이름}{메소드 이름}{Request}`
            - public class UserSignUpRequest
    - Response DTO
        - "응답" 에 관한 책임
        - 주로 Get 을 활용한 Http API 에서 어떠한 return 값을 받는지 전부 정의해야 합니다.
        - 파일 네이밍 예시
            - `{Package 이름}{메소드 이름}{Response}`
                - public class UserLoginResponse

### Application
- 실제 비즈니스 레이어 구현체
    - service 를 주로 구현한다.
    - 실제 비즈니스 로직이 이루어 지는 곳

### Domain
- 주로 변경되지 않는다.
    - 코어한 로직, 변경될 경우 다른 레이어 에도 전부 영향이 있다.
- 모든 레이어가 해당 Domain 레이어의 의존성을 갖고 있다.

### Infrastructure (Infra)
- 외부와 연결되어있는 레이어 및 구현체입니다.
- 소셜 로그인, 외부 연동 API, 등 해당 서버 애플리케이션에서만 이루어지지 않고 타 API 를 사용할때 주로 구현체로 사용합니다.
    - 예를들어 nice-pay 의 결제 완료에 대한 API 를 사용하고자 할때, 해당 레이어에 구현하여 따로 주입받습니다.
- 주입받은 Repository 뿐만 아닌 custom 한 Repository 등, 외부 의존성이 존재하는 경우 해당 레이어에 구현합니다.

### interface
- 도메인 내에서 사용하는 interface 및 enum 등을 정의합니다.
- infrastructure 레이어에서 사용하는 추상화된 interface 등 도 해당 레이어에 정의해서 사용합니다.
- 예를 들어
    - 나이스페이에서 사용하는 request 및 response 응답 interface 등도 정의합니다.

### 폴더 구조 예시
    ```
    +---order
    |   |
    |   +---presentation
    |   |       OrderController.java
    |   |       OrderRequest.java
    |   |       OrderResponse.java
    |   |
    |   +---application
    |   |       OrderService.java
    |   |
    |   +---domain
    |   |       Order.java
    |   |       OrderValidator.java
    |   |
    |   \---infrastructure
    |       +---client
    |       |       OrderApiClient.java
    |       |
    |       +---entity
    |       |   +---exception
    |       |   |       OrderNotFoundException.java
    |       |   |
    |       |   \--- OrderEntity.java
    |       |
    |       \---repository
    |               OrderRepository.java
    ```
- 파일 이름과 내부의 class 등 의 이름은 크게 벗어나지 않게 작성해주세요. (api 레이어 제외)


# Convention
* Class, Type, Interface 등 전역으로 선언 하는 것은 **PascalCase** 로 선언
* 이외의 case 에서는 전부 **camelCase** 로 선언
* Date 형식은 다음과 같이 정의 및 접미사는 다음과 같이 사용하며 request, 및 response dto 에 포함될경우 반드시 string type 으로 전달
    * 예시
        1. 날짜 시간 전체를 표기 하는 경우 접미사는 At을 사용 ex) key : createdAt, value: 2024-09-01 00:00:00
        2. 날짜만 표기할 시 Date 로 표기 ex) key: createdDate, value: 2024-09-01
        3. 시간만 표기할경우 Time 으로 표기 ex) eky: createdTime, value: 00:00:00
* get api 설계시, return json 직렬화는 .builder() 패턴을 사용해주세요.

# 협업시 주의사항
- api 설계시, end point url restful 하게 작성해주세요.
- git 에 push 할 때 반드시 "실행 가능한" 상태로 push 해주세요.
    - 만약 env 및 타 라이브러리를 사용해 실행이 필요할경우 readme 및 해당 Repository 에 반드시 공지를 해주세요.
- 컨플릭트는 "발견한 사람" 이 수정해서 업로드 해주세요.
    - 가급적 짧은 기능 단위로 push, 자주 push 하여 컨플릭트 상태를 오래 유지하지 않도록 해주세요.
- entity 같이 다른 사람과 함께쓰는 파일은 바로 바로 push 해주세요.
    - ex) entity, repository, util 함수 등과 같은 코드
- DDL_AUTO 값은 반드시 env로 관리하며, update, none 으로만 유지해주세요.

# Database
* 컬럼 작성시 필수사항
    * id : autoincrement 키 값 사용
    * createdAt : 생성 일
    * updatedAt : 수정 일
    * deletedAt : 삭제 일
        * 생성일, 수정일, 삭제일은 비즈니스 로직에서는 "가급적" 사용하지 말아주세요 (database 관리 및 파악용도로만 사용)
* 해당 컬럼이 무엇을 의미하는지 comment 를 작성해 주세요.
* 특히 enum을 사용하든, string을 사용하든 해당 데이터베이스에 고정적으로 들어가는 값(ex: 상태값 정의 status: "ACTIVE", "EXPIRE" 일 경우에 반드시 작성해주세요.)
* softDelete 를 원칙으로 사용해 주세요. (추가 적인 요구사항이 없을 경우)
* database 상에서 (mysql 혹은 postgresql), 날짜 형식의 데이터는 가급적 "UTC Time Zone" 을 사용. (선택사항)
    * 기획 단계 검토 후 Time zone 요구사항을 맞추어야함.
  
* FK 는 가급적 안쓰는걸 추천합니다. (선택사항)
    * 관리 및 테스트할 때 FK 로 인한 어려운 사항이 간혹 있습니다.

* FK key 값은 "반드시" 다음과 같은 규칙으로 사용해주세요.
    * {데이터베이스 테이블 이름}_id
    * ex) 댓글 중에 유저가 작성했다 라고 할 경우
        * comment 테이블에 user_id 로 정의
      
* FK 시 ORM 에 관계 정의를 할 때 이름의 통일성을 지켜주세요. camelCase를 유지해주세요. 복수일경우 복수형으로, 단수일경우 단수형으로 표기해주세요.
    * ex user - comment 1:N 일 경우
        * user Entity 상에서 comments 로 정의
        * comment entity 상에서 user 로 정의 
