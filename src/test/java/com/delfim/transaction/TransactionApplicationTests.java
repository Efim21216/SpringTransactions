package com.delfim.transaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
@Slf4j
@SpringBootTest
class TransactionApplicationTests {
	@Autowired
	private BookService bookService;

	@Test
	void concurrentBook() throws InterruptedException {
		Book book = bookService.saveBook(new Book(null, "Book 1", null));
		Person person1 = bookService.savePerson(new Person(null, "Person 1", 20));
		Person person2 = bookService.savePerson(new Person(null, "Person 2", 20));
		Thread thread1 = new Thread(() -> {
            try {
                log.info("Second person result {}", bookService.takeBook(person2.getId(), book.getId()));
            } catch (InterruptedException e) {
                log.info("Fail book in thread1 {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
		thread1.start();
		try {
			log.info("First person result {}", bookService.takeBook(person1.getId(), book.getId()));
		} catch (InterruptedException e) {
			log.info("Fail book {}", e.getMessage());
			throw new RuntimeException(e);
		}
		thread1.join();
		log.info("Book owner is {}", bookService.getBook(book.getId()).getOwner().getName());
	}

	@Test
	void testNonRepeatableReadAndPhantomRead() throws InterruptedException {
		Book book = bookService.saveBook(new Book(null, "Book 1", null));
		Thread thread1 = new Thread(() -> bookService.repeatingBookRead(List.of(1L, 1L, 2L)));
		thread1.start();
		Thread.sleep(200L);
		book.setTitle("Book 1 updated");
		book = bookService.saveBook(book);
		log.info("Saved book: {}", book.getTitle());
		bookService.saveBook(new Book(null, "Book 2", null));
	}
}
