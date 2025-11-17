package ru.skillfactorydemo.tgbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.skillfactorydemo.tgbot.bot.CurrencyBot;
import ru.skillfactorydemo.tgbot.dto.ValuteCursOnDate;
import ru.skillfactorydemo.tgbot.entity.ActiveChat;
import ru.skillfactorydemo.tgbot.repository.ActiveChatRepository;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private static final double CHANGE_THRESHOLD = 10.0;

    private final ActiveChatRepository activeChatRepository;
    private final CurrencyBot currencyBot;
    private final CentralRussianBankService centralRussianBankService;

    private final Map<String, Double> previousRates = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 0/3 * * *")
    public void notifyAboutChangesInCurrencyRate() {
        try {
            List<ValuteCursOnDate> currentRates = centralRussianBankService.getCurrenciesFromCbr();
            List<Long> activeChatIds = activeChatRepository.findAll().stream()
                    .map(ActiveChat::getChatId)
                    .collect(Collectors.toList());

            if (previousRates.isEmpty()) {
                currentRates.forEach(r -> previousRates.put(r.getChCode(), r.getCourse()));
                log.info("Начальные курсы сохранены: {} валют", previousRates.size());
                return;
            }

            for (ValuteCursOnDate current : currentRates) {
                Double prev = previousRates.get(current.getChCode());
                if (prev == null) continue;

                double diff = current.getCourse() - prev;

                if (diff >= CHANGE_THRESHOLD) {
                    String msg = String.format("📈 Курс %s вырос на %.2f ₽\nТекущий курс: %.2f ₽",
                            current.getName(), diff, current.getCourse());
                    activeChatIds.forEach(chatId -> currencyBot.sendMessage(chatId, msg));
                } else if (diff <= -CHANGE_THRESHOLD) {
                    String msg = String.format("📉 Курс %s упал на %.2f ₽\nТекущий курс: %.2f ₽",
                            current.getName(), Math.abs(diff), current.getCourse());
                    activeChatIds.forEach(chatId -> currencyBot.sendMessage(chatId, msg));
                }

                previousRates.put(current.getChCode(), current.getCourse());
            }

        } catch (DatatypeConfigurationException e) {
            log.error("Ошибка получения курсов в планировщике", e);
        }
    }

    public Map<String, Double> getPreviousRates() {
        return previousRates;
    }
}