package S13P11A708.backend.controller;

import S13P11A708.backend.domain.enums.BoardCategory;
import S13P11A708.backend.dto.response.board.BoardDetailResponseDto;
import S13P11A708.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<List<BoardDetailResponseDto>> getBoards(
            @RequestParam(value = "category", defaultValue = "ALL") String category) {

        BoardCategory boardCategory = boardService.convertToCategory(category);

        List<BoardDetailResponseDto> boards = boardService.getBoardsByCategory(boardCategory);

        return ResponseEntity.ok(boards);
    }

}
