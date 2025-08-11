package S13P11A708.backend.service;

import S13P11A708.backend.domain.Board;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.BoardCategory;
import S13P11A708.backend.dto.request.board.CreateBoardRequestDto;
import S13P11A708.backend.dto.request.board.UpdateBoardRequestDto;
import S13P11A708.backend.dto.response.board.BoardDetailResponseDto;
import S13P11A708.backend.repository.BoardRepository;
import S13P11A708.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final S3Service s3Service;

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

    /**
     * 게시글 상세 조회
     */
    public BoardDetailResponseDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        return BoardDetailResponseDto.from(board);
    }

    /**
     * 게시글 작성
     */
    public BoardDetailResponseDto createBoard(Long userId, CreateBoardRequestDto requestDto, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = s3Service.uploadMultipartFile(imageFile, "board");

            } catch (Exception e) {
                throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
            }
        }

        Board board = Board.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(requestDto.getBoardCategory())
                .boardImage(imageUrl)
                .likeCount(0)
                .user(user)
                .build();

        Board savedBoard = boardRepository.save(board);
        return BoardDetailResponseDto.from(savedBoard);
    }

    /**
     * 게시글 수정
     */
    public BoardDetailResponseDto updateBoard(Long userId, Long boardId, UpdateBoardRequestDto requestDto, MultipartFile imageFile) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!board.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }

        // 게시글 내용 수정
        board.updateBoard(requestDto.getTitle(), requestDto.getContent(), requestDto.getBoardCategory());

        // 이미지 처리
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 기존 이미지 삭제
                String newImageUrl = s3Service.uploadMultipartFile(imageFile, "board");
                board.updateBoardImage(newImageUrl);
            } catch (Exception e) {
                throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
            }
        }

        Board updateBoard = boardRepository.save(board);
        return BoardDetailResponseDto.from(updateBoard);

    }

    /**
     * 게시글 삭제
     */
    public void deleteBoard(Long userId, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!board.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }

        boardRepository.delete(board);
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
