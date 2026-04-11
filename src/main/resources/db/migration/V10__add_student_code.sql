-- 학생 코드(studentCode) 컬럼 추가: 학교 범위 내 고유 학생 식별자
-- studentName 기반 인증을 studentCode 기반으로 전환하기 위한 스키마 변경

-- app_users: student_code 컬럼 추가
ALTER TABLE app_users ADD COLUMN student_code VARCHAR(64);

-- 학교 범위 내 student_code 유니크 보장
-- H2/PostgreSQL 모두 NULL 값은 중복 허용하므로 교사/운영자와 충돌하지 않습니다.
CREATE UNIQUE INDEX uk_app_users_school_student_code ON app_users(school_id, student_code);

-- 기존 mock-student 계정에 student_code 설정
UPDATE app_users SET student_code = 'S001' WHERE id = 'student-1';
UPDATE app_users SET student_code = 'S002' WHERE id = 'student-2';

-- signup_requests: student_code 컬럼 추가 (승인 시 운영자가 지정/확인)
ALTER TABLE signup_requests ADD COLUMN student_code VARCHAR(64);
