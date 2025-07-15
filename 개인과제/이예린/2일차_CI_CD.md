## CI/CD란?
- CI/CD(지속적 통합 및 지속적 배포)

## CI (Continuous Integration), 지속적 통합
- 개발자가 `코드를 공유 저장소에 자주 통합(merge)`하는 프로제스
- 이 과정에서 자동화된 빌드와 테스트가 실행되어, 코드 변경이 기존 코드에 미치는 영향을 빠르게 확인할 수 있음
- `코드 변경이 발생할 때마다 자동으로 테스트를 수행하여 빌드 실패와 버그를 조기에 발견`할 수 있도록 도움

1. 젠킨스
2. github action 

## CD (Continuous Delivery), 지속적 배포
- `CI 후에 코드가 프로덕션 환경에 자동으로 배포될 수 있도록 준비`하는 프로제스
- 이는 코드 변경이 수동 개입 없이도 안전하게 배포될 수 있도록 보장함
- CI만 사용하는 경우, 코드를 자주 통합하더라도 배포 과정이 수동적이고 느려 새로운 기능이나 버그 수정을 신속하게 사용자에게 전달하기 어려움
- CD는 CI를 통해 테스트가 완료된 코드를 즉시 프로덕션에 반영함으로써, 이러한 문제를 시스템적으로 해결함

> `CI가 자동으로 테스트`하고, 문제가 없으면 `CD가 검증된 코드를 프로덕션에 반영`함

1. 도커
2. code deploy
3. 스크립트 (스크립트로 jar 파일을 실행)

## CI/CD 파이프라인
- 빌드 단계: 소스코드를 컴파일하고, 필요한 라이브러리를 다운로드하며, 애플리케이션을 패키징함
- 테스트 단계: 유닛 테슽, 통합 테스트, 기능 테스트 등을 실행하여 코드의 품질을 검증함
- 배포 단계: 테스트가 통과된 코드를 스테이징 환경이나 프로덕션 환경에 배포함. 여기에는 `Docker 이미지 생성 및 배포`, `Kubernetes 클러스터에 배포` 등이 포함될 수 있음
- 모니터링 및 피드백 단계: 배포된 애플리케이션의 성능과 안정성을 모니터링하고, 로그 및 사용자 피드백을 수집함

## CI/CD 개발도구
CI/CD 개발 도구는 다양하게 존재한다.
- 서버에 설치하여 사용하는 `Jenkins CI/CD`
- 형상 관리 프로그램 Git을 통해 쉽게 사용할 수 있는 `GitLab CI/CD`
- 클라우드 기반으로 파이프라인을 설계하는 `AWS CI/CD`

### Jenkins CI/CD
- 대표적인 오픈 소스 CI/CD 도구
- Java Runtime Environment에서 동작하는 서버 설치형 CI/CD 프로그램
- Jenkins는 배포에 필요한 리소스에 접근하기 위해 여러 중요한 정보(AWS 토큰, Git 액세스 토큰, SSH 키 등)를 저장해야 하는데, 이를 위한 Credentials Plugin도 직접 설정해야 함
  - Git 소스 코드 접근을 관리하는 Git Plugin
  - 컴포넌트 빌드를 위한 Docker Plugin
  - 파이프라인 설계 기능인 Pipeline Plugin
- 모두 플러그인으로 제공되며, 필요한 기능을 직접 설치하여 사용해야 함

### GitLab CI/CD
- 개발자가 Remote Repository에 코드를 올리면(code push) CI 프로그램이 빌드와 테스트를 진행함
- GitLab CI/CD는 `GitLab의 코드 변경을 자동으로 트리거`하여 빌드, 테스트, 배포 과정을 수행함
- `YAML 파일을 사용하여 파이프라인을 정의`할 수 있음
- 마켓 플레이스에서 기작성된 워크플로우를 다운로드하여 쉽게 사용할 수 있음
- 유사하게 Remote Repository 플랫폼에서 제공하는 CI/CD 개발도구로는 `GitHub Actions`가 있음

