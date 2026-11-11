INSERT INTO accounts (id, account_number, holder, status, balance) VALUES
(1, '000123-4', 'Ana Souza', 'ACTIVE', 1500.00),
(2, '000456-7', 'Bruno Lima', 'ACTIVE', 800.00),
(3, '000789-0', 'Carla Mendes', 'INACTIVE', 250.00),
(4, '001010-1', 'Diego Rocha', 'ACTIVE', 0.00),
(5, '001112-2', 'Elisa Costa', 'ACTIVE', 12500.75);

-- Ajusta o sequence do H2 / PostgreSQL caso utilize IDs explícitos na carga inicial
ALTER TABLE accounts ALTER COLUMN id RESTART WITH 6;