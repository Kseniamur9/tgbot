package ru.skillfactorydemo.tgbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.skillfactorydemo.tgbot.dto.ValuteCursOnDate;
import ru.skillfactorydemo.tgbot.entity.ActiveChat;
import ru.skillfactorydemo.tgbot.repository.ActiveChatRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotService extends TelegramLongPollingBot {

    private static final String CURRENT_RATES = "/currentrates";
    private static final String ADD_INCOME = "/addincome";
    private static final String ADD_SPEND = "/addspend";

    private final CentralRussianBankService centralRussianBankService;
    private final FinanceService financeService;
    private final ActiveChatRepository activeChatRepository;

    private Map<Long, List<String>> previousCommands = new ConcurrentHashMap<>();

    @Value("${bot.api.key}")
    private String apiKey;

    @Value("${bot.name}")
    private String name;

    @PostConstruct
    public void start() {
        log.info("Бот запущен: username = {}, token = {}", name, apiKey);
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return apiKey;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        try {
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(String.valueOf(chatId));

            if (CURRENT_RATES.equalsIgnoreCase(message.getText())) {
                for (ValuteCursOnDate valuteCursOnDate : centralRussianBankService.getCurrenciesFromCbr()) {
                    response.setText(StringUtils.defaultIfBlank(response.getText(), "") + valuteCursOnDate.getName() + " - " + valuteCursOnDate.getCourse() + "\n");
                }
            } else if (ADD_INCOME.equalsIgnoreCase(message.getText())) {
                response.setText("Отправьте мне сумму полученного дохода");
            } else if (ADD_SPEND.equalsIgnoreCase(message.getText())) {
                response.setText("Отправьте мне сумму расходов");
            } else {
                response.setText(financeService.addFinanceOperation(getPreviousCommand(chatId), message.getText(), chatId));
            }

            putPreviousCommand(chatId, message.getText());
            execute(response);

            // Добавляем в активные чаты
            if (activeChatRepository.findActiveChatByChatId(chatId).isEmpty()) {
                ActiveChat activeChat = new ActiveChat();
                activeChat.setChatId(chatId);
                activeChatRepository.save(activeChat);
            }

        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        } catch (Exception e) {
            log.error("Ошибка при получении данных от ЦБ РФ", e);
        }
    }

    public void sendNotificationToAllActiveChats(String message, Set<Long> chatIds) {
        for (Long id : chatIds) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(id));
            sendMessage.setText(message);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки уведомления", e);
            }
        }
    }

    private void putPreviousCommand(Long chatId, String command) {
        previousCommands.computeIfAbsent(chatId, k -> new ArrayList<>()).add(command);
    }

    private String getPreviousCommand(Long chatId) {
        List<String> commands = previousCommands.get(chatId);
        return commands != null && !commands.isEmpty() ? commands.get(commands.size() - 1) : "";
    }
}