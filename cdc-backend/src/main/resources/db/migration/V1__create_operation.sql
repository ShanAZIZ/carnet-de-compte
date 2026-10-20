CREATE TABLE operation (
    id              BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    operation_date  DATE           NOT NULL,
    debit_date      DATE           NOT NULL,
    label           TEXT           NOT NULL,
    suggested_label TEXT,
    amount          NUMERIC(14, 2) NOT NULL,
    account_id      TEXT           NOT NULL,
    account_label   TEXT           NOT NULL,
    balance         NUMERIC(14, 2) NOT NULL
);
