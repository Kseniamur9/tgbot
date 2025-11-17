package ru.skillfactorydemo.tgbot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skillfactorydemo.tgbot.entity.Income;
import ru.skillfactorydemo.tgbot.entity.Spend;
import ru.skillfactorydemo.tgbot.repository.IncomeRepository;
import ru.skillfactorydemo.tgbot.repository.SpendRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinanceService — юнит тесты")
class FinanceServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private SpendRepository spendRepository;

    @InjectMocks
    private FinanceService financeService;

    private static final Long CHAT_ID = 100500L;

    @Test
    @DisplayName("Добавление дохода: корректная сумма — сохраняется и возвращает успех")
    void addIncome_validAmount_savesAndReturnsSuccess() {
        when(incomeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, "5000.50", CHAT_ID);

        assertThat(result).contains("5000.50").contains("₽");

        ArgumentCaptor<Income> captor = ArgumentCaptor.forClass(Income.class);
        verify(incomeRepository).save(captor.capture());
        assertThat(captor.getValue().getChatId()).isEqualTo(CHAT_ID);
        assertThat(captor.getValue().getIncome()).isEqualByComparingTo("5000.50");
    }

    @Test
    @DisplayName("Добавление дохода: сумма с запятой — принимается корректно")
    void addIncome_amountWithComma_accepted() {
        when(incomeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, "1500,75", CHAT_ID);

        assertThat(result).contains("1500.75");
        verify(incomeRepository).save(any());
    }

    @Test
    @DisplayName("Добавление расхода: корректная сумма — сохраняется")
    void addSpend_validAmount_savesAndReturnsSuccess() {
        when(spendRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = financeService.addFinanceOperation(FinanceService.ADD_SPEND, "1200", CHAT_ID);

        assertThat(result).contains("1200").contains("₽");

        ArgumentCaptor<Spend> captor = ArgumentCaptor.forClass(Spend.class);
        verify(spendRepository).save(captor.capture());
        assertThat(captor.getValue().getSpend()).isEqualByComparingTo("1200");
    }

    @Test
    @DisplayName("Ошибка: текст вместо числа")
    void addIncome_nonNumericAmount_returnsError() {
        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, "abc", CHAT_ID);

        assertThat(result).containsIgnoringCase("формат");
        verifyNoInteractions(incomeRepository, spendRepository);
    }

    @Test
    @DisplayName("Ошибка: отрицательная сумма")
    void addIncome_negativeAmount_returnsError() {
        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, "-500", CHAT_ID);

        assertThat(result).containsIgnoringCase("положительн");
        verifyNoInteractions(incomeRepository, spendRepository);
    }

    @Test
    @DisplayName("Ошибка: сумма = 0")
    void addIncome_zeroAmount_returnsError() {
        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, "0", CHAT_ID);

        assertThat(result).containsIgnoringCase("положительн");
        verifyNoInteractions(incomeRepository, spendRepository);
    }

    @Test
    @DisplayName("Ошибка: пустая строка")
    void addIncome_emptyAmount_returnsError() {
        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, "   ", CHAT_ID);

        assertThat(result).containsIgnoringCase("сумм");
        verifyNoInteractions(incomeRepository, spendRepository);
    }

    @Test
    @DisplayName("Ошибка: null сумма")
    void addIncome_nullAmount_returnsError() {
        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, null, CHAT_ID);

        assertThat(result).containsIgnoringCase("сумм");
        verifyNoInteractions(incomeRepository, spendRepository);
    }

    @Test
    @DisplayName("Ошибка: null chatId")
    void addIncome_nullChatId_returnsError() {
        String result = financeService.addFinanceOperation(FinanceService.ADD_INCOME, "1000", null);

        assertThat(result).containsIgnoringCase("пользовател");
        verifyNoInteractions(incomeRepository, spendRepository);
    }

    @Test
    @DisplayName("Ошибка: неизвестный тип операции")
    void unknownOperationType_returnsError() {
        String result = financeService.addFinanceOperation("/unknowncmd", "1000", CHAT_ID);

        assertThat(result).containsIgnoringCase("неизвестн");
        verifyNoInteractions(incomeRepository, spendRepository);
    }
}