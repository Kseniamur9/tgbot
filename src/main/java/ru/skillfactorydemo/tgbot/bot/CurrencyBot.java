package ru.skillfactorydemo.tgbot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.skillfactorydemo.tgbot.dto.ValuteCursOnDate;
import ru.skillfactorydemo.tgbot.entity.Spend;
import ru.skillfactorydemo.tgbot.repository.SpendRepository;
import ru.skillfactorydemo.tgbot.service.CentralRussianBankService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrencyBot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    private final CentralRussianBankService cbrService;
    private final SpendRepository spendRepository;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();

        try {
            switch (text) {
                case "/start":
                    sendMsg(chatId, "Привет! Я бот курсов валют.\n" +
                            "Команды:\n" +
                            "/rates — курсы USD, EUR, CNY\n" +
                            "/add 150 USD — добавить расход\n" +
                            "/report — отчёт");
                    break;

                case "/rates":
                    List<ValuteCursOnDate> rates = cbrService.getCurrenciesFromCbr();
                    StringBuilder sb = new StringBuilder("Курсы ЦБ РФ на сегодня:\n\n");
                    rates.stream()
                            .filter(r -> List.of("USD", "EUR", "CNY").contains(r.getChCode()))
                            .forEach(r -> sb.append(String.format("%s: %.2f ₽\n", r.getChCode(), r.getCourse())));
                    sendMsg(chatId, sb.toString());
                    break;

                default:
                    if (text.startsWith("/add ")) {
                        handleAddExpense(text, chatId);
                    } else if (text.equals("/report")) {
                        handleReport(chatId);
                    } else {
                        sendMsg(chatId, "Неизвестная команда. Используй /start");
                    }
            }
        } catch (Exception e) {
            sendMsg(chatId, "Ошибка: " + e.getMessage());
        }
    }

    private void handleAddExpense(String text, Long chatId) {
        String[] parts = text.substring(5).trim().split(" ", 2);
        if (parts.length < 2) {
            sendMsg(chatId, "Формат: /add 150 USD");
            return;
        }

        try {
            Double amount = Double.parseDouble(parts[0]);
            String currency = parts[1].toUpperCase();

            Spend spend = new Spend();
            spend.setAmount(amount);
            spend.setCurrency(currency);
            spend.setDescription("Расход");
            spend.setChatId(chatId);
            spendRepository.save(spend);

            sendMsg(chatId, "Добавлено: " + amount + " " + currency);
        } catch (NumberFormatException e) {
            sendMsg(chatId, "Сумма должна быть числом!");
        }
    }

    private void handleReport(Long chatId) {
        List<Spend> spends = spendRepository.findByChatId(chatId);
        if (spends.isEmpty()) {
            sendMsg(chatId, "Нет расходов");
            return;
        }

        StringBuilder sb = new StringBuilder("Ваши расходы:\n");
        spends.forEach(s -> sb.append(String.format("%.2f %s — %s\n",
                s.getAmount(), s.getCurrency(), s.getDate().toLocalDate())));
        sendMsg(chatId, sb.toString());
    }

    public void sendMsg(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
