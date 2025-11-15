package ru.skillfactorydemo.tgbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.skillfactorydemo.tgbot.bot.CurrencyBot;
import ru.skillfactorydemo.tgbot.dto.ValuteCursOnDate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrencyScheduler {

    private final CentralRussianBankService cbrService;
    private final CurrencyBot bot;


    private final long NOTIFY_CHAT_ID = 123456789L;

    @Scheduled(fixedRate = 3600000) // каждые 60 минут
    public void checkUsdRate() {
        try {
            List<ValuteCursOnDate> rates = cbrService.getCurrenciesFromCbr();
            ValuteCursOnDate usd = rates.stream()
                    .filter(r -> "USD".equals(r.getChCode()))
                    .findFirst()
                    .orElse(null);

            if (usd != null && usd.getCourse() > 95.0) {
                bot.sendMessage(NOTIFY_CHAT_ID,
                        "ВНИМАНИЕ! USD вырос!\n" +
                                "Текущий курс: " + usd.getCourse() + " ₽");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
