package S13P11A708.backend.controller;

        import S13P11A708.backend.domain.enums.BoardCategory;
        import S13P11A708.backend.dto.request.board.BoardCreateRequestDto;
        import S13P11A708.backend.dto.response.board.BoardDetailResponseDto;
        import S13P11A708.backend.security.CustomOAuth2User;
        import S13P11A708.backend.service.BoardService;
        import jakarta.validation.Valid;
        import lombok.RequiredArgsConstructor;
        import org.springframework.http.ResponseEntity;
        import org.springframework.security.core.annotation.AuthenticationPrincipal;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;

        import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardController {

    private final BoardService boardService;

    /**
     * 카테고리별 게시글 조회
     */
    @GetMapping
    public ResponseEntity<List<BoardDetailResponseDto>> getBoards(
            @RequestParam(value = "category", defaultValue = "ALL") String category) {

        BoardCategory boardCategory = boardService.convertToCategory(category);

        List<BoardDetailResponseDto> boards = boardService.getBoardsByCategory(boardCategory);

        return ResponseEntity.ok(boards);
    }

    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<BoardDetailResponseDto> createBoard(
            @AuthenticationPrincipal CustomOAuth2User customUser,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("boardCategory") String boardCategory,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile){

        Long userId = customUser.getUserId();

        BoardCreateRequestDto requestDto = BoardCreateRequestDto.builder()
                .title(title)
                .content(content)
                .boardCategory(BoardCategory.valueOf(boardCategory.toUpperCase()))
                .build();

        BoardDetailResponseDto createBoard = boardService.createBoard(userId, requestDto, imageFile);
        return ResponseEntity.ok(createBoard);

    }




}
