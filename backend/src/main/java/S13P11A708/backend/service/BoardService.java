package S13P11A708.backend.service;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.enums.BoardCategory;
import S13P11A708.backend.dto.response.board.BoardDetailResponseDto;
import S13P11A708.backend.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 카테고리별 조회
     */
    public List<BoardDetailResponseDto> getBoardsByCategory(BoardCategory category) {
        List<Board> boards;

        switch (category) {
            case ALL:
                boards = boardRepository.findAllOrderByCreatedAtDesc();
                break;

            case POPULAR:
                boards = boardRepository.findPopularBoardOrderByCreatedAtDesc(30);
                break;

            case CHAT:
            case SHARE:
            case HOBBY:
            case INFO:
                boards = boardRepository.findByCategoryOrderByCreatedAtDesc(category);
                break;

            default:
                boards = boardRepository.findAllOrderByCreatedAtDesc();
                break;
        }

        return boards.stream()
                .map(BoardDetailResponseDto::from)
                .collect(Collectors.toList());

    }

    //== 헬퍼 메서드 ==//
    /**
     * String을 BoardCategory로 변환하는 핼퍼 메서드
     */
    public BoardCategory convertToCategory(String category) {
        try {
            return BoardCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BoardCategory.ALL;
        }
    }
}
