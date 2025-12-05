package com.pizza.libms.service;

import com.pizza.libms.common.PageResult;
import com.pizza.libms.dto.BookDTO;
import com.pizza.libms.dto.BookQuery;
import com.pizza.libms.entity.Book;
import com.pizza.libms.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookDTO getById(Long id) {
        Book b = bookRepository.findById(id);
        return b == null ? null : toDTO(b);
    }

    public Long create(Book book) {
        validateBook(book);
        if (bookRepository.findByIsbn(book.getIsbn()) != null) {
            throw new IllegalArgumentException("ISBN已存在");
        }
        bookRepository.insert(book);
        return book.getId();
    }

    public void update(Long id, Book book) {
        Book exists = bookRepository.findById(id);
        if (exists == null) { throw new IllegalArgumentException("图书不存在"); }
        if (!exists.getIsbn().equals(book.getIsbn()) && bookRepository.findByIsbn(book.getIsbn()) != null) {
            throw new IllegalArgumentException("ISBN已存在");
        }
        book.setId(id);
        validateBook(book);
        bookRepository.update(book);
    }

    public void delete(Long id) {
        long refs = bookRepository.countBorrowReferences(id);
        if (refs > 0) {
            // 业务错误码：4001 表示“被借阅记录引用，禁止删除”
            throw new com.pizza.libms.exception.BizException(4001, "该图书存在借阅记录，禁止删除");
        }
        bookRepository.deleteById(id);
    }

    public PageResult<BookDTO> page(BookQuery query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 100);
        int offset = (page - 1) * size;
        long total = bookRepository.count(query.getTitle(), query.getAuthor(), query.getIsbn(), query.getCategory());
        List<BookDTO> list = bookRepository.list(query.getTitle(), query.getAuthor(), query.getIsbn(), query.getCategory(), offset, size)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new PageResult<>(total, page, size, list);
    }

    private void validateBook(Book b) {
        if (b.getTotalCopies() == null || b.getTotalCopies() < 0) {
            throw new IllegalArgumentException("总库存必须为非负");
        }
        if (b.getAvailableCopies() == null || b.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("可用库存必须为非负");
        }
        if (b.getAvailableCopies() > b.getTotalCopies()) {
            throw new IllegalArgumentException("可用库存不能大于总库存");
        }
    }

    private BookDTO toDTO(Book b) {
        BookDTO dto = new BookDTO();
        dto.setId(b.getId());
        dto.setTitle(b.getTitle());
        dto.setAuthor(b.getAuthor());
        dto.setIsbn(b.getIsbn());
        dto.setCategory(b.getCategory());
        dto.setTotalCopies(b.getTotalCopies());
        dto.setAvailableCopies(b.getAvailableCopies());
        dto.setPublishedDate(b.getPublishedDate());
        return dto;
    }
}
