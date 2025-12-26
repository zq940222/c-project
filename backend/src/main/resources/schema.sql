-- 结构体定义表
CREATE TABLE IF NOT EXISTS struct_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    fields CLOB,
    source_file VARCHAR(500),
    source_location CLOB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 变量数据表
CREATE TABLE IF NOT EXISTS variable_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    struct_id BIGINT,
    name VARCHAR(100) NOT NULL,
    is_array BOOLEAN DEFAULT FALSE,
    data CLOB,
    source_file VARCHAR(500),
    source_location CLOB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 规则定义表
CREATE TABLE IF NOT EXISTS rule_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description CLOB,
    rule_type VARCHAR(50),
    dsl_content CLOB NOT NULL,
    target_variable VARCHAR(100),
    target_field VARCHAR(100),
    status VARCHAR(20) DEFAULT 'DRAFT',
    severity VARCHAR(20) DEFAULT 'ERROR',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入示例规则
INSERT INTO rule_definition (name, description, dsl_content, target_variable, target_field, status, severity)
VALUES
('ID唯一性检查', '检查数据ID是否重复', '园区的猫们 ID 不重复', '园区的猫们', 'ID', 'PUBLISHED', 'ERROR'),
('出生日期检查', '检查出生日期是否合理', '园区的猫们 出生日期 > 1990', '园区的猫们', '出生日期', 'PUBLISHED', 'WARNING'),
('颜色枚举检查', '检查颜色是否在允许范围内', '园区的猫们 颜色 IN (黄色, 黑色, 白色, 灰色, 花色)', '园区的猫们', '颜色', 'PUBLISHED', 'WARNING');
