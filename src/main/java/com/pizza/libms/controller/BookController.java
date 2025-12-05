package com.pizza.libms.controller;

import com.pizza.libms.common.ApiResponse;
import com.pizza.libms.common.PageResult;
import com.pizza.libms.dto.BookDTO;
import com.pizza.libms.dto.BookQuery;
import com.pizza.libms.entity.Book;
import com.pizza.libms.service.BookService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/books")
@Validated
public class BookController {

    private final BookService bookService;
    public BookController(BookService bookService) { this.bookService = bookService; }

    private record CreateReq(@NotBlank String title, @NotBlank String author,
                             @NotBlank String isbn, @NotBlank String category,
                             @NotNull @Min(0) Integer totalCopies,
                             @NotNull @Min(0) Integer availableCopies,
                             LocalDate publishedDate) {}

    private record UpdateReq(@NotBlank String title, @NotBlank String author,
                             @NotBlank String isbn, @NotBlank String category,
                             @NotNull @Min(0) Integer totalCopies,
                             @NotNull @Min(0) Integer availableCopies,
                             LocalDate publishedDate) {}

    @GetMapping("/{id}")
    public ApiResponse<BookDTO> get(@PathVariable("id") Long id) {
        BookDTO dto = bookService.getById(id);
        if (dto == null) { return ApiResponse.fail(404, "图书不存在"); }
        return ApiResponse.success(dto);
    }

    @GetMapping
    public ApiResponse<PageResult<BookDTO>> page(@RequestParam(value = "title", required = false) String title,
                                                 @RequestParam(value = "author", required = false) String author,
                                                 @RequestParam(value = "isbn", required = false) String isbn,
                                                 @RequestParam(value = "category", required = false) String category,
                                                 @RequestParam(value = "page", required = false) Integer page,
                                                 @RequestParam(value = "size", required = false) Integer size) {
        BookQuery q = new BookQuery();
        q.setTitle(title);
        q.setAuthor(author);
        q.setIsbn(isbn);
        q.setCategory(category);
        if (page != null) q.setPage(page);
        if (size != null) q.setSize(size);
        return ApiResponse.success(bookService.page(q));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody CreateReq req) {
        Book b = new Book();
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setIsbn(req.isbn());
        b.setCategory(req.category());
        b.setTotalCopies(req.totalCopies());
        b.setAvailableCopies(req.availableCopies());
        b.setPublishedDate(req.publishedDate());
        Long id = bookService.create(b);
        return ApiResponse.success(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody UpdateReq req) {
        Book b = new Book();
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setIsbn(req.isbn());
        b.setCategory(req.category());
        b.setTotalCopies(req.totalCopies());
        b.setAvailableCopies(req.availableCopies());
        b.setPublishedDate(req.publishedDate());
        bookService.update(id, b);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        bookService.delete(id);
        return ApiResponse.success(null);
    }
}
