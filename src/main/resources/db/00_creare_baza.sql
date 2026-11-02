-- Pasul 1: se ruleaza conectat la baza "postgres" (pgAdmin: click dreapta pe postgres > Query Tool).
-- CREATE DATABASE nu poate rula intr-o tranzactie, deci se executa singur.
CREATE DATABASE sala_fitness
    WITH ENCODING 'UTF8'
    TEMPLATE template0;
