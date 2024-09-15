DROP TABLE IF EXISTS hits CASCADE;

CREATE TABLE IF NOT EXISTS hits (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app             VARCHAR(150)        NOT NULL,
    uri             VARCHAR(250)        NOT NULL,
    ip              VARCHAR(15)        NOT NULL,
    request_time    TIMESTAMP WITHOUT TIME ZONE NOT NULL
);