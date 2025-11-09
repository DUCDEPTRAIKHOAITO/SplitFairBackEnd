package com.anygroup.splitfair.repository;

import com.anygroup.splitfair.model.Bill;
import com.anygroup.splitfair.model.Expense;
import com.anygroup.splitfair.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // Lấy các expense theo bill
    List<Expense> findByBill(Bill bill);

    // Lấy các expense mà user đã tạo
    List<Expense> findByCreatedBy(User user);

    // Lấy các expense mà user đã thanh toán
    List<Expense> findByPaidBy(User paidBy);
}