package org.example.test;

import org.example.model.SomeDocument;
import org.example.model.SomeEnum;
import org.example.repository.MyCouchbaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;

public class SomeTest extends TestBase {

    @Autowired
    private MyCouchbaseRepository myCouchbaseRepository;

    @Test
    public void shouldNotCrashWhenQueryingByEnum() {
        // given
        myCouchbaseRepository.save(SomeDocument.builder()
                .withId(UUID.randomUUID().toString())
                .withValue("1")
                .withStatus(SomeEnum.A)
                .build());
        // when
        var res = myCouchbaseRepository.findByStatus(SomeEnum.A, Pageable.ofSize(10));
        // then
        assertEquals(1, res.size());
    }

    @Test
    public void shouldNotCrashWhenUpdatingByEnum() {
        // given
        myCouchbaseRepository.save(SomeDocument.builder()
                .withId(UUID.randomUUID().toString())
                .withValue("1")
                .withStatus(SomeEnum.A)
                .build());
        // when
        var res = myCouchbaseRepository.changeStatus(Collections.singleton("1"), SomeEnum.B);
        // then
        assertEquals(1, res.size());
    }
}
