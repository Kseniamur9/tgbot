package ru.skillfactorydemo.tgbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.skillfactorydemo.tgbot.bot.CurrencyBot;
import ru.skillfactorydemo.tgbot.dto.ValuteCursOnDate;
import ru.skillfactorydemo.tgbot.entity.ActiveChat;
import ru.skillfactorydemo.tgbot.repository.ActiveChatRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyScheduler {

    private final CentralRussianBankService cbrService;
    private final CurrencyBot bot;
    private final ActiveChatRepository activeChatRepository;

    @Value("${currency.usd.alert.threshold:95.0}")
    private double usdAlertThreshold;

    @Scheduled(fixedRate = 3600000)
    public void checkUsdRate() {
        try {
            List<ValuteCursOnDate> rates = cbrService.getCurrenciesFromCbr();
            rates.stream()
                    .filter(r -> "USD".equals(r.getChCode()))
                    .findFirst()
                    .ifPresent(usd -> {
                        if (usd.getCourse() > usdAlertThreshold) {
                            String msg = String.format(
                                    "⚠️ Курс USD превысил %.1f ₽\nТекущий курс: %.2f ₽",
                                    usdAlertThreshold, usd.getCourse());
                            List<Long> chatIds = activeChatRepository.findAll().stream()
                                    .map(ActiveChat::getChatId)
                                    .collect(Collectors.toList());
                            chatIds.forEach(chatId -> bot.sendMessage(chatId, msg));
                            log.info("Отправлено предупреждение USD ({}) в {} чатов",
                                    usd.getCourse(), chatIds.size());
                        }
                    });
        } catch (Exception e) {
            log.error("Ошибка при проверке курса USD", e);
        }
    }
}
