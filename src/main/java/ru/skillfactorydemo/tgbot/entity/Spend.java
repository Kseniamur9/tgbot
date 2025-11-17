package ru.skillfactorydemo.tgbot.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "SPEND")
public class Spend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CHAT_ID")
    private Long chatId;

    @Column(name = "SPEND")
    private BigDecimal spend;

    public void setCurrency(String currency) {

    }
}