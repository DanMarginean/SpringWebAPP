package com.example.onlineshop.Service;

import com.example.onlineshop.dto.CustomerRequestDto;
import com.example.onlineshop.dto.CustomerResponseDto;
import com.example.onlineshop.Entity.Customer;
import com.example.onlineshop.Util.CustomerMapper;
import com.example.onlineshop.Repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerService(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public CustomerResponseDto create(CustomerRequestDto dto) {
        Customer saved = repository.save(mapper.toEntity(dto));
        return mapper.toResponse(saved);
    }

    public List<CustomerResponseDto> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerResponseDto getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public CustomerResponseDto update(Long id, CustomerRequestDto dto) {
        Customer entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        mapper.updateEntity(entity, dto);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
