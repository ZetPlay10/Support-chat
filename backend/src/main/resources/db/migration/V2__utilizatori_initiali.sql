-- Conturi de test: clientii se inregistreaza singuri, agentii si adminii nu.
-- Parole (BCrypt): agent / agent123, admin / admin123

INSERT INTO users (username, email, password_hash, display_name, role) VALUES
    ('agent', 'agent@supportchat.local',
     '$2a$10$7CGKBOsk3/vyLV9Gf3M7xem/YAiVGCFVUk8cvZbzsbq9S6luLaBmC', 'Agent Suport', 'AGENT'),
    ('admin', 'admin@supportchat.local',
     '$2a$10$QNaGCRFSKuGlqtXF6//IteRocYjJJ00gn8OsSfsSBL9JbtFg7wMlm', 'Administrator', 'ADMIN');
