package S13P11A708.backend.domain.enums;

public enum GameMessageType {
    /**
     * 1. 게임 흐름
     */
    GAME_START,     //게임 시작
    ROUND_QUESTION, //문제 전송
    ROUND_END,      //라운드 종료
    GAME_END,       //게임 종료

    /**
     * 2. 정답 관련
     */
    ANSWER_SUBMIT,  //참가자 정답 제출
    ANSWER_RESULT,  //정답 여부 전송
    ANSWER_REJECTED, //정답 제출이 무효(EX. 중복 제출)

    /**
     * 3. 힌트 관련
     */
    HINT_REQUEST,   //힌트 요청
    HINT_RESPONSE,  //힌트 제공
    HINT_REJECTED,  //힌트 사용 실패 (포인트 부족)

    /**
     * 4. 포인트 관련
     */
    POINT_UPDATE,   //개인 포인트 변경 알림(힌트 사용/ 정답 보상)

    /**
     * 5. 시스템 / 유저 상태 관련
     */
    USER_JOINED,    //대기 방 구현시 사용가능
    USER_LEFT,      //유저 연결이 끊겼을 때,
    SYSTEM_MESSAGE  // 일반 안내 메세지
}
