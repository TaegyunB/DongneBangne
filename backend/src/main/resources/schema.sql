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
    user_role           ENUM('ADMIN', 'MEMBER', 'GUEST'),
    personal_point      BIGINT DEFAULT 0, -- 추가
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL
);

-- SeniorCenter 테이블 (admin_user_id 제거, total_point 오타 수정)
CREATE TABLE senior_center (
    senior_center_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
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

-- AiNews 테이블 (먼저 생성 - Challenge에서 참조하므로)
CREATE TABLE ai_news (
    ai_news_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_title      VARCHAR(255) NOT NULL,
    news_content    TEXT,
    year            INT NOT NULL,
    month           INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL
);

-- Board 테이블 (like_count 컬럼명 수정)
CREATE TABLE board (
    board_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    title           VARCHAR(50),
    content         VARCHAR(50),
    image           VARCHAR(255) NULL,
    like_count      INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

-- BoardLike 테이블 (변경사항 없음)
CREATE TABLE board_like (
    board_like_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id        BIGINT,
    user_id         BIGINT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL,
    FOREIGN KEY (board_id) REFERENCES board(board_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL
);

-- Comment 테이블 (변경사항 없음)
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

-- GameType 테이블 (변경사항 없음)
CREATE TABLE game_type (
    game_type_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_name       VARCHAR(255) NOT NULL,
    game_point      INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL
);

-- GameResult 테이블 (PROGRESS 오타 수정, loser_id 제거)
CREATE TABLE game_result (
    game_result_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_type_id    BIGINT,
    player1_id      BIGINT,
    player2_id      BIGINT,
    winner_id       BIGINT,
    earned_point    INT,
    status          ENUM('DONE', 'PROGRESS'),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL,
    FOREIGN KEY (game_type_id) REFERENCES game_type(game_type_id) ON UPDATE CASCADE,
    FOREIGN KEY (player1_id) REFERENCES senior_center(senior_center_id) ON DELETE SET NULL,
    FOREIGN KEY (player2_id) REFERENCES senior_center(senior_center_id) ON DELETE SET NULL,
    FOREIGN KEY (winner_id) REFERENCES senior_center(senior_center_id) ON DELETE SET NULL
);

-- Challenge 테이블 (ai_news 테이블 생성 후)
CREATE TABLE challenge (
    challenge_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    senior_center_id    BIGINT NOT NULL,
    ai_news_id          BIGINT,
    challenge_title     VARCHAR(255) NOT NULL,
    challenge_place     VARCHAR(255),
    description         TEXT,
    year                INT NOT NULL,
    month               INT NOT NULL,
    point               INT NOT NULL,
    challenge_image     VARCHAR(255),
    image_description   TEXT,
    is_success          BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL,
    FOREIGN KEY (senior_center_id) REFERENCES senior_center(senior_center_id) ON DELETE CASCADE,
    FOREIGN KEY (ai_news_id) REFERENCES ai_news(ai_news_id) ON DELETE SET NULL
);

-- -- Ranking 테이블 (rank를 ranking으로 변경, ai_news_id 추가)
-- CREATE TABLE ranking (
--     ranking_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
--     senior_center_id    BIGINT NOT NULL,
--     ai_news_id          BIGINT,
--     ranking             INT NOT NULL,
--     year                INT NOT NULL,
--     month               INT NOT NULL,
--     created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at          TIMESTAMP NULL,
--     FOREIGN KEY (senior_center_id) REFERENCES senior_center(senior_center_id) ON DELETE CASCADE,
--     FOREIGN KEY (ai_news_id) REFERENCES ai_news(ai_news_id) ON DELETE SET NULL
-- );

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