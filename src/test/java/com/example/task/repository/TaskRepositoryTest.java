package com.example.task.repository;

import com.example.task.entity.Task;
import com.example.task.entity.TaskPriority;
import com.example.task.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- findByStatus ---

    @Test
    void findByStatus_existingRecord_returnsResult() {
        Task task = new Task();
        task.setTitle("Pending Task");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIUM);
        entityManager.persistAndFlush(task);

        Page<Task> result = taskRepository.findByStatus(TaskStatus.PENDING, PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertEquals(TaskStatus.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    void findByStatus_noMatchingRecord_returnsEmpty() {
        Page<Task> result = taskRepository.findByStatus(TaskStatus.COMPLETED, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    // --- findByPriority ---

    @Test
    void findByPriority_existingRecord_returnsResult() {
        Task task = new Task();
        task.setTitle("High Priority Task");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.HIGH);
        entityManager.persistAndFlush(task);

        Page<Task> result = taskRepository.findByPriority(TaskPriority.HIGH, PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertEquals(TaskPriority.HIGH, result.getContent().get(0).getPriority());
    }

    @Test
    void findByPriority_noMatchingRecord_returnsEmpty() {
        Page<Task> result = taskRepository.findByPriority(TaskPriority.LOW, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }
}
