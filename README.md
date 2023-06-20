# yalooStore-auth
- spring security - authentication 관련 정리 레포지토리 : https://github.com/yeomyaloo/spring-security-practice
# 구성
인증, 인가 관련 작업을 처리하는 서버입니다.
- 인증 작업은 front server(client)에서 요청이 들어오면 여기서 처리를 진행하고 jwt, redis 저장 작업을 진행하여 넘겨줍니다.
- 인가 작업 역시 해당 페이지에 인가 작업이 필요하다면 이곳에서 진행하여 넘겨줄 수 있게 합니다.

# 해당 구조의 흐름
![image](https://github.com/yalooStore/yalooStore-auth/assets/81970382/fd69041c-560c-4586-af91-60e8bed36d8a)

# 설명
인증 작업
- Front 서버에서 넘어오는 요청(request)를 넘겨받아 해당 정보로 인증 작업을 진행해주고 해당 인증 작업이 성공적이라면 Jwt 발급과 발급한 Jwt 정보를 redis에 저장합니다.
- 이때 회원 정보를 가지고 있는 DB는 API 서버에서 관리하고 있기 때문에 해당 회원 정보를 API에게 요청하고 회원 정보를 대조해줍니다..
- 실패한다면 Front 서버에 있는 로그인 폼으로 다시 돌아갈 수 있게 합니다,

인가 작업
- 인가 작업을 이곳에서 진행합니다.

