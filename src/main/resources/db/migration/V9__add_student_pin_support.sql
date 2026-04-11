-- 학생 PIN 로그인 지원: app_users에 pin 컬럼 추가
ALTER TABLE app_users ADD COLUMN pin VARCHAR(255);

-- 기존 mock-student 계정에 PIN 설정 (기존 password 해시와 동일한 값으로, PIN 값은 student123)
UPDATE app_users SET pin = password WHERE id = 'student-1';