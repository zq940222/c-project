# C语言数据文件智能处理平台

一套基于 Vue + Spring Boot 的C语言数据文件处理工具，用于处理以C语言格式存储数据的文件。

## 功能特性

- **智能数据检查**：自动检查C语言文件中的数据是否符合业务规则
- **数据快速查询**：用简单的查询语句从C语言文件中检索数据
- **智能文件比对**：语法级和语义级的文件差异比较，自动过滤无意义差异
- **规则管理**：低代码方式配置和管理业务检查规则

## 技术栈

### 后端
- Spring Boot 3.2
- MyBatis-Plus 3.5
- H2 Database (开发环境)
- java-diff-utils 4.12

### 前端
- Vue 3.4
- Vite 5
- Ant Design Vue 4
- Monaco Editor 0.45
- Pinia 2

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Maven 3.8+

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:5173 启动

## 项目结构

```
c-project/
├── backend/                 # 后端Spring Boot项目
│   ├── src/main/java/
│   │   └── com/cfileprocessor/
│   │       ├── controller/  # REST API控制器
│   │       ├── service/     # 业务服务
│   │       ├── engine/      # 核心引擎
│   │       │   ├── parser/  # C语言解析引擎
│   │       │   ├── rule/    # 规则引擎
│   │       │   ├── query/   # 查询引擎
│   │       │   └── diff/    # 比对引擎
│   │       ├── model/       # 数据模型
│   │       └── repository/  # 数据访问层
│   └── pom.xml
├── frontend/                # 前端Vue项目
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── components/     # 通用组件
│   │   ├── api/            # API接口
│   │   ├── stores/         # Pinia状态管理
│   │   └── router/         # 路由配置
│   └── package.json
├── samples/                 # 示例数据文件
└── docs/                    # 文档
```

## 功能演示

### 1. 代码编辑与规则检查

1. 打开 http://localhost:5173
2. 点击"加载示例"加载示例C代码
3. 点击"解析"查看解析结果
4. 点击"规则检查"执行业务规则检查
5. 在问题列表中点击问题可定位到代码位置

### 2. 数据查询

1. 切换到"数据查询"页面
2. 加载示例数据源并解析
3. 输入查询语句，例如：
   - `SEARCH 园区的猫们` - 查询所有数据
   - `SEARCH 园区的猫们 WHERE 品种 = '狸花'` - 条件查询
   - `SEARCH 园区的猫们 ORDER BY 出生日期 DESC` - 排序查询

### 3. 规则管理

1. 切换到"规则管理"页面
2. 查看预置规则
3. 创建新规则：选择规则类型，填写目标变量和字段
4. 发布规则后即可在检查中使用

### 4. 文件比对

1. 切换到"文件比对"页面
2. 分别加载左右两侧示例
3. 选择比对选项
4. 点击"执行比对"查看差异
5. 系统会自动识别顺序差异、格式差异等可忽略差异

## API接口

### 文件解析
- `POST /api/v1/parse/content` - 解析文本内容

### 规则管理
- `GET /api/v1/rules` - 获取规则列表
- `POST /api/v1/rules` - 创建规则
- `POST /api/v1/rules/execute` - 执行规则检查

### 数据查询
- `POST /api/v1/query` - 执行DSL查询

### 文件比对
- `POST /api/v1/diff` - 执行文件比对

## 规则DSL语法

```
# 唯一性检查
变量名 字段名 不重复

# 范围检查
变量名 字段名 > 值
变量名 字段名 < 值
变量名 字段名 >= 值
变量名 字段名 <= 值

# 枚举检查
变量名 字段名 IN (值1, 值2, 值3)

# 正则匹配
变量名 字段名 MATCH 正则表达式
```

## 查询DSL语法

```sql
SEARCH 变量名 [WHERE 条件] [ORDER BY 字段 [ASC|DESC]] [LIMIT 数量]

-- 示例
SEARCH 园区的猫们
SEARCH 园区的猫们 WHERE 品种 = '狸花'
SEARCH 园区的猫们 WHERE 出生日期 > 2022 ORDER BY 出生日期 DESC
SEARCH 园区的猫们 LIMIT 10
```

## License

MIT
