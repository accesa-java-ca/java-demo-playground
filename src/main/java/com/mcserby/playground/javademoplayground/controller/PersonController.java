package com.mcserby.playground.javademoplayground.controller;

import com.mcserby.playground.javademoplayground.dto.Person;
import com.mcserby.playground.javademoplayground.mapper.DtoToEntityMapper;
import com.mcserby.playground.javademoplayground.monitoring.TrackExecutionTime;
import com.mcserby.playground.javademoplayground.persistence.repository.PersonRepository;
import com.mcserby.playground.javademoplayground.persistence.repository.WalletRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class PersonController {

    private final PersonRepository repository;
    private final WalletRepository walletRepository;

    public PersonController(
            PersonRepository repository,
            WalletRepository walletRepository) {
        this.repository = repository;
        this.walletRepository = walletRepository;
    }

    @GetMapping("/persons")
    @TrackExecutionTime
    List<Person> all() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(DtoToEntityMapper::map)
                .collect(Collectors.toList());
    }

    @PostMapping("/person")
    @TrackExecutionTime
    @CachePut("person")
    Person newAgency(@RequestBody Person person) {
        return DtoToEntityMapper.map(repository.save(DtoToEntityMapper.map(person)));
    }

    @GetMapping("/person/{id}")
    @TrackExecutionTime
    @Cacheable("person")
    Person getOne(@PathVariable Long id) {
        return repository.findById(id).map(DtoToEntityMapper::map)
                .orElseThrow(() -> new RuntimeException("not found"));
    }

    @PutMapping("/person")
    @TrackExecutionTime
    @CachePut("person")
    Person replace(@RequestBody Person person) {
        return DtoToEntityMapper.map(repository.save(DtoToEntityMapper.map(person)));
    }

    @DeleteMapping("/person/{id}")
    @TrackExecutionTime
    void delete(@PathVariable Long id) {
        repository.findById(id).ifPresent(
                a -> a.getWallets().forEach(ep -> walletRepository.deleteById(ep.getId())));
        repository.deleteById(id);
    }

    @DeleteMapping("/persons")
    @TrackExecutionTime
    void deleteAll() {
        walletRepository.deleteAll();
        repository.deleteAll();
    }
}
