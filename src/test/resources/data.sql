-- Активные чаты
INSERT INTO active_chats (chat_id) VALUES (100500);
INSERT INTO active_chats (chat_id) VALUES (200300);
INSERT INTO active_chats (chat_id) VALUES (999111);

-- Доходы
INSERT INTO incomes (chat_id, income) VALUES (100500, 50000.00);
INSERT INTO incomes (chat_id, income) VALUES (100500, 15000.50);
INSERT INTO incomes (chat_id, income) VALUES (200300, 70000.00);

-- Расходы
INSERT INTO SPEND (chat_id, spend) VALUES (100500, 12000.00);
INSERT INTO SPEND (chat_id, spend) VALUES (100500, 8000.50);
INSERT INTO SPEND (chat_id, spend) VALUES (100500, 3500.00);
INSERT INTO SPEND (chat_id, spend) VALUES (200300, 25000.00);