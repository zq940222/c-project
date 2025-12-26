# C语言数据文件智能处理平台
# 软件设计文档 (SDD)

---

**文档信息**

| 项目 | 内容 |
|------|------|
| 项目名称 | C语言数据文件智能处理平台 |
| 文档版本 | v1.0 |
| 创建日期 | 2025-12-26 |
| 文档状态 | 初稿 |

**版本历史**

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| v1.0 | 2025-12-26 | - | 初始版本 |

---

## 目录

1. [引言](#1-引言)
2. [系统概述](#2-系统概述)
3. [设计约束与考虑](#3-设计约束与考虑)
4. [系统架构设计](#4-系统架构设计)
5. [模块详细设计](#5-模块详细设计)
6. [数据库设计](#6-数据库设计)
7. [接口设计](#7-接口设计)
8. [用户界面设计](#8-用户界面设计)
9. [安全设计](#9-安全设计)
10. [部署设计](#10-部署设计)
11. [附录](#11-附录)

---

## 1. 引言

### 1.1 文档目的

本软件设计文档(Software Design Document, SDD)详细描述了"C语言数据文件智能处理平台"的系统设计方案，包括系统架构、模块设计、数据库设计、接口设计等内容，为开发团队提供详细的技术指导。

### 1.2 文档范围

本文档覆盖以下内容：
- 系统整体架构设计
- 各功能模块的详细设计
- 数据库结构设计
- 前后端接口设计
- 用户界面设计规范
- 系统部署方案

### 1.3 预期读者

| 角色 | 关注重点 |
|------|----------|
| 项目经理 | 整体架构、开发计划 |
| 架构师 | 系统架构、技术选型 |
| 开发工程师 | 模块设计、接口设计、数据库设计 |
| 测试工程师 | 功能模块、接口规范 |
| 运维工程师 | 部署设计、系统架构 |

### 1.4 参考文档

| 文档名称 | 版本 | 说明 |
|----------|------|------|
| 技术解决方案.md | v1.0 | 项目技术方案 |
| 产品解决方案（客户版）.md | v1.0 | 产品需求说明 |

### 1.5 术语定义

| 术语 | 定义 |
|------|------|
| AST | Abstract Syntax Tree，抽象语法树 |
| DSL | Domain Specific Language，领域特定语言 |
| DRL | Drools Rule Language，Drools规则语言 |
| SPA | Single Page Application，单页面应用 |
| ANTLR | ANother Tool for Language Recognition，语言识别工具 |

---

## 2. 系统概述

### 2.1 系统背景

在铁路车站信息管理等工业控制领域，大量关键数据以C语言源文件的形式存储和管理。这些文件包含结构体定义、宏定义、变量声明等，是系统运行的核心配置。

### 2.2 系统目标

开发一套基于 **Vue + Spring Boot** 的C语言数据文件处理工具，实现：

| 目标 | 描述 |
|------|------|
| 业务正确性检查 | 自动检查C语言文件中的数据是否符合业务规则 |
| 数据快速查询 | 通过简单查询语句从C语言文件中提取数据 |
| 智能文件比对 | 语法级和语义级的文件差异比较 |
| 数据安全修改 | 通过指令修改数据，自动生成正确的C代码 |

### 2.3 系统边界

```
┌─────────────────────────────────────────────────────────────────┐
│                          系统边界                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    本系统处理范围                        │   │
│  │                                                         │   │
│  │  ┌───────────┐  ┌───────────┐  ┌───────────┐           │   │
│  │  │ C语言文件  │  │ 规则管理  │  │ 数据操作  │           │   │
│  │  │ 解析处理  │  │ 配置执行  │  │ 查询修改  │           │   │
│  │  └───────────┘  └───────────┘  └───────────┘           │   │
│  │                                                         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  外部接口：文件系统 | 用户浏览器 | 数据库                        │
└─────────────────────────────────────────────────────────────────┘
```

### 2.4 用户角色

| 角色 | 描述 | 主要功能 |
|------|------|----------|
| 业务人员 | 使用系统进行数据检查和查询 | 数据查询、规则执行、结果查看 |
| 规则管理员 | 配置和管理业务检查规则 | 规则创建、编辑、发布 |
| 系统管理员 | 系统配置和用户管理 | 用户管理、系统配置、日志查看 |

---

## 3. 设计约束与考虑

### 3.1 设计约束

#### 3.1.1 技术约束

| 约束项 | 说明 |
|--------|------|
| 编程语言 | 前端：TypeScript/JavaScript；后端：Java 17+ |
| 框架版本 | Vue 3.x、Spring Boot 3.2+ |
| 数据库 | MySQL 8.0+ 或 PostgreSQL 14+ |
| 浏览器支持 | Chrome 90+、Firefox 88+、Edge 90+ |

#### 3.1.2 业务约束

| 约束项 | 说明 |
|--------|------|
| 文件格式 | 仅支持标准C语言语法的源文件 |
| 文件大小 | 单文件最大支持 10MB |
| 并发用户 | 支持最大 100 并发用户 |

### 3.2 设计原则

| 原则 | 描述 |
|------|------|
| 高内聚低耦合 | 各模块职责单一，模块间通过接口通信 |
| 可扩展性 | 支持规则类型、查询语法的扩展 |
| 容错性 | 语法错误时仍需提供部分解析结果 |
| 可维护性 | 代码结构清晰，注释完善，便于维护 |

### 3.3 技术挑战与解决方案

| 挑战 | 解决方案 |
|------|----------|
| ANTLR不支持宏定义 | 实现预处理器进行宏展开 |
| 结构体嵌套复杂 | 递归解析 + 扁平化存储 |
| 语义比对准确性 | 多层比对 + 用户确认机制 |
| 大文件性能 | 增量解析 + 异步处理 |

---

## 4. 系统架构设计

### 4.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     表现层 (Presentation Layer)                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ Monaco      │  │ 规则配置    │  │ 结果展示组件            │  │
│  │ Editor      │  │ 界面        │  │ (表格/图表/Diff视图)    │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
│                         Vue 3 + TypeScript + Ant Design Vue     │
├─────────────────────────────────────────────────────────────────┤
│                        接口层 (API Layer)                        │
│                    RESTful API + WebSocket                       │
├─────────────────────────────────────────────────────────────────┤
│                      业务层 (Business Layer)                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────┐ │
│  │ C语言解析   │  │ 规则引擎    │  │ 比对引擎    │  │ 查询   │ │
│  │ 服务        │  │ 服务        │  │ 服务        │  │ 引擎   │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └────────┘ │
│                       Spring Boot 3.x                           │
├─────────────────────────────────────────────────────────────────┤
│                      数据层 (Data Layer)                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ 结构化数据  │  │ 文件存储    │  │ 规则库      │              │
│  │ (MySQL)     │  │ (FileSystem)│  │ (MySQL)     │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 技术栈

#### 4.2.1 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.x | 前端框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Monaco Editor | 0.45+ | 代码编辑器 |
| Ant Design Vue | 4.x | UI组件库 |
| ECharts | 5.x | 图表展示 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP客户端 |

#### 4.2.2 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2+ | 后端框架 |
| ANTLR4 | 4.13+ | C语言解析 |
| Drools | 8.x | 规则引擎 |
| java-diff-utils | 4.12+ | 文本比对 |
| MyBatis-Plus | 3.5+ | ORM框架 |
| Redis | 7.x | 缓存 |
| MySQL | 8.0+ | 数据库 |

### 4.3 分层架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         Controller 层                            │
│         处理HTTP请求，参数校验，调用Service层                     │
├─────────────────────────────────────────────────────────────────┤
│                          Service 层                              │
│              业务逻辑处理，事务管理，调用各引擎                   │
├─────────────────────────────────────────────────────────────────┤
│                          Engine 层                               │
│     ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│     │ Parser   │  │ Rule     │  │ Query    │  │ Diff     │     │
│     │ Engine   │  │ Engine   │  │ Engine   │  │ Engine   │     │
│     └──────────┘  └──────────┘  └──────────┘  └──────────┘     │
├─────────────────────────────────────────────────────────────────┤
│                         Repository 层                            │
│                    数据访问，ORM映射                              │
├─────────────────────────────────────────────────────────────────┤
│                          Model 层                                │
│                    实体类，DTO，VO定义                            │
└─────────────────────────────────────────────────────────────────┘
```

### 4.4 包结构设计

```
com.example.cfileprocessor
├── controller          # 控制器层
│   ├── FileController
│   ├── RuleController
│   ├── QueryController
│   └── DiffController
├── service             # 服务层
│   ├── FileService
│   ├── RuleService
│   ├── QueryService
│   └── DiffService
├── engine              # 引擎层
│   ├── parser          # C语言解析引擎
│   │   ├── CPreprocessor
│   │   ├── CAntlrParser
│   │   └── AstBuilder
│   ├── rule            # 规则引擎
│   │   ├── RuleParser
│   │   ├── DroolsExecutor
│   │   └── RuleConverter
│   ├── query           # 查询引擎
│   │   ├── DslParser
│   │   ├── QueryExecutor
│   │   └── ResultFormatter
│   └── diff            # 比对引擎
│       ├── TextDiffer
│       ├── SyntaxDiffer
│       └── SemanticDiffer
├── repository          # 数据访问层
│   ├── StructRepository
│   ├── VariableRepository
│   ├── RuleRepository
│   └── CheckResultRepository
├── model               # 模型层
│   ├── entity          # 数据库实体
│   ├── dto             # 数据传输对象
│   └── vo              # 视图对象
├── config              # 配置类
└── util                # 工具类
```

---

## 5. 模块详细设计

### 5.1 C语言解析模块

#### 5.1.1 模块职责

解析C语言源文件，提取结构体定义、宏定义、变量声明等信息，生成结构化数据。

#### 5.1.2 处理流程

```
┌────────────────┐     ┌────────────────┐     ┌────────────────┐
│   C源文件      │────▶│   预处理器     │────▶│  ANTLR4解析    │
│  (含宏定义)    │     │  (宏展开)      │     │  (生成AST)     │
└────────────────┘     └────────────────┘     └────────────────┘
                                                      │
        ┌─────────────────────────────────────────────┘
        ▼
┌────────────────┐     ┌────────────────┐
│   AST遍历器    │────▶│  结构化数据    │
│  (数据提取)    │     │  (存储)        │
└────────────────┘     └────────────────┘
```

#### 5.1.3 核心类设计

**CPreprocessor - 预处理器类**

```java
/**
 * C语言预处理器
 * 负责宏定义展开、条件编译处理
 */
public class CPreprocessor {

    private Map<String, String> macroDefinitions;

    /**
     * 预处理C源文件
     * @param sourceCode 原始源代码
     * @return 预处理后的代码
     */
    public PreprocessResult preprocess(String sourceCode);

    /**
     * 提取宏定义
     * @param sourceCode 源代码
     * @return 宏定义映射表
     */
    public Map<String, MacroDefinition> extractMacros(String sourceCode);

    /**
     * 展开宏
     * @param code 含宏的代码
     * @return 展开后的代码
     */
    public String expandMacros(String code);
}
```

**CAntlrParser - ANTLR解析器类**

```java
/**
 * 基于ANTLR4的C语言解析器
 */
public class CAntlrParser {

    /**
     * 解析C源文件
     * @param preprocessedCode 预处理后的代码
     * @return 解析结果
     */
    public ParseResult parse(String preprocessedCode);

    /**
     * 解析并生成AST
     * @param code 源代码
     * @return 抽象语法树
     */
    public CAbstractSyntaxTree buildAST(String code);

    /**
     * 容错解析（语法错误时返回部分结果）
     * @param code 源代码
     * @return 部分解析结果
     */
    public PartialParseResult tolerantParse(String code);
}
```

#### 5.1.4 数据结构定义

```java
/**
 * 结构体定义
 */
public class StructDefinition {
    private Long id;
    private String name;                    // 结构体名称
    private List<FieldDefinition> fields;   // 字段列表
    private SourceLocation location;        // 源码位置
    private String sourceFile;              // 来源文件
    private LocalDateTime createTime;
}

/**
 * 字段定义
 */
public class FieldDefinition {
    private String name;                    // 字段名
    private String type;                    // 类型（int, char*, struct xxx等）
    private String rawType;                 // 原始类型字符串
    private boolean isPointer;              // 是否指针
    private boolean isArray;                // 是否数组
    private Integer arraySize;              // 数组大小
    private Object defaultValue;            // 默认值
}

/**
 * 变量实例
 */
public class VariableInstance {
    private Long id;
    private String name;                    // 变量名
    private String structType;              // 结构体类型
    private Long structId;                  // 关联的结构体定义ID
    private boolean isArray;                // 是否数组变量
    private List<Map<String, Object>> values; // 字段值列表
    private SourceLocation location;        // 源码位置
    private String sourceFile;              // 来源文件
}

/**
 * 源码位置（用于IDE关联显示）
 */
public class SourceLocation {
    private String filePath;
    private int startLine;
    private int endLine;
    private int startColumn;
    private int endColumn;
}

/**
 * 宏定义
 */
public class MacroDefinition {
    private String name;                    // 宏名称
    private String value;                   // 宏值
    private List<String> parameters;        // 参数列表（函数宏）
    private SourceLocation location;
}
```

### 5.2 规则引擎模块

#### 5.2.1 模块职责

管理业务检查规则，将用户低代码规则转换为可执行的Drools规则，执行规则检查并返回结果。

#### 5.2.2 处理流程

```
┌────────────────────┐     ┌────────────────────┐     ┌────────────────────┐
│  用户低代码规则    │────▶│   DSL解析器        │────▶│  规则转换器        │
│  (简单语法)        │     │   (语法分析)       │     │  (生成DRL)         │
└────────────────────┘     └────────────────────┘     └────────────────────┘
                                                              │
        ┌─────────────────────────────────────────────────────┘
        ▼
┌────────────────────┐     ┌────────────────────┐     ┌────────────────────┐
│  Drools执行引擎    │────▶│  结果收集器        │────▶│  结果格式化        │
│  (规则执行)        │     │  (问题汇总)        │     │  (输出)            │
└────────────────────┘     └────────────────────┘     └────────────────────┘
```

#### 5.2.3 规则DSL语法设计

**规则语法BNF定义**

```bnf
rule           ::= target_var field_name rule_type [rule_param]
target_var     ::= IDENTIFIER
field_name     ::= IDENTIFIER
rule_type      ::= '不重复' | '>' | '<' | '>=' | '<=' | '=' | 'MATCH' | 'IN'
rule_param     ::= NUMBER | STRING | REGEX | value_list
value_list     ::= '(' value (',' value)* ')'
value          ::= NUMBER | STRING
```

**支持的规则类型**

| 规则类型 | 低代码语法 | 说明 | 示例 |
|----------|------------|------|------|
| 唯一性检查 | `变量 字段 不重复` | 检查字段值唯一 | `园区的猫们 ID 不重复` |
| 大于检查 | `变量 字段 > 值` | 检查字段大于某值 | `园区的猫们 出生日期 > 1990` |
| 小于检查 | `变量 字段 < 值` | 检查字段小于某值 | `园区的猫们 年龄 < 20` |
| 等于检查 | `变量 字段 = 值` | 检查字段等于某值 | `园区的猫们 状态 = 正常` |
| 正则匹配 | `变量 字段 MATCH 正则` | 格式校验 | `园区的猫们 ID MATCH ^CAT\d+$` |
| 枚举检查 | `变量 字段 IN (值列表)` | 检查字段在枚举范围内 | `园区的猫们 颜色 IN (黄色,黑色,白色)` |

#### 5.2.4 核心类设计

**RuleDefinition - 规则定义类**

```java
/**
 * 规则定义实体
 */
public class RuleDefinition {
    private Long id;
    private String name;                // 规则名称
    private String description;         // 规则描述
    private String ruleType;            // 规则类型
    private String dslContent;          // 用户输入的DSL规则
    private String droolsContent;       // 转换后的Drools规则
    private RuleStatus status;          // 状态：草稿/已发布/已禁用
    private RuleSeverity severity;      // 严重程度：ERROR/WARNING/INFO
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

public enum RuleStatus {
    DRAFT,      // 草稿
    PUBLISHED,  // 已发布
    DISABLED    // 已禁用
}

public enum RuleSeverity {
    ERROR,      // 错误
    WARNING,    // 警告
    INFO        // 信息
}
```

**RuleConverter - 规则转换器**

```java
/**
 * 将用户DSL规则转换为Drools DRL
 */
public class RuleConverter {

    /**
     * 转换规则
     * @param dslRule 用户DSL规则
     * @return Drools DRL规则
     */
    public String convertToDrools(String dslRule);

    /**
     * 验证规则语法
     * @param dslRule DSL规则
     * @return 验证结果
     */
    public ValidationResult validateSyntax(String dslRule);

    /**
     * 批量转换规则
     * @param dslRules 规则列表
     * @return DRL文件内容
     */
    public String convertBatch(List<String> dslRules);
}
```

**DroolsExecutor - Drools执行器**

```java
/**
 * Drools规则执行器
 */
public class DroolsExecutor {

    private KieContainer kieContainer;

    /**
     * 执行规则检查
     * @param data 待检查数据
     * @param rules 规则列表
     * @return 检查结果
     */
    public List<CheckResult> execute(List<VariableInstance> data,
                                     List<RuleDefinition> rules);

    /**
     * 动态加载规则
     * @param droolsContent DRL内容
     */
    public void loadRules(String droolsContent);

    /**
     * 重新加载所有已发布规则
     */
    public void reloadAllRules();
}
```

#### 5.2.5 检查结果数据结构

```java
/**
 * 检查结果
 */
public class CheckResult {
    private Long id;
    private Long ruleId;                    // 关联规则ID
    private String ruleName;                // 规则名称
    private String filePath;                // 文件路径
    private CheckResultType resultType;     // 结果类型
    private String message;                 // 错误/警告信息
    private SourceLocation location;        // 问题位置
    private String contextCode;             // 上下文代码片段
    private LocalDateTime checkTime;
}

public enum CheckResultType {
    ERROR,      // 错误
    WARNING,    // 警告
    INFO,       // 信息
    PASS        // 通过
}
```

### 5.3 查询引擎模块

#### 5.3.1 模块职责

解析用户查询DSL，从解析后的结构化数据中检索符合条件的数据。

#### 5.3.2 查询DSL语法设计

**语法定义**

```bnf
query          ::= 'SEARCH' target_var [where_clause] [order_clause] [limit_clause]
where_clause   ::= 'WHERE' condition (('AND' | 'OR') condition)*
condition      ::= field_name operator value
operator       ::= '=' | '!=' | '>' | '<' | '>=' | '<=' | 'LIKE' | 'IN'
order_clause   ::= 'ORDER BY' field_name ('ASC' | 'DESC')?
limit_clause   ::= 'LIMIT' NUMBER ('OFFSET' NUMBER)?
value          ::= STRING | NUMBER | value_list
value_list     ::= '(' value (',' value)* ')'
```

**查询示例**

```sql
-- 基础查询
SEARCH 园区的猫们

-- 条件查询
SEARCH 园区的猫们 WHERE 品种 = '狸花'

-- 多条件查询
SEARCH 园区的猫们 WHERE 品种 = '狸花' AND 出生日期 > 2020

-- 排序查询
SEARCH 园区的猫们 ORDER BY 出生日期 DESC

-- 分页查询
SEARCH 园区的猫们 LIMIT 10 OFFSET 0

-- 模糊查询
SEARCH 园区的猫们 WHERE 名字 LIKE '%黄%'

-- 枚举查询
SEARCH 园区的猫们 WHERE 颜色 IN ('黄色', '白色')
```

#### 5.3.3 处理流程

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  DSL输入     │───▶│  词法分析    │───▶│  语法分析    │───▶│  AST生成     │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
                                                                   │
┌──────────────┐    ┌──────────────┐    ┌──────────────┐           │
│  结果返回    │◀───│  数据过滤    │◀───│  执行计划    │◀──────────┘
└──────────────┘    └──────────────┘    └──────────────┘
```

#### 5.3.4 核心类设计

**QueryDslParser - 查询DSL解析器**

```java
/**
 * 查询DSL解析器
 */
public class QueryDslParser {

    /**
     * 解析查询语句
     * @param dsl 查询DSL
     * @return 查询AST
     */
    public QueryAST parse(String dsl);

    /**
     * 验证查询语法
     * @param dsl 查询DSL
     * @return 验证结果
     */
    public ValidationResult validate(String dsl);
}

/**
 * 查询AST节点
 */
public class QueryAST {
    private String targetVariable;          // 查询目标变量
    private List<Condition> conditions;     // 条件列表
    private String orderField;              // 排序字段
    private OrderDirection orderDirection;  // 排序方向
    private Integer limit;                  // 限制数量
    private Integer offset;                 // 偏移量
}
```

**QueryExecutor - 查询执行器**

```java
/**
 * 查询执行器
 */
public class QueryExecutor {

    /**
     * 执行查询
     * @param ast 查询AST
     * @param data 数据源
     * @return 查询结果
     */
    public QueryResult execute(QueryAST ast, List<VariableInstance> data);

    /**
     * 执行聚合查询
     * @param ast 聚合查询AST
     * @param data 数据源
     * @return 聚合结果
     */
    public AggregateResult executeAggregate(AggregateAST ast,
                                            List<VariableInstance> data);
}

/**
 * 查询结果
 */
public class QueryResult {
    private List<Map<String, Object>> data;     // 结果数据
    private int totalCount;                      // 总数量
    private int returnedCount;                   // 返回数量
    private long executionTime;                  // 执行时间(ms)
    private List<String> columns;                // 列名列表
}
```

### 5.4 比对引擎模块

#### 5.4.1 模块职责

实现三层比对机制：文本比对、语法比对、业务比对，智能识别文件差异。

#### 5.4.2 三层比对架构

```
┌─────────────────────────────────────────────────────────────┐
│                     第一层：纯文本比对                       │
│                   (java-diff-utils)                         │
│         逐行比较，生成基础diff结果                           │
├─────────────────────────────────────────────────────────────┤
│                     第二层：语法比对                         │
│         - 忽略空白差异（空格、制表符、换行）                 │
│         - 忽略编码格式差异 (0x01 vs 0x1)                    │
│         - 忽略注释差异                                      │
├─────────────────────────────────────────────────────────────┤
│                     第三层：业务比对                         │
│         - 忽略顺序差异 ({A,B} = {B,A})                      │
│         - 语义等价检查                                      │
│         - 结构体字段对比                                    │
└─────────────────────────────────────────────────────────────┘
```

#### 5.4.3 核心类设计

**DiffEngine - 比对引擎**

```java
/**
 * 比对引擎
 */
public class DiffEngine {

    private TextDiffer textDiffer;
    private SyntaxDiffer syntaxDiffer;
    private SemanticDiffer semanticDiffer;

    /**
     * 执行完整比对
     * @param file1 文件1内容
     * @param file2 文件2内容
     * @param options 比对选项
     * @return 比对结果
     */
    public DiffResult compare(String file1, String file2, DiffOptions options);
}

/**
 * 比对选项
 */
public class DiffOptions {
    private boolean ignoreWhitespace;       // 忽略空白
    private boolean ignoreComments;         // 忽略注释
    private boolean ignoreOrder;            // 忽略顺序
    private boolean ignoreNumberFormat;     // 忽略数值格式差异
    private boolean semanticCompare;        // 启用语义比对
}
```

**DiffResult - 比对结果**

```java
/**
 * 比对结果
 */
public class DiffResult {
    private List<DiffItem> items;           // 差异项列表
    private DiffSummary summary;            // 差异摘要
    private String unifiedDiff;             // 统一diff格式输出
}

/**
 * 差异项
 */
public class DiffItem {
    private DiffType type;                  // 差异类型
    private SourceLocation leftLocation;    // 左侧位置
    private SourceLocation rightLocation;   // 右侧位置
    private String leftContent;             // 左侧内容
    private String rightContent;            // 右侧内容
    private boolean ignorable;              // 是否可忽略
    private String ignoreReason;            // 忽略原因
}

/**
 * 差异类型
 */
public enum DiffType {
    ADDED,          // 新增
    REMOVED,        // 删除
    MODIFIED,       // 修改
    MOVED,          // 移动位置
    FORMAT_ONLY,    // 仅格式差异（可忽略）
    ORDER_ONLY,     // 仅顺序差异（可忽略）
    SEMANTIC_EQUAL  // 语义等价
}

/**
 * 差异摘要
 */
public class DiffSummary {
    private int totalDiffs;                 // 总差异数
    private int significantDiffs;           // 实质性差异数
    private int ignorableDiffs;             // 可忽略差异数
    private int added;                      // 新增数
    private int removed;                    // 删除数
    private int modified;                   // 修改数
}
```

### 5.5 数据修改模块

#### 5.5.1 模块职责

解析修改指令，执行数据修改，生成正确的C语言代码，保持结构化数据与源代码同步。

#### 5.5.2 修改指令语法

```bnf
modify_stmt    ::= 'UPDATE' target_var 'SET' assignment+ ['WHERE' condition]
insert_stmt    ::= 'INSERT' 'INTO' target_var 'VALUES' '(' value_list ')'
delete_stmt    ::= 'DELETE' 'FROM' target_var 'WHERE' condition

assignment     ::= field_name '=' value
value_list     ::= value (',' value)*
```

**指令示例**

```sql
-- 修改数据
UPDATE 园区的猫们 SET 品种 = '金渐层' WHERE 名字 = '小黄'

-- 插入数据
INSERT INTO 园区的猫们 VALUES ('小花', '奶牛', 2025, '黑白')

-- 删除数据
DELETE FROM 园区的猫们 WHERE 名字 = '小花'
```

#### 5.5.3 处理流程

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   修改指令       │────▶│   指令解析       │────▶│   合法性验证     │
└──────────────────┘     └──────────────────┘     └──────────────────┘
                                                          │
┌──────────────────┐     ┌──────────────────┐             │
│   返回结果       │◀────│   代码生成       │◀────────────┘
│   (预览/确认)    │     │   (生成C代码)    │
└──────────────────┘     └──────────────────┘
         │
         ▼ (用户确认后)
┌──────────────────┐     ┌──────────────────┐
│   更新源文件     │────▶│   同步结构化数据 │
└──────────────────┘     └──────────────────┘
```

#### 5.5.4 核心类设计

**ModifyExecutor - 修改执行器**

```java
/**
 * 修改执行器
 */
public class ModifyExecutor {

    /**
     * 预览修改结果
     * @param command 修改指令
     * @param targetFile 目标文件
     * @return 修改预览
     */
    public ModifyPreview preview(String command, String targetFile);

    /**
     * 执行修改
     * @param preview 修改预览
     * @return 执行结果
     */
    public ModifyResult execute(ModifyPreview preview);

    /**
     * 撤销修改
     * @param modifyId 修改ID
     * @return 撤销结果
     */
    public boolean rollback(Long modifyId);
}

/**
 * 修改预览
 */
public class ModifyPreview {
    private String originalCode;            // 原始代码
    private String modifiedCode;            // 修改后代码
    private String unifiedDiff;             // diff格式差异
    private List<ModifyItem> items;         // 修改项列表
    private boolean valid;                  // 是否合法
    private List<String> warnings;          // 警告信息
}

/**
 * 修改项
 */
public class ModifyItem {
    private ModifyType type;                // 修改类型
    private String targetName;              // 目标名称
    private String fieldName;               // 字段名
    private Object oldValue;                // 原值
    private Object newValue;                // 新值
    private SourceLocation location;        // 位置
}

public enum ModifyType {
    UPDATE,     // 更新
    INSERT,     // 插入
    DELETE      // 删除
}
```

**CCodeGenerator - C代码生成器**

```java
/**
 * C代码生成器
 */
public class CCodeGenerator {

    /**
     * 生成变量声明代码
     * @param instance 变量实例
     * @param structDef 结构体定义
     * @return C代码
     */
    public String generateVariableCode(VariableInstance instance,
                                       StructDefinition structDef);

    /**
     * 生成结构体数组代码
     * @param instances 变量实例列表
     * @param structDef 结构体定义
     * @return C代码
     */
    public String generateArrayCode(List<VariableInstance> instances,
                                    StructDefinition structDef);

    /**
     * 格式化代码
     * @param code 原始代码
     * @param style 格式化风格
     * @return 格式化后的代码
     */
    public String formatCode(String code, CodeStyle style);
}
```

---

## 6. 数据库设计

### 6.1 E-R图

```
┌─────────────────┐         ┌─────────────────┐
│ struct_definition│         │ variable_data   │
├─────────────────┤    1:N  ├─────────────────┤
│ id (PK)         │◀────────│ id (PK)         │
│ name            │         │ struct_id (FK)  │
│ fields (JSON)   │         │ name            │
│ source_file     │         │ data (JSON)     │
│ create_time     │         │ source_location │
└─────────────────┘         │ create_time     │
                            └─────────────────┘
                                    │
                                    │ 1:N
                                    ▼
┌─────────────────┐         ┌─────────────────┐
│ rule_definition │    1:N  │ check_result    │
├─────────────────┤◀────────├─────────────────┤
│ id (PK)         │         │ id (PK)         │
│ name            │         │ rule_id (FK)    │
│ description     │         │ file_path       │
│ rule_type       │         │ result_type     │
│ dsl_content     │         │ message         │
│ drools_content  │         │ source_location │
│ status          │         │ check_time      │
│ severity        │         └─────────────────┘
│ create_time     │
│ update_time     │
└─────────────────┘

┌─────────────────┐         ┌─────────────────┐
│ macro_definition│         │ modify_history  │
├─────────────────┤         ├─────────────────┤
│ id (PK)         │         │ id (PK)         │
│ name            │         │ file_path       │
│ value           │         │ modify_type     │
│ parameters      │         │ command         │
│ source_file     │         │ before_content  │
│ source_location │         │ after_content   │
│ create_time     │         │ operator        │
└─────────────────┘         │ create_time     │
                            └─────────────────┘
```

### 6.2 数据库表结构

#### 6.2.1 结构体定义表

```sql
CREATE TABLE struct_definition (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name            VARCHAR(100) NOT NULL COMMENT '结构体名称',
    fields          JSON NOT NULL COMMENT '字段定义JSON',
    source_file     VARCHAR(500) NOT NULL COMMENT '来源文件路径',
    source_location JSON COMMENT '源码位置信息',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_name (name),
    INDEX idx_source_file (source_file)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结构体定义表';
```

**fields字段示例**

```json
[
    {
        "name": "ID",
        "type": "char*",
        "isPointer": true,
        "isArray": false
    },
    {
        "name": "品种",
        "type": "char*",
        "isPointer": true,
        "isArray": false
    },
    {
        "name": "出生日期",
        "type": "int",
        "isPointer": false,
        "isArray": false
    }
]
```

#### 6.2.2 变量数据表

```sql
CREATE TABLE variable_data (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    struct_id       BIGINT NOT NULL COMMENT '关联结构体ID',
    name            VARCHAR(100) NOT NULL COMMENT '变量名称',
    is_array        TINYINT(1) DEFAULT 0 COMMENT '是否数组',
    data            JSON NOT NULL COMMENT '变量数据JSON',
    source_file     VARCHAR(500) NOT NULL COMMENT '来源文件路径',
    source_location JSON COMMENT '源码位置信息',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_struct_id (struct_id),
    INDEX idx_name (name),
    INDEX idx_source_file (source_file),

    FOREIGN KEY (struct_id) REFERENCES struct_definition(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='变量数据表';
```

**data字段示例**

```json
[
    {
        "ID": "CAT001",
        "名字": "小黄",
        "品种": "橘猫",
        "出生日期": 2023,
        "颜色": "黄色"
    },
    {
        "ID": "CAT002",
        "名字": "小黑",
        "品种": "狸花",
        "出生日期": 2024,
        "颜色": "灰色"
    }
]
```

#### 6.2.3 规则定义表

```sql
CREATE TABLE rule_definition (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name            VARCHAR(100) NOT NULL COMMENT '规则名称',
    description     TEXT COMMENT '规则描述',
    rule_type       VARCHAR(50) NOT NULL COMMENT '规则类型',
    dsl_content     TEXT NOT NULL COMMENT '用户DSL规则内容',
    drools_content  TEXT COMMENT '转换后的Drools规则',
    status          VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/DISABLED',
    severity        VARCHAR(20) DEFAULT 'ERROR' COMMENT '严重程度: ERROR/WARNING/INFO',
    target_variable VARCHAR(100) COMMENT '目标变量名',
    target_field    VARCHAR(100) COMMENT '目标字段名',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by      VARCHAR(100) COMMENT '创建人',
    updated_by      VARCHAR(100) COMMENT '更新人',

    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_target (target_variable, target_field)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则定义表';
```

#### 6.2.4 检查结果表

```sql
CREATE TABLE check_result (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    batch_id        VARCHAR(50) NOT NULL COMMENT '批次ID',
    rule_id         BIGINT NOT NULL COMMENT '关联规则ID',
    rule_name       VARCHAR(100) COMMENT '规则名称',
    file_path       VARCHAR(500) NOT NULL COMMENT '检查文件路径',
    result_type     VARCHAR(20) NOT NULL COMMENT '结果类型: ERROR/WARNING/INFO/PASS',
    message         TEXT COMMENT '错误/警告信息',
    source_location JSON COMMENT '问题位置信息',
    context_code    TEXT COMMENT '上下文代码片段',
    check_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',

    INDEX idx_batch_id (batch_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_file_path (file_path),
    INDEX idx_result_type (result_type),
    INDEX idx_check_time (check_time),

    FOREIGN KEY (rule_id) REFERENCES rule_definition(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查结果表';
```

#### 6.2.5 宏定义表

```sql
CREATE TABLE macro_definition (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name            VARCHAR(100) NOT NULL COMMENT '宏名称',
    value           TEXT COMMENT '宏值',
    parameters      JSON COMMENT '参数列表(函数宏)',
    is_function     TINYINT(1) DEFAULT 0 COMMENT '是否函数宏',
    source_file     VARCHAR(500) NOT NULL COMMENT '来源文件路径',
    source_location JSON COMMENT '源码位置信息',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_name (name),
    INDEX idx_source_file (source_file)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宏定义表';
```

#### 6.2.6 修改历史表

```sql
CREATE TABLE modify_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_path       VARCHAR(500) NOT NULL COMMENT '文件路径',
    modify_type     VARCHAR(20) NOT NULL COMMENT '修改类型: UPDATE/INSERT/DELETE',
    command         TEXT NOT NULL COMMENT '修改指令',
    before_content  LONGTEXT COMMENT '修改前内容',
    after_content   LONGTEXT COMMENT '修改后内容',
    diff_content    LONGTEXT COMMENT 'diff内容',
    status          VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态: COMPLETED/ROLLBACK',
    operator        VARCHAR(100) COMMENT '操作人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_file_path (file_path),
    INDEX idx_modify_type (modify_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='修改历史表';
```

#### 6.2.7 用户表

```sql
CREATE TABLE sys_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(200) NOT NULL COMMENT '密码(加密)',
    nickname        VARCHAR(50) COMMENT '昵称',
    email           VARCHAR(100) COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '手机号',
    role            VARCHAR(20) DEFAULT 'USER' COMMENT '角色: ADMIN/RULE_ADMIN/USER',
    status          TINYINT(1) DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最后登录时间',

    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

---

## 7. 接口设计

### 7.1 接口规范

#### 7.1.1 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {},
    "timestamp": 1703577600000
}
```

**响应码定义**

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

#### 7.1.2 分页请求格式

```json
{
    "pageNum": 1,
    "pageSize": 10,
    "orderBy": "createTime",
    "orderDir": "desc"
}
```

#### 7.1.3 分页响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "list": [],
        "total": 100,
        "pageNum": 1,
        "pageSize": 10,
        "pages": 10
    }
}
```

### 7.2 RESTful API设计

#### 7.2.1 文件解析接口

**上传并解析文件**

```
POST /api/v1/parse/upload
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | C语言源文件 |
| projectId | Long | 否 | 项目ID |

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "fileId": "f001",
        "fileName": "data.c",
        "parseResult": {
            "structs": [
                {
                    "id": 1,
                    "name": "猫",
                    "fields": [
                        {"name": "ID", "type": "char*"},
                        {"name": "品种", "type": "char*"},
                        {"name": "出生日期", "type": "int"}
                    ]
                }
            ],
            "variables": [
                {
                    "id": 1,
                    "name": "园区的猫们",
                    "structType": "猫",
                    "count": 5
                }
            ],
            "macros": [
                {"name": "WHITE", "value": "白色"}
            ],
            "errors": [],
            "warnings": []
        }
    }
}
```

**解析文本内容**

```
POST /api/v1/parse/content
Content-Type: application/json
```

**请求体**

```json
{
    "content": "struct 猫 { char* ID; char* 品种; };",
    "fileName": "temp.c"
}
```

#### 7.2.2 规则管理接口

**获取规则列表**

```
GET /api/v1/rules
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 状态筛选 |
| keyword | String | 否 | 关键词搜索 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

**创建规则**

```
POST /api/v1/rules
Content-Type: application/json
```

**请求体**

```json
{
    "name": "ID唯一性检查",
    "description": "检查园区的猫ID是否重复",
    "dslContent": "园区的猫们 ID 不重复",
    "severity": "ERROR"
}
```

**更新规则**

```
PUT /api/v1/rules/{id}
Content-Type: application/json
```

**删除规则**

```
DELETE /api/v1/rules/{id}
```

**发布规则**

```
POST /api/v1/rules/{id}/publish
```

**执行规则检查**

```
POST /api/v1/rules/execute
Content-Type: application/json
```

**请求体**

```json
{
    "ruleIds": [1, 2, 3],
    "fileIds": ["f001", "f002"],
    "async": false
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "batchId": "batch001",
        "totalFiles": 2,
        "totalRules": 3,
        "results": [
            {
                "ruleId": 1,
                "ruleName": "ID唯一性检查",
                "filePath": "/path/to/data.c",
                "resultType": "ERROR",
                "message": "ID \"CAT001\" 重复出现",
                "location": {
                    "startLine": 15,
                    "endLine": 15,
                    "startColumn": 5,
                    "endColumn": 20
                }
            }
        ],
        "summary": {
            "errorCount": 1,
            "warningCount": 0,
            "passCount": 5
        }
    }
}
```

#### 7.2.3 查询接口

**执行查询**

```
POST /api/v1/query
Content-Type: application/json
```

**请求体**

```json
{
    "dsl": "SEARCH 园区的猫们 WHERE 品种 = '狸花'",
    "fileIds": ["f001"]
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "columns": ["ID", "名字", "品种", "出生日期", "颜色"],
        "rows": [
            {
                "ID": "CAT002",
                "名字": "小黑",
                "品种": "狸花",
                "出生日期": 2024,
                "颜色": "灰色",
                "_location": {
                    "startLine": 10,
                    "endLine": 10
                }
            }
        ],
        "totalCount": 1,
        "executionTime": 15
    }
}
```

**获取可查询变量列表**

```
GET /api/v1/query/variables
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | String | 否 | 文件ID |

#### 7.2.4 比对接口

**执行文件比对**

```
POST /api/v1/diff
Content-Type: application/json
```

**请求体**

```json
{
    "leftFileId": "f001",
    "rightFileId": "f002",
    "options": {
        "ignoreWhitespace": true,
        "ignoreComments": true,
        "ignoreOrder": true,
        "ignoreNumberFormat": true,
        "semanticCompare": true
    }
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "summary": {
            "totalDiffs": 5,
            "significantDiffs": 2,
            "ignorableDiffs": 3,
            "added": 1,
            "removed": 0,
            "modified": 1
        },
        "items": [
            {
                "type": "ADDED",
                "rightLocation": {"startLine": 12, "endLine": 12},
                "rightContent": "{\"小花\", \"奶牛\", 2025, \"黑白\"}",
                "ignorable": false
            },
            {
                "type": "MODIFIED",
                "leftLocation": {"startLine": 8, "endLine": 8},
                "rightLocation": {"startLine": 8, "endLine": 8},
                "leftContent": "{\"小黄\", \"橘猫\", 2023, \"黄色\"}",
                "rightContent": "{\"小黄\", \"金渐层\", 2023, \"黄色\"}",
                "ignorable": false
            },
            {
                "type": "ORDER_ONLY",
                "ignorable": true,
                "ignoreReason": "仅顺序差异"
            }
        ],
        "unifiedDiff": "--- left.c\n+++ right.c\n..."
    }
}
```

#### 7.2.5 数据修改接口

**预览修改**

```
POST /api/v1/modify/preview
Content-Type: application/json
```

**请求体**

```json
{
    "fileId": "f001",
    "command": "UPDATE 园区的猫们 SET 品种 = '金渐层' WHERE 名字 = '小黄'"
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "valid": true,
        "preview": {
            "originalCode": "猫 园区的猫们[] = {\n    {\"小黄\", \"橘猫\", 2023, \"黄色\"},\n    ...\n};",
            "modifiedCode": "猫 园区的猫们[] = {\n    {\"小黄\", \"金渐层\", 2023, \"黄色\"},\n    ...\n};",
            "unifiedDiff": "...",
            "items": [
                {
                    "type": "UPDATE",
                    "targetName": "小黄",
                    "fieldName": "品种",
                    "oldValue": "橘猫",
                    "newValue": "金渐层",
                    "location": {"startLine": 2}
                }
            ]
        },
        "warnings": []
    }
}
```

**执行修改**

```
POST /api/v1/modify/execute
Content-Type: application/json
```

**请求体**

```json
{
    "fileId": "f001",
    "command": "UPDATE 园区的猫们 SET 品种 = '金渐层' WHERE 名字 = '小黄'",
    "confirmed": true
}
```

**撤销修改**

```
POST /api/v1/modify/rollback/{historyId}
```

**获取修改历史**

```
GET /api/v1/modify/history
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | String | 否 | 文件ID |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

### 7.3 WebSocket接口

#### 7.3.1 实时检查结果推送

**连接地址**

```
WS /ws/check-result
```

**消息格式**

```json
{
    "type": "CHECK_PROGRESS",
    "batchId": "batch001",
    "progress": 50,
    "currentFile": "data.c",
    "currentRule": "ID唯一性检查"
}
```

```json
{
    "type": "CHECK_RESULT",
    "batchId": "batch001",
    "result": {
        "ruleId": 1,
        "ruleName": "ID唯一性检查",
        "filePath": "/path/to/data.c",
        "resultType": "ERROR",
        "message": "ID重复"
    }
}
```

```json
{
    "type": "CHECK_COMPLETE",
    "batchId": "batch001",
    "summary": {
        "errorCount": 1,
        "warningCount": 0,
        "passCount": 5
    }
}
```

---

## 8. 用户界面设计

### 8.1 界面布局

#### 8.1.1 主界面布局

```
┌─────────────────────────────────────────────────────────────────────┐
│  Logo   文件  编辑  查询  检查  比对  帮助         用户: 张三  退出  │
├─────────────────────────────────────────────────────────────────────┤
│ ┌─────────┐ ┌───────────────────────────────────────────────────┐   │
│ │ 项目    │ │                                                   │   │
│ │ 文件树  │ │              代码编辑区                           │   │
│ │         │ │          (Monaco Editor)                          │   │
│ │ 📁项目A │ │                                                   │   │
│ │  📄data │ │   struct 猫 {                                     │   │
│ │  📄conf │ │       char* ID;         ⚠ 第15行: ID重复         │   │
│ │ 📁项目B │ │       char* 品种;                                 │   │
│ │         │ │       int 出生日期;                               │   │
│ │         │ │   };                                              │   │
│ │         │ │                                                   │   │
│ └─────────┘ └───────────────────────────────────────────────────┘   │
│             ┌───────────────────────────────────────────────────┐   │
│             │ 问题 | 输出 | 终端 | 查询结果                     │   │
│             ├───────────────────────────────────────────────────┤   │
│             │ ⚠ 第15行: ID "CAT001" 重复                       │   │
│             │ ✗ 第23行: 出生日期不符合规则                     │   │
│             │                                                   │   │
│             └───────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────┤
│ 就绪                                           行: 15  列: 10  UTF-8│
└─────────────────────────────────────────────────────────────────────┘
```

#### 8.1.2 规则配置界面

```
┌─────────────────────────────────────────────────────────────────────┐
│                           规则配置                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  规则名称:   [ ID唯一性检查                              ]          │
│                                                                     │
│  规则描述:   [ 检查数据ID是否存在重复                    ]          │
│                                                                     │
│  目标变量:   [ 园区的猫们                              ▼ ]          │
│                                                                     │
│  规则类型:   ● 唯一性检查    ○ 范围检查                             │
│              ○ 格式校验      ○ 自定义规则                           │
│                                                                     │
│  检查字段:   [ ID                                      ▼ ]          │
│                                                                     │
│  严重程度:   ○ 错误(ERROR)   ● 警告(WARNING)   ○ 提示(INFO)        │
│                                                                     │
│  ───────────────────────────────────────────────────────────────    │
│  生成的规则语句:                                                    │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  园区的猫们 ID 不重复                                       │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│                    [ 测试 ]    [ 保存草稿 ]    [ 发布 ]    [ 取消 ] │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

#### 8.1.3 查询界面

```
┌─────────────────────────────────────────────────────────────────────┐
│                           数据查询                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  查询语句:                                                          │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  SEARCH 园区的猫们 WHERE 品种 = '狸花' ORDER BY 出生日期    │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                     [ 执行查询 ]    │
│                                                                     │
│  查询结果 (2条记录, 耗时15ms):                                      │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ ID       │ 名字   │ 品种   │ 出生日期 │ 颜色   │ 位置      │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ CAT002   │ 小黑   │ 狸花   │ 2024     │ 灰色   │ 第10行    │   │
│  │ CAT005   │ 花花   │ 狸花   │ 2023     │ 花色   │ 第18行    │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│                                    [ 导出Excel ]  [ 导出CSV ]       │
└─────────────────────────────────────────────────────────────────────┘
```

#### 8.1.4 比对界面

```
┌─────────────────────────────────────────────────────────────────────┐
│                           文件比对                                   │
├─────────────────────────────────────────────────────────────────────┤
│  比对选项: ☑ 忽略空白  ☑ 忽略注释  ☑ 忽略顺序  ☑ 语义比对         │
├────────────────────────────┬────────────────────────────────────────┤
│       左侧文件 (v1.c)      │        右侧文件 (v2.c)                 │
├────────────────────────────┼────────────────────────────────────────┤
│  8│ {小黄, 橘猫, 2023}     │  8│ {小黄, 金渐层, 2023}   ← 修改      │
│  9│ {小黑, 狸花, 2024}     │  9│ {小黑, 狸花, 2024}                 │
│ 10│                        │ 10│ {小花, 奶牛, 2025}     ← 新增      │
├────────────────────────────┴────────────────────────────────────────┤
│  📊 比对摘要                                                        │
│  ├─ 实质性变化: 2处                                                 │
│  │   ├─ 新增: 1处                                                   │
│  │   └─ 修改: 1处                                                   │
│  └─ 已过滤差异: 3处 (顺序差异2处, 格式差异1处)                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 8.2 组件设计

#### 8.2.1 Monaco Editor配置

```typescript
// Monaco Editor配置
const editorOptions: monaco.editor.IStandaloneEditorConstructionOptions = {
    language: 'c',
    theme: 'vs',
    fontSize: 14,
    lineNumbers: 'on',
    minimap: { enabled: true },
    scrollBeyondLastLine: false,
    automaticLayout: true,
    readOnly: false,
    glyphMargin: true,  // 用于显示错误标记
};

// 自定义DSL语言配置
monaco.languages.register({ id: 'rule-dsl' });
monaco.languages.setMonarchTokensProvider('rule-dsl', {
    tokenizer: {
        root: [
            [/SEARCH|WHERE|ORDER BY|LIMIT|AND|OR/, 'keyword'],
            [/不重复|MATCH|IN/, 'keyword.operator'],
            [/'[^']*'/, 'string'],
            [/\d+/, 'number'],
            [/[a-zA-Z_\u4e00-\u9fa5][a-zA-Z0-9_\u4e00-\u9fa5]*/, 'identifier'],
        ]
    }
});
```

#### 8.2.2 问题标记组件

```typescript
// 问题标记接口
interface ProblemMarker {
    severity: monaco.MarkerSeverity;
    message: string;
    startLineNumber: number;
    startColumn: number;
    endLineNumber: number;
    endColumn: number;
    source: string;
}

// 设置问题标记
function setMarkers(model: monaco.editor.ITextModel, markers: ProblemMarker[]) {
    monaco.editor.setModelMarkers(model, 'rule-check', markers);
}
```

### 8.3 交互流程

#### 8.3.1 规则检查流程

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐
│ 选择文件 │────▶│ 选择规则 │────▶│ 执行检查 │────▶│ 查看结果 │
└─────────┘     └─────────┘     └─────────┘     └─────────┘
                                     │
                                     ▼
                              ┌─────────────┐
                              │ 进度显示    │
                              │ (WebSocket) │
                              └─────────────┘
                                     │
                                     ▼
                              ┌─────────────┐
                              │ 点击问题    │
                              │ 定位到源码  │
                              └─────────────┘
```

#### 8.3.2 数据修改流程

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐
│ 输入指令 │────▶│ 预览修改 │────▶│ 确认修改 │────▶│ 执行修改 │────▶│ 查看结果 │
└─────────┘     └─────────┘     └─────────┘     └─────────┘     └─────────┘
                    │                                               │
                    ▼                                               ▼
              ┌─────────────┐                                ┌─────────────┐
              │ 显示diff    │                                │ 支持撤销    │
              │ 预览        │                                │ (rollback)  │
              └─────────────┘                                └─────────────┘
```

---

## 9. 安全设计

### 9.1 认证与授权

#### 9.1.1 认证机制

- 采用JWT(JSON Web Token)进行用户认证
- Token有效期：2小时
- 支持Token刷新机制

**JWT Token结构**

```json
{
    "header": {
        "alg": "HS256",
        "typ": "JWT"
    },
    "payload": {
        "sub": "user123",
        "username": "张三",
        "role": "RULE_ADMIN",
        "iat": 1703577600,
        "exp": 1703584800
    }
}
```

#### 9.1.2 权限控制

| 角色 | 权限 |
|------|------|
| ADMIN | 所有权限 |
| RULE_ADMIN | 规则管理、数据查询、文件比对 |
| USER | 数据查询、文件比对、只读规则 |

**接口权限矩阵**

| 接口 | ADMIN | RULE_ADMIN | USER |
|------|-------|------------|------|
| POST /api/v1/rules | ✓ | ✓ | ✗ |
| PUT /api/v1/rules/{id} | ✓ | ✓ | ✗ |
| DELETE /api/v1/rules/{id} | ✓ | ✗ | ✗ |
| POST /api/v1/rules/execute | ✓ | ✓ | ✓ |
| POST /api/v1/query | ✓ | ✓ | ✓ |
| POST /api/v1/modify/execute | ✓ | ✓ | ✗ |

### 9.2 数据安全

#### 9.2.1 敏感数据处理

- 用户密码使用BCrypt加密存储
- 敏感配置使用环境变量或加密配置文件
- 日志脱敏处理

#### 9.2.2 文件安全

- 上传文件类型限制：仅允许.c、.h文件
- 文件大小限制：单文件最大10MB
- 文件存储路径隔离，禁止路径穿越

### 9.3 输入验证

#### 9.3.1 DSL注入防护

- DSL解析器严格校验语法
- 禁止执行任意代码
- 参数化查询

#### 9.3.2 XSS防护

- 输出编码
- Content Security Policy
- HttpOnly Cookie

### 9.4 审计日志

```sql
CREATE TABLE sys_audit_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT COMMENT '用户ID',
    username        VARCHAR(50) COMMENT '用户名',
    operation       VARCHAR(100) COMMENT '操作类型',
    method          VARCHAR(10) COMMENT 'HTTP方法',
    url             VARCHAR(500) COMMENT '请求URL',
    ip              VARCHAR(50) COMMENT 'IP地址',
    params          TEXT COMMENT '请求参数',
    result          VARCHAR(20) COMMENT '操作结果',
    error_msg       TEXT COMMENT '错误信息',
    duration        BIGINT COMMENT '耗时(ms)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

---

## 10. 部署设计

### 10.1 部署架构

#### 10.1.1 开发环境

```
┌─────────────────┐     ┌─────────────────┐
│   Vue Dev       │────▶│  Spring Boot    │
│   Server        │     │  (localhost:    │
│   (localhost:   │     │   8080)         │
│    5173)        │     │                 │
└─────────────────┘     └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │    H2/MySQL     │
                        │  (localhost:    │
                        │   3306)         │
                        └─────────────────┘
```

#### 10.1.2 生产环境

```
                    ┌─────────────────┐
                    │   负载均衡器    │
                    │   (Nginx/SLB)   │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
       ┌───────────┐  ┌───────────┐  ┌───────────┐
       │  App-1    │  │  App-2    │  │  App-3    │
       │(Spring    │  │(Spring    │  │(Spring    │
       │ Boot)     │  │ Boot)     │  │ Boot)     │
       └─────┬─────┘  └─────┬─────┘  └─────┬─────┘
             │              │              │
             └──────────────┼──────────────┘
                            │
              ┌─────────────┴─────────────┐
              ▼                           ▼
       ┌───────────┐               ┌───────────┐
       │  MySQL    │               │  Redis    │
       │  (主从)   │               │  (集群)   │
       └───────────┘               └───────────┘
```

### 10.2 容器化部署

#### 10.2.1 Docker Compose配置

```yaml
version: '3.8'

services:
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - app-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
    networks:
      - app-network

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root123
      - MYSQL_DATABASE=c_file_processor
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    volumes:
      - redis-data:/data
    networks:
      - app-network

volumes:
  mysql-data:
  redis-data:

networks:
  app-network:
    driver: bridge
```

#### 10.2.2 后端Dockerfile

```dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 10.2.3 前端Dockerfile

```dockerfile
# 构建阶段
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 10.3 配置管理

#### 10.3.1 应用配置

```yaml
# application-prod.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/c_file_processor
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root123}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  redis:
    host: ${REDIS_HOST:localhost}
    port: 6379

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# 规则引擎配置
drools:
  rules-path: classpath:rules/

# 文件存储配置
file:
  upload-path: /data/uploads
  temp-path: /data/temp

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 7200
```

### 10.4 监控与日志

#### 10.4.1 健康检查端点

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

#### 10.4.2 日志配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <property name="LOG_PATH" value="/var/log/app"/>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/app.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

---

## 11. 附录

### 11.1 开源组件清单

| 组件 | 版本 | 用途 | 许可证 |
|------|------|------|--------|
| Vue.js | 3.x | 前端框架 | MIT |
| TypeScript | 5.x | 类型安全 | Apache-2.0 |
| Vite | 5.x | 构建工具 | MIT |
| Monaco Editor | 0.45+ | 代码编辑器 | MIT |
| Ant Design Vue | 4.x | UI组件库 | MIT |
| ECharts | 5.x | 图表展示 | Apache-2.0 |
| Pinia | 2.x | 状态管理 | MIT |
| Spring Boot | 3.2+ | 后端框架 | Apache-2.0 |
| ANTLR4 | 4.13+ | C语言解析 | BSD-3 |
| Drools | 8.x | 规则引擎 | Apache-2.0 |
| java-diff-utils | 4.12+ | 文本比对 | Apache-2.0 |
| MyBatis-Plus | 3.5+ | ORM框架 | Apache-2.0 |

### 11.2 错误码定义

| 错误码 | 说明 |
|--------|------|
| 10001 | 文件解析失败 |
| 10002 | 文件格式不支持 |
| 10003 | 文件大小超限 |
| 20001 | 规则语法错误 |
| 20002 | 规则执行失败 |
| 20003 | 规则不存在 |
| 30001 | 查询语法错误 |
| 30002 | 查询目标不存在 |
| 40001 | 比对文件不存在 |
| 40002 | 比对执行失败 |
| 50001 | 修改语法错误 |
| 50002 | 修改目标不存在 |
| 50003 | 修改验证失败 |

### 11.3 性能指标

| 指标 | 目标值 |
|------|--------|
| 文件解析响应时间 | < 2秒 (1MB文件) |
| 规则检查响应时间 | < 5秒 (10条规则/文件) |
| 查询响应时间 | < 500ms |
| 比对响应时间 | < 3秒 (1MB文件) |
| 并发用户数 | 100 |
| 系统可用性 | 99.9% |

---

*文档版本: v1.0*
*创建日期: 2025-12-26*
