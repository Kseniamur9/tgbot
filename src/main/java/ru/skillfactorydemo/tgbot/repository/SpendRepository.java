package ru.skillfactorydemo.tgbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillfactorydemo.tgbot.entity.Spend;
import java.util.List;

public interface SpendRepository extends JpaRepository<Spend, Long> {
    List<Spend> findByChatId(Long chatId);
}