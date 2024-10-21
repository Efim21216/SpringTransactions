package com.delfim.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final PersonRepository personRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean takeBook(Long personId, Long bookId) throws InterruptedException {
        Person person = personRepository.findById(personId).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();
        Thread.sleep(1000);
        if (person.getAge() > 18 && book.getOwner() == null) {
            book.setOwner(person);
            bookRepository.save(book);
            return true;
        }
        return false;
    }
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }
    public Book getBook(Long id) {
        return bookRepository.findById(id).orElseThrow();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void repeatingBookRead(List<Long> bookIds) {
        long waitTime = 500L;
        bookIds.forEach(bookId -> oneBookRead(bookId, waitTime));
    }
    private void oneBookRead(Long bookId, Long wait) {
        log.info(bookRepository.findById(bookId).orElseThrow().getTitle());
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
