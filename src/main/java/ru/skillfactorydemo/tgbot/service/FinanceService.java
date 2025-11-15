package ru.skillfactorydemo.tgbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillfactorydemo.tgbot.entity.Income;
import ru.skillfactorydemo.tgbot.entity.Spend;
import ru.skillfactorydemo.tgbot.repository.IncomeRepository;
import ru.skillfactorydemo.tgbot.repository.SpendRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceService {

    private static final String ADD_INCOME = "/addincome";
    private static final String ADD_SPEND = "/addspend";

    private final IncomeRepository incomeRepository;
    private final SpendRepository spendRepository;

    @Transactional
    public String addFinanceOperation(String operationType, String priceStr, Long chatId) {
        // 1. Проверка входных данных
        if (chatId == null) {
            return "Ошибка: не удалось определить пользователя";
        }
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return "Ошибка: укажите сумму. Пример: /addincome 5000";
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(priceStr.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "Ошибка: сумма должна быть положительной";
            }
        } catch (NumberFormatException e) {
            return "Ошибка: неверный формат суммы. Используйте цифры, например: 1500.50";
        }


        try {
            if (ADD_INCOME.equalsIgnoreCase(operationType)) {
                Income income = new Income();
                income.setChatId(chatId);
                income.setIncome(amount);
                incomeRepository.save(income);
                log.info("Добавлен доход: {} руб. для chatId={}", amount, chatId);
                return "Доход в размере " + amount + " ₽ успешно добавлен!";
            }

            // расход
            Spend spend = new Spend();
            spend.setChatId(chatId);
            spend.setSpend(amount);
            spendRepository.save(spend);
            log.info("Добавлен расход: {} руб. для chatId={}", amount, chatId);
            return "Расход в размере " + amount + " ₽ успешно добавлен!";

        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка сохранения в БД", e);
            return "Ошибка: не удалось сохранить операцию. Попробуйте позже.";
        } catch (Exception e) {
            log.error("Неизвестная ошибка при добавлении операции", e);
            return "Произошла ошибка. Попробуйте ещё раз.";
        }
    }


    public String addIncome(String price, Long chatId) {
        return addFinanceOperation(ADD_INCOME, price, chatId);
    }

    public String addSpend(String price, Long chatId) {
        return addFinanceOperation(ADD_SPEND, price, chatId);
    }
}