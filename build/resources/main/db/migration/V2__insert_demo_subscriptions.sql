-- Demo job alert subscriptions covering different filters and active states.

INSERT INTO alert_subscriptions (id, user_id, user_email, keywords, location, min_salary, max_salary, employment_type, work_format, skill_ids, is_active, created_at, updated_at)
VALUES
    (100, 101, 'alex.backend@laborexchange.demo', 'Java,Spring,Kafka', 'Екатеринбург', 250000, 380000, 'FULL_TIME', 'HYBRID', '1,2,100,101', TRUE, NOW() - INTERVAL '20 days', NOW()),
    (101, 102, 'maria.frontend@laborexchange.demo', 'React,Next.js,Frontend', '', 160000, 260000, 'FULL_TIME', 'REMOTE', '11,12,106', TRUE, NOW() - INTERVAL '18 days', NOW()),
    (102, 103, 'ivan.data@laborexchange.demo', 'Data,Analytics,ClickHouse', 'Москва', 150000, 280000, 'FULL_TIME', 'HYBRID', '16,33,111,112', TRUE, NOW() - INTERVAL '15 days', NOW()),
    (103, 104, 'olga.qa@laborexchange.demo', 'QA,Automation,Playwright', '', 120000, 220000, 'FULL_TIME', 'REMOTE', '42,45,118', TRUE, NOW() - INTERVAL '12 days', NOW()),
    (104, 105, 'nikita.devops@laborexchange.demo', 'DevOps,SRE,Kubernetes', '', 200000, 320000, 'CONTRACT', 'HYBRID', '18,23,38,116', TRUE, NOW() - INTERVAL '9 days', NOW()),
    (105, 106, 'pending.email@laborexchange.demo', 'Junior,Python', 'Новосибирск', 70000, 130000, 'INTERNSHIP', 'HYBRID', '16,17,33', FALSE, NOW() - INTERVAL '4 days', NOW())
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    user_email = EXCLUDED.user_email,
    keywords = EXCLUDED.keywords,
    location = EXCLUDED.location,
    min_salary = EXCLUDED.min_salary,
    max_salary = EXCLUDED.max_salary,
    employment_type = EXCLUDED.employment_type,
    work_format = EXCLUDED.work_format,
    skill_ids = EXCLUDED.skill_ids,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

SELECT setval(pg_get_serial_sequence('alert_subscriptions', 'id'), COALESCE((SELECT MAX(id) FROM alert_subscriptions), 1), TRUE);
