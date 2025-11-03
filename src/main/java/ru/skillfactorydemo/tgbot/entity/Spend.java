package ru.skillfactorydemo.tgbot.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spends")
@Data
public class Spend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String currency;
    private LocalDateTime date = LocalDateTime.now();
    private String description;

    @Column(name = "chat_id")
    private Long chatId;
}