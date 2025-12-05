package com.pizza.libms.controller;

import com.pizza.libms.common.ApiResponse;
import com.pizza.libms.common.PageResult;
import com.pizza.libms.dto.BorrowRecordDTO;
import com.pizza.libms.service.BorrowService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/borrows")
@Validated
public class BorrowController {

    private final BorrowService borrowService;
    public BorrowController(BorrowService borrowService) { this.borrowService = borrowService; }

    private record BorrowReq(@NotNull @Min(1) Long userId,
                             @NotNull @Min(1) Long bookId,
                             @NotNull LocalDate borrowDate,
                             @NotNull LocalDate dueDate) {}

    private record ReturnReq(@NotNull LocalDate returnDate) {}

    @PostMapping
    public ApiResponse<Long> borrow(@RequestBody BorrowReq req) {
        Long id = borrowService.borrow(req.userId(), req.bookId(), req.borrowDate(), req.dueDate());
        return ApiResponse.success(id);
    }

    @PutMapping("/{id}/return")
    public ApiResponse<Void> returnBook(@PathVariable("id") Long id, @RequestBody ReturnReq req) {
        borrowService.returnBook(id, req.returnDate());
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<PageResult<BorrowRecordDTO>> page(@RequestParam(value = "userId", required = false) Long userId,
                                                        @RequestParam(value = "bookId", required = false) Long bookId,
                                                        @RequestParam(value = "status", required = false) String status,
                                                        @RequestParam(value = "page", required = false) Integer page,
                                                        @RequestParam(value = "size", required = false) Integer size) {
        return ApiResponse.success(borrowService.page(userId, bookId, status, page, size));
    }

    /**
     * 查询指定用户与 ISBN 的所有未归还借阅记录（用于前端归还列表）
     */
    @GetMapping("/active")
    public ApiResponse<java.util.List<BorrowRecordDTO>> active(@RequestParam("userId") Long userId,
                                                               @RequestParam("isbn") String isbn) {
        return ApiResponse.success(borrowService.activeBorrowsByUserAndIsbn(userId, isbn));
    }
}
