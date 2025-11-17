package ru.skillfactorydemo.tgbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skillfactorydemo.tgbot.bot.CurrencyBot;
import ru.skillfactorydemo.tgbot.dto.ValuteCursOnDate;
import ru.skillfactorydemo.tgbot.entity.ActiveChat;
import ru.skillfactorydemo.tgbot.repository.ActiveChatRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService — юнит тесты")
class ScheduleServiceTest {

    @Mock private ActiveChatRepository activeChatRepository;
    @Mock private CurrencyBot currencyBot;
    @Mock private CentralRussianBankService centralRussianBankService;

    @InjectMocks
    private ScheduleService scheduleService;

    private ValuteCursOnDate makeRate(String code, String name, double course) {
        ValuteCursOnDate r = new ValuteCursOnDate();
        r.setChCode(code);
        r.setName(name);
        r.setCourse(course);
        return r;
    }

    private ActiveChat makeChat(Long id) {
        ActiveChat ac = new ActiveChat();
        ac.setChatId(id);
        return ac;
    }

    @BeforeEach
    void setUp() {
        scheduleService.getPreviousRates().clear();
    }

    @Test
    @DisplayName("Первый запуск: курсы сохраняются, уведомления не отправляются")
    void firstRun_savesRates_noNotifications() throws Exception {
        when(centralRussianBankService.getCurrenciesFromCbr())
                .thenReturn(List.of(makeRate("USD", "Доллар", 90.0)));

        scheduleService.notifyAboutChangesInCurrencyRate();

        verifyNoInteractions(currencyBot);
        assertThat(scheduleService.getPreviousRates().get("USD")).isEqualTo(90.0);
    }

    @Test
    @DisplayName("Курс вырос на 10+ ₽: уведомление отправляется")
    void rateIncreasedByThreshold_sendsNotification() throws Exception {
        scheduleService.getPreviousRates().put("USD", 90.0);
        when(centralRussianBankService.getCurrenciesFromCbr())
                .thenReturn(List.of(makeRate("USD", "Доллар", 101.0)));
        when(activeChatRepository.findAll())
                .thenReturn(List.of(makeChat(111L), makeChat(222L)));

        scheduleService.notifyAboutChangesInCurrencyRate();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(currencyBot, times(2)).sendMessage(anyLong(), captor.capture());
        assertThat(captor.getValue()).contains("вырос");
    }

    @Test
    @DisplayName("Курс упал на 10+ ₽: уведомление отправляется")
    void rateDecreasedByThreshold_sendsNotification() throws Exception {
        scheduleService.getPreviousRates().put("USD", 100.0);
        when(centralRussianBankService.getCurrenciesFromCbr())
                .thenReturn(List.of(makeRate("USD", "Доллар", 89.0)));
        when(activeChatRepository.findAll())
                .thenReturn(List.of(makeChat(111L)));

        scheduleService.notifyAboutChangesInCurrencyRate();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(currencyBot).sendMessage(anyLong(), captor.capture());
        assertThat(captor.getValue()).contains("упал");
    }

    @Test
    @DisplayName("Изменение меньше 10 ₽: уведомления нет")
    void rateChangeUnderThreshold_noNotification() throws Exception {
        scheduleService.getPreviousRates().put("USD", 90.0);
        when(centralRussianBankService.getCurrenciesFromCbr())
                .thenReturn(List.of(makeRate("USD", "Доллар", 95.0)));
        when(activeChatRepository.findAll())
                .thenReturn(List.of(makeChat(111L)));

        scheduleService.notifyAboutChangesInCurrencyRate();

        verifyNoInteractions(currencyBot);
    }

    @Test
    @DisplayName("После уведомления previousRates обновляется")
    void afterNotification_previousRatesUpdated() throws Exception {
        scheduleService.getPreviousRates().put("USD", 90.0);
        when(centralRussianBankService.getCurrenciesFromCbr())
                .thenReturn(List.of(makeRate("USD", "Доллар", 105.0)));
        when(activeChatRepository.findAll())
                .thenReturn(List.of(makeChat(111L)));

        scheduleService.notifyAboutChangesInCurrencyRate();

        assertThat(scheduleService.getPreviousRates().get("USD")).isEqualTo(105.0);
    }

    @Test
    @DisplayName("Нет активных чатов: уведомления не отправляются")
    void noActiveChats_noNotifications() throws Exception {
        scheduleService.getPreviousRates().put("USD", 90.0);
        when(centralRussianBankService.getCurrenciesFromCbr())
                .thenReturn(List.of(makeRate("USD", "Доллар", 105.0)));
        when(activeChatRepository.findAll()).thenReturn(List.of());

        scheduleService.notifyAboutChangesInCurrencyRate();

        verifyNoInteractions(currencyBot);
    }
}