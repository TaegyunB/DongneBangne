DROP DATABASE IF EXISTS dongnae;
CREATE DATABASE IF NOT EXISTS dongnae;
USE dongnae;

-- User 테이블 (카카오 OAuth 필드들 추가)
CREATE TABLE user (
    user_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    senior_center_id    BIGINT,
    kakao_id			VARCHAR(100), -- kakao 1234567890
    nickname            VARCHAR(100) NOT NULL, -- 홍길동
    profile_image       VARCHAR(255),
    personal_point      BIGINT DEFAULT 0,
    user_role           ENUM('ADMIN', 'MEMBER', 'GUEST'),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL
);

-- SeniorCenter 테이블
CREATE TABLE senior_center (
    senior_center_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id       BIGINT,             
    center_name         VARCHAR(255) NOT NULL,
    address             VARCHAR(255) NOT NULL,
    trot_point          BIGINT DEFAULT 0,
    challenge_point     BIGINT DEFAULT 0,
    total_point         BIGINT DEFAULT 0,
    ranking_year        INT,
    ranking_month       INT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL
);

-- User 테이블에 외래키 제약 조건 추가
ALTER TABLE user
ADD CONSTRAINT fk_user_senior_center
FOREIGN KEY (senior_center_id)
REFERENCES senior_center(senior_center_id)
ON DELETE SET NULL;

-- SeniorCenter 테이블에 admin_user_id 외래키 제약 조건 추가
ALTER TABLE senior_center
ADD CONSTRAINT fk_senior_center_admin_user
FOREIGN KEY (admin_user_id)
REFERENCES user(user_id)
ON DELETE SET NULL;

-- AiNews 테이블
CREATE TABLE ai_news (
    ai_news_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    senior_center_id    BIGINT NOT NULL,              -- 어느 경로당의 신문인지
    news_title          VARCHAR(255) NOT NULL,        -- 백엔드에서 생성한 제목
    year                INT NOT NULL,
    month               INT NOT NULL,
    pdf_url             VARCHAR(500),                 -- 생성된 PDF 파일 URL
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL,
    FOREIGN KEY (senior_center_id) REFERENCES senior_center(senior_center_id) ON DELETE CASCADE
);

-- Board 테이블
CREATE TABLE board (
    board_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    title           VARCHAR(500),
    content         VARCHAR(1000),
    category        ENUM('ALL', 'POPULAR', 'CHAT', 'SHARE', 'HOBBY', 'INFO'),
    board_image     VARCHAR(255) NULL,
    like_count      INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

-- BoardLike 테이블
CREATE TABLE board_like (
    board_like_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id        BIGINT,
    user_id         BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL,
    FOREIGN KEY (board_id) REFERENCES board(board_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

-- Comment 테이블
CREATE TABLE comment (
    comment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id        BIGINT,
    user_id         BIGINT,
    content         VARCHAR(1000),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (board_id) REFERENCES board(board_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

-- game_room 게임방 정보
CREATE TABLE game_room (
    game_room_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_title         VARCHAR(100) NOT NULL,
    game_round         INT NOT NULL,
    music_era          VARCHAR(50),
    category           VARCHAR(50),
    game_status        ENUM('WAITING', 'PROGRESS', 'FINISHED') DEFAULT 'WAITING',
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- game_room_user 방에 참가한 사용자 정보와 준비 상태
CREATE TABLE game_room_user (
	  game_room_user_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_room_id       BIGINT NOT NULL,
    user_id            BIGINT NOT NULL,
    ready              BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_roomuser_room FOREIGN KEY (game_room_id) REFERENCES game_room(game_room_id),
    CONSTRAINT fk_roomuser_user FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 게임 기록 table
CREATE TABLE game_history (
    game_history_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_room_id       BIGINT NOT NULL,
    winner_user_id     BIGINT,
    game_round         INT,
    music_era          VARCHAR(50),
    category           VARCHAR(50),
    started_at         TIMESTAMP,
    ended_at           TIMESTAMP,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_room FOREIGN KEY (game_room_id) REFERENCES game_room(game_room_id),
    CONSTRAINT fk_history_winner FOREIGN KEY (winner_user_id) REFERENCES user(user_id)
);

-- 각 유저에 대한 게임 기록
CREATE TABLE game_history_user (
    game_history_user_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_history_id        BIGINT NOT NULL,
    user_id                BIGINT NOT NULL, -- 해당 기록의 주인
    senior_center_id       BIGINT NOT NULL, -- user_id가 속한 경로당 id
    correct_count          INT DEFAULT 0,
    hint_used              INT DEFAULT 0,
    is_winner              BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_histuser_history FOREIGN KEY (game_history_id) REFERENCES game_history(game_history_id),
    CONSTRAINT fk_histuser_user FOREIGN KEY (user_id) REFERENCES user(user_id),
    CONSTRAINT fk_histuser_center FOREIGN KEY (senior_center_id) REFERENCES senior_center(senior_center_id)
);

-- Challenge 테이블 (AI 신문 생성을 위한 미션 데이터 - 수정됨)
CREATE TABLE challenge (
    challenge_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    senior_center_id    BIGINT NOT NULL,
    ai_news_id          BIGINT,                       -- AI 신문과의 연관관계
    challenge_title     VARCHAR(255) NOT NULL,
    challenge_place     VARCHAR(255),
    description         TEXT,
    year                INT NOT NULL,
    month               INT NOT NULL,
    point               INT NOT NULL DEFAULT 300,     -- 기본 포인트 300점 설정
    challenge_image     VARCHAR(255),                 -- AI 신문에 포함될 이미지
    image_description   TEXT,                         -- 이미지 설명 (AI 신문 생성시 활용)
    ai_description      LONGTEXT,                     -- ✅ 추가: AI가 생성한 개별 기사 내용
    is_success          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL,
    FOREIGN KEY (senior_center_id) REFERENCES senior_center(senior_center_id) ON DELETE CASCADE,
    FOREIGN KEY (ai_news_id) REFERENCES ai_news(ai_news_id) ON DELETE SET NULL
);

-- ChallengeReport 테이블 (단순 버전으로 수정)
CREATE TABLE challenge_report (
    challenge_report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id        BIGINT NOT NULL,
    report_user_id      BIGINT,
    reason              VARCHAR(1000) NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL,
    FOREIGN KEY (challenge_id) REFERENCES challenge(challenge_id) ON DELETE CASCADE,
    FOREIGN KEY (report_user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

-- AI 신문 생성 성능 최적화를 위한 인덱스 추가
CREATE INDEX idx_challenge_senior_center_year_month ON challenge(senior_center_id, year, month);
CREATE INDEX idx_challenge_success ON challenge(is_success);
CREATE INDEX idx_ai_news_senior_center_year_month ON ai_news(senior_center_id, year, month);
CREATE INDEX idx_senior_center_admin_user ON senior_center(admin_user_id);