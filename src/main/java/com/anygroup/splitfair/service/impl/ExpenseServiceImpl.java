package com.anygroup.splitfair.service.impl;

import com.anygroup.splitfair.dto.ExpenseDTO;
import com.anygroup.splitfair.enums.DebtStatus;
import com.anygroup.splitfair.enums.ExpenseStatus;
import com.anygroup.splitfair.model.*;
import com.anygroup.splitfair.repository.*;
import com.anygroup.splitfair.service.ExpenseService;
import com.anygroup.splitfair.mapper.ExpenseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final DebtRepository debtRepository;
    private final ExpenseMapper expenseMapper;


      //Tạo mới Expense (chưa chia shares)
    @Override
    public ExpenseDTO createExpense(ExpenseDTO dto) {
        //  Map DTO → Entity
        Expense expense = expenseMapper.toEntity(dto);

        // Liên kết Bill
        if (dto.getBillId() != null) {
            Bill bill = billRepository.findById(dto.getBillId())
                    .orElseThrow(() -> new RuntimeException("Bill not found with id: " + dto.getBillId()));
            expense.setBill(bill);
        }

        // Gắn người tạo
        if (dto.getCreatedBy() != null) {
            User creator = userRepository.findById(dto.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getCreatedBy()));
            expense.setCreatedBy(creator);
        }

        // Gắn người trả tiền
        if (dto.getPaidBy() != null) {
            User payer = userRepository.findById(dto.getPaidBy())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getPaidBy()));
            expense.setPaidBy(payer);
        }

        //  Gắn user_id (nếu chưa có thì mặc định bằng createdBy)
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
            expense.setUser(user);
        } else if (expense.getCreatedBy() != null) {
            expense.setUser(expense.getCreatedBy());
        }

        //  Gán trạng thái mặc định
        if (expense.getStatus() == null) {
            expense.setStatus(ExpenseStatus.PENDING);
        }

        //  Lưu vào DB
        expense = expenseRepository.save(expense);

        return expenseMapper.toDTO(expense);
    }


     //Lấy tất cả Expense

    @Override
    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.findAll()
                .stream()
                .map(expenseMapper::toDTO)
                .collect(Collectors.toList());
    }


     //Lấy 1 Expense theo ID

    @Override
    public ExpenseDTO getExpenseById(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        return expenseMapper.toDTO(expense);
    }


      //Lấy Expense theo Bill
    @Override
    public List<ExpenseDTO> getExpensesByBill(UUID billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + billId));
        return expenseRepository.findByBill(bill)
                .stream()
                .map(expenseMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<ExpenseDTO> getExpensesCreatedByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return expenseRepository.findByCreatedBy(user)
                .stream()
                .map(expenseMapper::toDTO)
                .collect(Collectors.toList());
    }



    //Lấy Expense theo người thanh toán
    @Override
    public List<ExpenseDTO> getExpensesPaidByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return expenseRepository.findByPaidBy(user)
                .stream()
                .map(expenseMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Override
    public ExpenseDTO updateExpense(UUID id, ExpenseDTO dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());

        if (dto.getPaidBy() != null) {
            User payer = userRepository.findById(dto.getPaidBy())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getPaidBy()));
            expense.setPaidBy(payer);
        }

        //  Cập nhật trạng thái nếu có
        if (dto.getStatus() != null) {
            expense.setStatus(dto.getStatus());
        }

        expense = expenseRepository.save(expense);
        return expenseMapper.toDTO(expense);
    }


    @Override
    public void deleteExpense(UUID id) {
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }
}
