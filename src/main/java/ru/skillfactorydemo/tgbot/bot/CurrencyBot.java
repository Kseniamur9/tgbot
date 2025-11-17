package ru.skillfactorydemo.tgbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.skillfactorydemo.tgbot.dto.ValuteCursOnDate;
import ru.skillfactorydemo.tgbot.entity.ActiveChat;
import ru.skillfactorydemo.tgbot.repository.ActiveChatRepository;
import ru.skillfactorydemo.tgbot.service.CentralRussianBankService;
import ru.skillfactorydemo.tgbot.service.FinanceService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class CurrencyBot extends TelegramLongPollingBot {

    private static final String CURRENT_RATES = "/currentrates";
    private static final String ADD_INCOME = "/addincome";
    private static final String ADD_SPEND = "/addspend";

    private final CentralRussianBankService cbrService;
    private final FinanceService financeService;
    private final ActiveChatRepository activeChatRepository;

    // Хранит последнюю команду пользователя
    private final Map<Long, String> lastCommand = new ConcurrentHashMap<>();

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.api.key}")
    private String botToken;

    @PostConstruct
    public void init() {
        log.info("Бот @{} запущен! Токен: {}", botName, botToken.substring(0, 10) + "...");
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();
        String responseText = "Неизвестная команда. Используй /help";

        try {
            if (text.equalsIgnoreCase("/start") || text.equalsIgnoreCase("/help")) {
                responseText = "Привет! Я — бот курсов ЦБ РФ + личные финансы.\n\n" +
                        "Команды:\n" +
                        "/currentrates — курсы USD, EUR, CNY\n" +
                        "/addincome 5000 — добавить доход\n" +
                        "/addspend 200 — добавить расход";

            } else if (text.equalsIgnoreCase(CURRENT_RATES)) {
                responseText = getRatesText();
                saveActiveChat(chatId);

            } else if (text.equalsIgnoreCase(ADD_INCOME)) {
                responseText = "Отправь сумму дохода (например: 7500.50)";
                lastCommand.put(chatId, ADD_INCOME);

            } else if (text.equalsIgnoreCase(ADD_SPEND)) {
                responseText = "Отправь сумму расхода (например: 1200)";
                lastCommand.put(chatId, ADD_SPEND);

            } else if (lastCommand.containsKey(chatId)) {
                responseText = financeService.addFinanceOperation(
                        lastCommand.get(chatId),
                        text,
                        chatId
                );
                lastCommand.remove(chatId);

            } else {
                responseText = "Сначала выбери команду: /addincome или /addspend";
            }

            sendMessage(chatId, responseText);

        } catch (Exception e) {
            log.error("Ошибка обработки сообщения от {}", chatId, e);
            sendMessage(chatId, "Произошла ошибка. Попробуй позже.");
        }
    }

    private String getRatesText() {
        try {
            List<ValuteCursOnDate> rates = cbrService.getCurrenciesFromCbr();
            StringBuilder sb = new StringBuilder("Курсы ЦБ РФ на сегодня:\n\n");
            rates.stream()
                    .filter(r -> List.of("USD", "EUR", "CNY").contains(r.getChCode()))
                    .forEach(r -> sb.append(String.format("%s — %.2f ₽\n", r.getChCode(), r.getCourse())));
            return sb.toString();
        } catch (Exception e) {
            log.error("Ошибка получения курсов", e);
            return "Не удалось загрузить курсы. Попробуй позже.";
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в чат {}", chatId, e);
        }
    }

    private void saveActiveChat(Long chatId) {
        if (activeChatRepository.findActiveChatByChatId(chatId).isEmpty()) {
            ActiveChat ac = new ActiveChat();
            ac.setChatId(chatId);
            activeChatRepository.save(ac);
            log.info("Новый активный чат: {}", chatId);
        }
    }

    // Для Scheduler
    public void sendToAll(String text) {
        activeChatRepository.findAll().forEach(ac -> sendMessage(ac.getChatId(), text));
    }
}