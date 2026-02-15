package com.expensora.expensora_api.service.impl;

import com.expensora.expensora_api.dto.CalendarEventDto;
import com.expensora.expensora_api.entity.Expense;
import com.expensora.expensora_api.entity.Income;
import com.expensora.expensora_api.repository.ExpenseRepository;
import com.expensora.expensora_api.repository.IncomeRepository;
import com.expensora.expensora_api.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CalendarServiceImpl implements CalendarService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Override
    public List<CalendarEventDto> getCalendarEvents(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<CalendarEventDto> events = new ArrayList<>();

        // Get expenses
        Specification<Expense> expenseSpec = Specification.where((root, query, cb) -> cb.conjunction());
        expenseSpec = expenseSpec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        expenseSpec = expenseSpec.and((root, query, cb) -> cb.between(root.get("expenseDate"), startDate, endDate));

        List<Expense> expenses = expenseRepository.findAll(expenseSpec);
        for (Expense expense : expenses) {
            CalendarEventDto event = new CalendarEventDto();
            event.setId(expense.getId());
            event.setTitle(expense.getDescription());
            event.setType("EXPENSE");
            event.setAmount(expense.getAmount());
            event.setDate(expense.getExpenseDate());
            event.setCategoryName(expense.getCategory() != null ? expense.getCategory().getName() : null);
            event.setCategoryColor(expense.getCategory() != null ? expense.getCategory().getColor() : null);
            event.setDescription(expense.getNotes());
            events.add(event);
        }

        // Get incomes
        Specification<Income> incomeSpec = Specification.where((root, query, cb) -> cb.conjunction());
        incomeSpec = incomeSpec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        incomeSpec = incomeSpec.and((root, query, cb) -> cb.between(root.get("incomeDate"), startDate, endDate));

        List<Income> incomes = incomeRepository.findAll(incomeSpec);
        for (Income income : incomes) {
            CalendarEventDto event = new CalendarEventDto();
            event.setId(income.getId());
            event.setTitle(income.getDescription());
            event.setType("INCOME");
            event.setAmount(income.getAmount());
            event.setDate(income.getIncomeDate());
            event.setCategoryName(income.getCategory() != null ? income.getCategory().getName() : null);
            event.setCategoryColor(income.getCategory() != null ? income.getCategory().getColor() : null);
            event.setDescription(income.getNotes());
            events.add(event);
        }

        return events;
    }
}
