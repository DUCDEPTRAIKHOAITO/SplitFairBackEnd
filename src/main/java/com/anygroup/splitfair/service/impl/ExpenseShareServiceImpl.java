package com.anygroup.splitfair.service.impl;

import com.anygroup.splitfair.dto.ExpenseShareDTO;
import com.anygroup.splitfair.dto.ExpenseShareSaveRequest;
import com.anygroup.splitfair.enums.DebtStatus;
import com.anygroup.splitfair.enums.ShareStatus;
import com.anygroup.splitfair.mapper.ExpenseShareMapper;
import com.anygroup.splitfair.model.*;
import com.anygroup.splitfair.repository.*;
import com.anygroup.splitfair.service.ExpenseShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseShareServiceImpl implements ExpenseShareService {

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final DebtRepository debtRepository;
    private final ExpenseShareMapper expenseShareMapper;

    // Tạo phần chia chi phí riêng lẻ
    @Override
    public ExpenseShareDTO createShare(ExpenseShareDTO dto) {
        Expense expense = expenseRepository.findById(dto.getExpenseId())
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + dto.getExpenseId()));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        ExpenseShare share = new ExpenseShare();
        share.setExpense(expense);
        share.setUser(user);
        share.setPercentage(dto.getPercentage());
        share.setStatus(dto.getStatus() == null ? ShareStatus.UNPAID : dto.getStatus());

        share = expenseShareRepository.save(share);

        // 👉 Không tạo Debt nếu chính người trả tiền (đã trả rồi, không nợ ai)
        User paidBy = expense.getPaidBy();
        if (paidBy != null && paidBy.getId().equals(user.getId())) {
            return expenseShareMapper.toDTO(share);
        }

        BigDecimal shareAmount = expense.getAmount()
                .multiply(share.getPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Debt debt = new Debt();
        debt.setExpense(expense);
        debt.setAmountFrom(user);
        debt.setAmountTo(paidBy);
        debt.setAmount(shareAmount);
        debt.setStatus(DebtStatus.UNSETTLED);
        debtRepository.save(debt);

        return expenseShareMapper.toDTO(share);
    }

    @Override
    public ExpenseShareDTO updateShareStatus(UUID id, String status) {
        ExpenseShare share = expenseShareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ExpenseShare not found with id: " + id));

        ShareStatus newStatus = ShareStatus.valueOf(status.toUpperCase());
        share.setStatus(newStatus);
        share = expenseShareRepository.save(share);

        List<Debt> debts = debtRepository.findByExpenseAndAmountFrom(
                share.getExpense(),
                share.getUser()
        );

        for (Debt debt : debts) {
            if (newStatus == ShareStatus.PAID) {
                debt.setStatus(DebtStatus.SETTLED);
            } else {
                debt.setStatus(DebtStatus.UNSETTLED);
            }
            debtRepository.save(debt);
        }

        return expenseShareMapper.toDTO(share);
    }

    @Override
    public List<ExpenseShareDTO> getSharesByExpense(UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));

        return expenseShareRepository.findByExpense(expense)
                .stream()
                .map(expenseShareMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseShareDTO> getSharesByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return expenseShareRepository.findByUser(user)
                .stream()
                .map(expenseShareMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteShare(UUID id) {
        ExpenseShare share = expenseShareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ExpenseShare not found with id: " + id));

        List<Debt> debts = debtRepository.findByExpenseAndAmountFrom(
                share.getExpense(),
                share.getUser()
        );
        debts.forEach(debtRepository::delete);

        expenseShareRepository.delete(share);
    }

    // Lưu danh sách chia từ frontend (frontend đã chia sẵn)
    @Override
    @Transactional
    public void saveExpenseShares(ExpenseShareSaveRequest request) {
        BigDecimal totalShare = request.getShares().stream()
                .map(ExpenseShareSaveRequest.ShareInput::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShare.compareTo(request.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Tổng chia không khớp tổng chi phí");
        }

        Expense expense = expenseRepository.findById(request.getExpenseId())
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + request.getExpenseId()));

        User paidBy = expense.getPaidBy();

        // 👉 XÓA TẤT CẢ share & debt CŨ của expense trước khi lưu lại
        List<ExpenseShare> oldShares = expenseShareRepository.findByExpense(expense);
        expenseShareRepository.deleteAll(oldShares);

        List<Debt> oldDebts = debtRepository.findByExpense(expense);
        debtRepository.deleteAll(oldDebts);

        // 👉 Tạo lại share + debt mới theo data từ frontend
        for (ExpenseShareSaveRequest.ShareInput input : request.getShares()) {
            User user = userRepository.findById(input.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + input.getUserId()));

            ExpenseShare share = new ExpenseShare();
            share.setExpense(expense);
            share.setUser(user);
            share.setPercentage(input.getPercentage());
            share.setStatus(ShareStatus.UNPAID);
            expenseShareRepository.save(share);

            // Không tạo Debt cho chính người trả tiền
            if (paidBy != null && paidBy.getId().equals(user.getId())) {
                continue;
            }

            Debt debt = new Debt();
            debt.setExpense(expense);
            debt.setAmountFrom(user);
            debt.setAmountTo(paidBy);
            debt.setAmount(input.getShareAmount());
            debt.setStatus(DebtStatus.UNSETTLED);
            debtRepository.save(debt);
        }
    }
}
