package com.anygroup.splitfair.service.impl;

import com.anygroup.splitfair.dto.DebtDTO;
import com.anygroup.splitfair.enums.DebtStatus;
import com.anygroup.splitfair.mapper.DebtMapper;
import com.anygroup.splitfair.model.*;
import com.anygroup.splitfair.repository.*;
import com.anygroup.splitfair.service.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;
    private final DebtMapper debtMapper;
    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Override
    public List<DebtDTO> getAllDebts() {
        return debtRepository.findAll()
                .stream()
                .map(debtMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DebtDTO getDebtById(UUID id) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Debt not found with id: " + id));
        return debtMapper.toDTO(debt);
    }

    @Override
    public DebtDTO createDebt(DebtDTO dto) {
        Debt debt = debtMapper.toEntity(dto);
        if (debt.getStatus() == null) {
            debt.setStatus(DebtStatus.UNSETTLED);
        }
        debt = debtRepository.save(debt);
        return debtMapper.toDTO(debt);
    }

    @Override
    public DebtDTO updateDebt(DebtDTO dto) {
        if (dto.getId() == null) throw new RuntimeException("Debt ID is required for update");

        Debt existing = debtRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Debt not found with id: " + dto.getId()));

        Debt updated = debtMapper.toEntity(dto);
        updated.setId(existing.getId());

        updated = debtRepository.save(updated);
        return debtMapper.toDTO(updated);
    }

    @Override
    public void deleteDebt(UUID id) {
        debtRepository.deleteById(id);
    }

    @Override
    public DebtDTO markAsSettled(UUID id) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Debt not found with id: " + id));
        debt.setStatus(DebtStatus.SETTLED);
        debtRepository.save(debt);
        return debtMapper.toDTO(debt);
    }

    // ⚠️ HÀM NÀY HIỆN CHƯA THẤY BẠN GỌI
    // Nếu đang dùng saveExpenseShares để tạo Debt thì không cần gọi thêm hàm này nữa.
    @Override
    public void calculateDebtsForExpense(Expense expense) {
        List<ExpenseShare> shares = expenseShareRepository.findByExpense(expense);
        User payer = expense.getPaidBy();

        for (ExpenseShare share : shares) {
            User debtor = share.getUser();
            if (debtor.getId().equals(payer.getId())) continue; // Người trả không nợ chính mình

            BigDecimal amount = expense.getAmount()
                    .multiply(share.getPercentage().divide(BigDecimal.valueOf(100)));

            Optional<Debt> existing = debtRepository.findByAmountFromAndAmountTo(debtor, payer);
            if (existing.isPresent()) {
                Debt debt = existing.get();
                debt.setAmount(debt.getAmount().add(amount));
                debtRepository.save(debt);
            } else {
                Debt newDebt = Debt.builder()
                        .expense(expense)
                        .amountFrom(debtor)
                        .amountTo(payer)
                        .amount(amount)
                        .status(DebtStatus.UNSETTLED)
                        .build();
                debtRepository.save(newDebt);
            }
        }
    }

    // Tổng kết số dư nợ của tất cả người dùng (chỉ tính UNSETTLED)
    @Override
    public Map<UUID, BigDecimal> getNetBalances() {
        List<Debt> debts = debtRepository.findAll();
        Map<UUID, BigDecimal> balance = new HashMap<>();

        for (Debt d : debts) {
            if (d.getStatus() == DebtStatus.SETTLED) continue; // bỏ nợ đã thanh toán

            UUID from = d.getAmountFrom().getId();
            UUID to = d.getAmountTo().getId();
            BigDecimal amount = d.getAmount();

            balance.put(from, balance.getOrDefault(from, BigDecimal.ZERO).subtract(amount));
            balance.put(to, balance.getOrDefault(to, BigDecimal.ZERO).add(amount));
        }

        return balance;
    }

    @Override
    public List<String> getReadableBalances() {
        Map<UUID, BigDecimal> balances = getNetBalances();

        List<String> readable = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            UUID userId = entry.getKey();
            BigDecimal amount = entry.getValue();

            String userName = userRepository.findById(userId)
                    .map(User::getUserName)
                    .orElse("Unknown User");

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                readable.add(userName + " owes " + amount.abs() + " VND");
            } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
                readable.add(userName + " should receive " + amount + " VND");
            } else {
                readable.add(userName + " is settled up");
            }
        }

        return readable;
    }

    // 👉 HÀM BẠN ĐANG DÙNG ĐỂ TRẢ VỀ JSON:
    // {
    //   userId, totalOwes, totalIsOwed, netBalance, details[]
    // }
    @Override
    public Map<String, Object> getUserDebtDetails(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // A nợ người khác
        List<Debt> owes = debtRepository.findByAmountFrom(user)
                .stream()
                .filter(d -> d.getStatus() != DebtStatus.SETTLED)
                .collect(Collectors.toList());

        // Người khác nợ A
        List<Debt> isOwed = debtRepository.findByAmountTo(user)
                .stream()
                .filter(d -> d.getStatus() != DebtStatus.SETTLED)
                .collect(Collectors.toList());

        Map<UUID, Summary> summaryMap = new HashMap<>();

        // --- A NỢ NGƯỜI KHÁC ---
        for (Debt d : owes) {
            UUID otherId = d.getAmountTo().getId();
            summaryMap.putIfAbsent(otherId, new Summary());
            summaryMap.get(otherId).owes = summaryMap.get(otherId).owes.add(d.getAmount());
        }

        // --- NGƯỜI KHÁC NỢ A ---
        for (Debt d : isOwed) {
            UUID otherId = d.getAmountFrom().getId();
            summaryMap.putIfAbsent(otherId, new Summary());
            summaryMap.get(otherId).isOwed = summaryMap.get(otherId).isOwed.add(d.getAmount());
        }

        BigDecimal totalOwes = BigDecimal.ZERO;
        BigDecimal totalIsOwed = BigDecimal.ZERO;

        List<Map<String, Object>> details = new ArrayList<>();

        for (UUID otherId : summaryMap.keySet()) {

            if (otherId.equals(userId)) continue;

            Summary s = summaryMap.get(otherId);
            User other = userRepository.findById(otherId).orElse(null);

            totalOwes = totalOwes.add(s.owes);
            totalIsOwed = totalIsOwed.add(s.isOwed);

            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", otherId);
            entry.put("userName", other != null ? other.getUserName() : "Unknown");
            entry.put("owes", s.owes);
            entry.put("isOwed", s.isOwed);

            details.add(entry);
        }

        BigDecimal net = totalIsOwed.subtract(totalOwes);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("totalOwes", totalOwes);
        response.put("totalIsOwed", totalIsOwed);
        response.put("netBalance", net);
        response.put("details", details);

        return response;
    }

    static class Summary {
        BigDecimal owes = BigDecimal.ZERO;
        BigDecimal isOwed = BigDecimal.ZERO;
    }

    @Override
    public List<DebtDTO> getDebtsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Debt> debts = debtRepository.findByAmountFrom(user);
        debts.addAll(debtRepository.findByAmountTo(user));

        return debts.stream()
                .filter(d -> d.getStatus() != DebtStatus.SETTLED)
                .map(debtMapper::toDTO)
                .collect(Collectors.toList());
    }
}
