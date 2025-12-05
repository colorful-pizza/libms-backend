package com.pizza.libms.service;

import com.pizza.libms.common.PageResult;
import com.pizza.libms.dto.BorrowRecordDTO;
import com.pizza.libms.entity.Book;
import com.pizza.libms.entity.BorrowRecord;
import com.pizza.libms.repository.BookRepository;
import com.pizza.libms.repository.BorrowRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;

    public BorrowService(BorrowRecordRepository borrowRecordRepository, BookRepository bookRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
    }

    public Long borrow(Long userId, Long bookId, LocalDate borrowDate, LocalDate dueDate) {
        Book book = bookRepository.findById(bookId);
        if (book == null) throw new com.pizza.libms.exception.BizException(4002, "图书不存在");
        if (book.getAvailableCopies() == null || book.getAvailableCopies() < 1) {
            throw new com.pizza.libms.exception.BizException(4003, "库存不足，无法借阅");
        }
        // 扣减库存
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.update(book);

        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(borrowDate);
        record.setDueDate(dueDate);
        record.setStatus("BORROWED");
        borrowRecordRepository.insert(record);
        return record.getId();
    }

    public void returnBook(Long recordId, LocalDate returnDate) {
        BorrowRecord record = borrowRecordRepository.findById(recordId);
        if (record == null) throw new com.pizza.libms.exception.BizException(4004, "借阅记录不存在");
        if (!"BORROWED".equals(record.getStatus())) {
            throw new com.pizza.libms.exception.BizException(4005, "该记录已归还或状态异常");
        }
        // 增加库存
        Book book = bookRepository.findById(record.getBookId());
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.update(book);
        }
        borrowRecordRepository.updateReturn(recordId, returnDate, "RETURNED");
    }

    public PageResult<BorrowRecordDTO> page(Long userId, Long bookId, String status, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 100);
        int offset = (p - 1) * s;
        long total = borrowRecordRepository.count(userId, bookId, status);
        List<BorrowRecordDTO> list = borrowRecordRepository.list(userId, bookId, status, offset, s)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new PageResult<>(total, p, s, list);
    }

    private BorrowRecordDTO toDTO(BorrowRecord r) {
        BorrowRecordDTO dto = new BorrowRecordDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUserId());
        dto.setBookId(r.getBookId());
        dto.setBorrowDate(r.getBorrowDate());
        dto.setDueDate(r.getDueDate());
        dto.setReturnDate(r.getReturnDate());
        dto.setStatus(r.getStatus());
        return dto;
    }

    /**
     * 查询用户某 ISBN 下所有未归还的借阅记录
     */
    public java.util.List<BorrowRecordDTO> activeBorrowsByUserAndIsbn(Long userId, String isbn) {
        if (userId == null || isbn == null || isbn.isBlank()) {
            return java.util.Collections.emptyList();
        }
        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            return java.util.Collections.emptyList();
        }
        return borrowRecordRepository.listAll(userId, book.getId(), "BORROWED")
                .stream().map(this::toDTO).collect(java.util.stream.Collectors.toList());
    }
}