### AWS CI/CD
- 개발자는 코드 변경 사항을 즉시 배포할 수 있으며, 이는 애플리케이션의 신뢰성과 속도를 크게 향상시킴
- `AWS CodeCommit(소스 코드 저장소)`, `AWS CodeBuild(빌드 서비스)`, `AWS CodeDeploy(배포 서비스)`, `AWS CodePipeline(파이프라인 관리)`
- 특히 `AWS CodePipeline`은 다양한 AWS 서비스와의 통합을 통해 자동화된 배포 프로세스를 제공하여 개발자의 생산성을 높임

## .github/workflows/cicd.yml
### 구성요소
- Workflow
  - 한개 이상의 job (CI/CD)을 실행할 수 있는 일종의 프로세스
  - YAML 파일 (.yml 등)과 문법으로 구성되며 내부 스크립트에 정의된 event로 워크플로우가 실행됨

- Event
  - workflow가 실행되기 위한 특정한 행동을 가리킴
  - 예를 들어, GitHub에서 사용되는 `push`, `pull request`, `merge` 등 코드를 관리하는 이벤트들이 이에 해당됨

- Jobs
  - 워크플로우에서 `특정 이벤트에 따라 처리하는 프로세스`를 구분하고 정의할 수 있음
  - 프로세스는 각각의 `step`으로 나뉘고 이 step은 `s`hell에서 동작하는 CLI와 동일하게 실행`됨
  - 각각의 step들 정의한 순서대로 실행되며, step 별로 동일한 환경변수를 지정할 수 있어 `데이터를 공유`할 수 있음
  - 하나의 job이 실행되기 전에 다른 job이 무조건 실행되어야 하는 것처럼 `의존관계`를 가질 수 있으며 `병렬적으로 실행`할 수 있음

- Actions
  - 반복되는 코드를 모듈이나 함수로 관리하는 것처럼 `복잡하고 자주 사용되는 작업을 정의`할 수 있음
  - 워크플로우 내에서 `자주 반복되는 스크립트를 미리 정의`하여 좀 더 효율적으로 관리할 수 있음
  - 이미 많은 사용자들이 불편함을 느껴 GitHub 마켓플레이스에 `퍼블릭하게 배포해 놓은 action들`이 많이 있으니, 참고하여 각자 개발 프로세스에 맞는 CI/CD를 구축할 수 있음

- Runner
  - Runner는 `workflow가 트리거되었을 때 동작하는 서버`
  - 일종의 `VM`이라 볼 수도 있음
  - 각각의 Runner는 `한 번에 하나의 Job을 실행`할 수 있음

- env
  - 환경변수가 반드시 필요한 구성요소는 아니지만, `노출되어서는 안되는 민감한 정보를 환경변수로 설정`하여 사용할 수 있음
  - Repository별로 환경변수를 독립적으로 설정할 수 있고, Organization을 사용한다면, 모든 Repository에 적용되는 환경변수를 관리할 수 있음

> Settings -> Secrets and variables (Actions) -> New Repository secret 순으로 이동

```text
- name: Environment Setup
  run: ${{ secrets.등록한_환경변수_이름 }}
```

## with AWS
### .github/workflows/deploy.yml
```text
name: Spring Boot CI/CD

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
    - name: 코드 체크아웃
      uses: actions/checkout@v3

    - name: JDK 설치
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Gradle 빌드
      run: ./gradlew clean build

    - name: EC2 서버에 배포
      uses: appleboy/scp-action@v0.1.3
      with:
        host: ${{ secrets.HOST }}
        username: ${{ secrets.USERNAME }}
        key: ${{ secrets.SSH_PRIVATE_KEY }}
        source: "build/libs/*.jar"
        target: "/home/ubuntu/app"

    - name: SSH 접속 후 실행
      uses: appleboy/ssh-action@v1.0.0
      with:
        host: ${{ secrets.HOST }}
        username: ${{ secrets.USERNAME }}
        key: ${{ secrets.SSH_PRIVATE_KEY }}
        script: |
          pkill -f 'java' || true
          nohup java -jar /home/ubuntu/app/*.jar > app.log 2>&1 &
```

### Spring Boot CI/CD 핵심 흐름 요약
```text
[코드 푸시]
   ↓
[GitHub Actions 트리거]
   ↓
[Gradle로 Build & Test]
   ↓
[Jar 파일 생성]
   ↓
[서버로 전송 및 실행]
   ↓
[Spring Boot 서비스 자동 재시작]
```