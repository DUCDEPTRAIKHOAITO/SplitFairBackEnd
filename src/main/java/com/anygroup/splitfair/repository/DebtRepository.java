package com.anygroup.splitfair.repository;

import com.anygroup.splitfair.model.Debt;
import com.anygroup.splitfair.model.Expense;
import com.anygroup.splitfair.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, UUID> {

    // Tìm các khoản nợ mà user này nợ người khác
    List<Debt> findByAmountFrom(User fromUser);

    // Tìm các khoản nợ mà người khác nợ user này
    List<Debt> findByAmountTo(User toUser);

    Optional<Debt> findByExpenseAndAmountFromAndAmountTo(Expense expense, User from, User to);

    Optional<Debt> findByAmountFromAndAmountTo(User from, User to);

    List<Debt> findByExpenseAndAmountFrom(Expense expense, User amountFrom);

    // 👉 Thêm: lấy tất cả Debt của 1 Expense (dùng khi lưu lại chia tiền)
    List<Debt> findByExpense(Expense expense);
}
