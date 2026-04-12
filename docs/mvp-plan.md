# 可视化爬虫 MVP 计划

## 1. 文档用途

本文档是 `visual_spider2` 的执行基线，用于统一以下内容：

- MVP 范围
- 已确认的默认设计决策
- 按里程碑拆分的实施计划
- 每个里程碑的验收标准
- 后续执行时的文档更新约定

除非后续明确调整，否则开发工作默认以本文档为准。每个里程碑完成后，都必须更新本文档中的状态、实现结果、偏差说明和验证记录。

## 2. 项目目标

构建一个基于 Java 的可视化爬虫 MVP，主流程为：

`URL 输入 -> 可视化选区 -> 规则生成 -> 抽取预览 -> 字段校验 -> 数据库映射 -> Quartz 定时调度`

## 3. 技术约束

- Java 21
- Spring Boot
- Thymeleaf
- MyBatis
- PostgreSQL
- Quartz
- Playwright for Java
- Jsoup
- 单体应用
- 第一版优先采用服务端渲染后台页面

## 4. 不可违反的约束

- 除非明确要求，否则不要引入新的前端框架
- 每个里程碑结束时必须能编译通过
- 每个数据库变更都必须包含 migration SQL
- 每个功能变更都必须给出至少一条可执行的手工验证路径
- 不要重写或重构无关模块
- 规则配置必须支持版本化
- 不要把规则系统设计成只依赖单个 XPath
- 不使用 iframe 嵌入第三方页面做选区
- 只做 MVP，不做分布式爬虫

## 5. 已确认的默认设计决策

以下设计点已按默认值确认，后续除非显式变更，否则直接据此实施。

### 5.1 目标站点范围

- 先支持公开可访问、无需登录、单页详情抽取
- 暂不覆盖登录态、多步翻页、反爬对抗

### 5.2 `article` 表最小字段集合

- `id`
- `source_url`
- `title`
- `author`
- `published_at`
- `summary`
- `content`
- `cover_image`
- `created_at`
- `updated_at`

说明：

- 允许部分字段为空
- 后续如需扩展字段，必须补 migration SQL 和映射说明

### 5.3 规则版本发布语义

- 规则支持多版本保存
- 同一抓取任务只允许绑定一个已发布版本
- 预览可以使用草稿版本
- 定时任务只能使用已发布版本

### 5.4 字段校验范围

MVP 仅覆盖以下校验能力：

- 必填
- 长度
- 时间格式
- URL 格式
- 去空白
- 简单去重

暂不引入复杂业务校验引擎。

### 5.5 调度模型

- 一个抓取任务绑定一个 URL 模板和一个已发布规则版本
- Quartz 仅支持 Cron 表达式调度
- 暂不支持任务依赖、分片、分布式执行

### 5.6 页面快照形式

- 每次运行保存 `HTML 快照 + PNG 截图 + 抽取结果 JSON`
- 快照文件先落磁盘
- 数据库存储索引路径和元数据

## 6. 里程碑总览

| 里程碑 | 名称 | 目标 |
| --- | --- | --- |
| M1 | 基础骨架与可运行环境 | 打通 Spring Boot、数据库、Playwright、Quartz 基础链路 |
| M2 | URL 输入与页面加载预览 | 支持输入 URL 并加载真实页面生成预览 |
| M3 | 可视化选区与字段规则创建 | 支持点击元素创建字段规则和 selector 候选 |
| M4 | 抽取预览与字段校验 | 支持规则预览抽取、候选切换和校验反馈 |
| M5 | 规则版本化与发布 | 建立草稿、发布、历史版本与任务绑定关系 |
| M6 | 映射到 `article` 表 | 支持字段映射、入库和基础去重 |
| M7 | Quartz 定时抓取、运行日志与页面快照 | 完成调度闭环和运行审计 |
| M8 | MVP 收口与可用性补强 | 打通演示链路并补齐错误处理和最小文档 |

## 7. 里程碑详情

### M1 基础骨架与可运行环境

状态：`done`

目标：

- 初始化 Spring Boot + Thymeleaf + MyBatis + PostgreSQL + Quartz 工程
- 配置 Playwright for Java 基础能力
- 建立 migration 体系
- 提供最小后台首页和健康检查能力

范围：

- 工程骨架
- 基础配置文件
- 数据源与 MyBatis 基础集成
- Quartz 基础配置
- Playwright 基础依赖与演示能力

验收标准：

- 项目在 Java 21 下可编译通过
- 本地可启动 Spring Boot 应用
- PostgreSQL migration 可执行成功
- Playwright 能打开页面并返回标题
- Quartz 可注册一个示例 Job 并成功触发一次

手工验证路径：

1. 启动应用并访问后台首页
2. 执行一次“打开 URL 并读取标题”的演示流程
3. 查看数据库确认 migration 已执行
4. 查看日志确认 Quartz 示例任务执行成功

完成后更新：

- 实际落地的项目结构
- 已引入依赖与版本
- migration 列表
- 编译与启动结果

本次实现结果：

- 新建 Maven 单体工程骨架，基于 Spring Boot `3.4.5`，编译目标锁定为 Java 21
- 接入 Thymeleaf 服务端页面，提供后台首页 `/admin`
- 接入 MyBatis，并增加 `DatabaseProbeMapper` 作为最小数据库连通探针
- 接入 Flyway，并新增 migration `V1__bootstrap_marker.sql`
- 接入 Quartz 内存调度，并在应用启动后注册一次示例 Job
- 接入 Playwright for Java，提供页面标题探测服务和后台演示表单
- 补充 `docker-compose.yml`，提供本地 PostgreSQL 17 启动方式
- 补充 `application.yml`、`application-test.yml`、`.gitignore` 和基础测试类

实际落地的项目结构：

- `src/main/java/com/visualspider/VisualSpiderApplication.java`
- `src/main/java/com/visualspider/admin/*`
- `src/main/java/com/visualspider/health/*`
- `src/main/java/com/visualspider/persistence/*`
- `src/main/java/com/visualspider/runtime/*`
- `src/main/java/com/visualspider/scheduler/*`
- `src/main/resources/templates/admin/index.html`
- `src/main/resources/db/migration/V1__bootstrap_marker.sql`

已引入依赖与版本：

- Spring Boot `3.4.5`
- MyBatis Spring Boot Starter `3.0.4`
- Playwright `1.53.0`
- Jsoup `1.18.3`
- Flyway `10.20.1`
- PostgreSQL JDBC `42.7.5`

migration 列表：

- `V1__bootstrap_marker.sql`

验收记录：

- `mvn test` 通过，Spring Boot 测试上下文、Flyway migration、Quartz 初始化均成功
- `mvn -q -DskipTests compile` 通过
- 使用 `docker compose up -d postgres` 启动 PostgreSQL 17 容器成功
- 使用默认 profile 启动应用后，`/healthz` 返回 `200`，数据库状态为 `UP`
- 访问 `/admin` 返回 `200`
- 通过 `/admin/playwright-demo` 探测 `https://www.sina.com.cn` 成功，页面成功返回标题
- 应用日志确认 Quartz 示例任务执行成功：`Bootstrap Quartz job executed successfully.`

实际手工验证路径：

1. 执行 `docker compose up -d postgres`
2. 执行 `mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"` 安装 Playwright 浏览器
3. 执行 `mvn spring-boot:run`
4. 访问 `http://localhost:8080/healthz`，确认返回 `status=UP`
5. 访问 `http://localhost:8080/admin`
6. 在后台页输入 `https://www.sina.com.cn`，确认能返回页面标题
7. 查看启动日志，确认出现 `Bootstrap Quartz job executed successfully.`

编译与启动结果：

- 编译结果：通过
- 测试结果：通过
- 默认 profile + PostgreSQL 容器启动结果：通过

偏差说明：

- 本机实际安装的是 JDK 24，但 Maven 使用 `--release 21` 编译，产物仍按 Java 21 目标生成

### M2 URL 输入与页面加载预览

状态：`done`

目标：

- 用户输入 URL 后，由服务端驱动 Playwright 打开真实页面并生成预览

范围：

- URL 输入页
- 页面加载服务
- 页面基础元信息采集
- 一次调试会话记录
- 静态截图输出

验收标准：

- 输入 URL 后可成功打开真实页面
- 能看到页面标题、最终 URL、截图
- 加载失败时有明确错误信息
- 里程碑结束时项目可编译通过

手工验证路径：

1. 输入公开文章页 URL
2. 提交后查看标题、截图、最终 URL
3. 输入无效 URL，确认失败提示清晰

完成后更新：

- 支持的 URL 输入约束
- 页面加载失败场景说明
- 预览页截图或界面说明

本次实现结果：

- 复用 M1 的 `/admin` 页面，扩展为 M2 的 URL 输入与页面加载预览入口
- 新增 `page_preview_session` 表，并通过 MyBatis 持久化每次页面预览会话
- `PlaywrightService` 扩展为支持真实页面打开、标题采集、最终 URL 采集、耗时统计和全页截图保存
- 新增 `PagePreviewSessionService`，负责把 Playwright 结果写入数据库并组装页面展示模型
- 新增截图访问入口 `/admin/preview-sessions/{id}/screenshot`
- 预览成功时在页面展示会话 ID、请求 URL、最终 URL、标题、耗时、状态和截图
- 预览失败时会写入失败会话，并返回可读错误信息

支持的 URL 输入约束：

- 当前仅做非空校验
- 默认按公开可访问、无需登录的页面处理
- 非法 URL、网络不可达、浏览器未安装等情况由 Playwright 异常统一返回错误提示

页面加载失败场景说明：

- 目标地址不可访问
- URL 格式不被浏览器接受
- Playwright 浏览器未安装
- 页面在超时时间内未完成导航

预览页截图或界面说明：

- 预览结果显示在 `/admin` 同页结果卡片中
- 截图文件默认保存到 `snapshots/page-preview/`
- 页面内可直接显示截图，并可通过 `/admin/preview-sessions/{id}/screenshot` 打开原图

验收记录：

- `mvn test` 通过，新增 `AdminControllerWebMvcTest` 后总计 5 个测试通过
- `mvn -q -DskipTests compile` 通过
- 使用 `docker compose up -d postgres` 启动 PostgreSQL 后，默认 profile 启动应用成功
- `http://localhost:8080/admin` 访问返回 `200`
- 提交 `https://www.sina.com.cn` 后，页面成功展示页面标题、最终 URL 和耗时
- 截图文件已生成到 `snapshots/page-preview/`
- `http://localhost:8080/admin/preview-sessions/1/screenshot` 访问返回 `200`
- 数据库查询确认 `page_preview_session` 已插入成功记录

实际手工验证路径：

1. 执行 `docker compose up -d postgres`
2. 确认已安装 Playwright Chromium 浏览器
3. 执行 `mvn spring-boot:run`
4. 访问 `http://localhost:8080/admin`
5. 输入 `https://www.sina.com.cn` 并提交
6. 确认页面出现会话 ID、页面标题、最终 URL、加载耗时和截图
7. 点击截图原图链接，确认 `/admin/preview-sessions/{id}/screenshot` 返回 `200`
8. 在 PostgreSQL 中执行 `select id, requested_url, final_url, page_title, status from page_preview_session order by id desc limit 3;`，确认记录已落库

偏差说明：

- 当前 URL 输入仅做非空校验，尚未加入更严格的格式校验；M2 范围内通过 Playwright 导航异常兜底

### M3 可视化选区与字段规则创建

状态：`done`

目标：

- 在不使用 iframe 的前提下，完成点击元素创建字段规则的 MVP 闭环

实现方向：

- 后端通过 Playwright 打开页面并注入轻量脚本
- 使用独立预览页展示页面截图或静态镜像以及 DOM 元数据映射
- 点击元素后回传元素路径、文本、属性和上下文特征
- 为字段生成多种 selector 候选，而不是只存单个 XPath

范围：

- 元素拾取会话
- 点击元素创建字段
- selector 候选生成
- 字段命名
- 字段基础类型选择
- 规则草稿保存

验收标准：

- 用户可从预览中点击元素并创建字段
- 每个字段至少生成 2 种 selector 候选
- 字段配置可保存为规则草稿版本
- 里程碑结束时项目可编译通过

手工验证路径：

1. 打开一个文章详情页
2. 依次点击标题、发布时间、正文区域
3. 保存后在规则页看到字段和 selector 候选

完成后更新：

- 实际采用的选区交互方案
- selector 候选生成策略摘要
- 字段配置结构说明

本次实现结果：

- 新增规则草稿相关表结构：`crawl_rule`、`crawl_rule_version`、`crawl_rule_field`、`crawl_selector_candidate`
- 通过 `RuleDraftController` 和 `RuleDraftService` 打通“从预览会话进入规则草稿页 -> 选择元素 -> 创建字段 -> 保存 selector 候选”的最小闭环
- 复用 M2 的截图能力，在规则草稿页展示页面截图，并叠加可点击选区按钮
- 同时提供右侧候选元素列表，便于在截图覆盖层之外完成选择，降低交互复杂度
- 每次创建字段时都会生成多种 selector 候选并落库，当前最小候选类型包括：`css`、`dom_path`、`text`、`css_class`、`attribute`
- 已保存字段会在规则草稿页下方展示，并显示对应 selector 候选摘要

实际采用的选区交互方案：

- 不使用 iframe
- 基于 M2 的页面截图做覆盖层点击
- 后端使用 Playwright 重新分析页面元素，生成可选元素的文本、DOM 路径、属性和位置信息
- 前端页面采用服务端渲染 + 少量原生 JavaScript，将点击结果写入隐藏表单后提交保存

selector 候选生成策略摘要：

- 优先生成 `css` 候选：当元素存在 id 时使用 `#id`
- 生成 `dom_path` 候选：使用结构化 CSS 路径
- 生成 `text` 候选：保存归一化后的文本锚点
- 当类名存在时生成 `css_class` 候选
- 当存在 `href`、`title`、`datetime` 等属性时生成 `attribute` 候选
- 若前述候选不足 2 个，则补一个 `tag` 候选，保证每个字段至少有 2 个候选
- 当前实现明确不生成 XPath 候选，符合“不要依赖单个 XPath”的约束

字段配置结构说明：

- 规则主对象：`crawl_rule`
- 草稿版本：`crawl_rule_version`，当前 M3 仅保存 `DRAFT`
- 字段对象：`crawl_rule_field`
- selector 候选对象：`crawl_selector_candidate`

验收记录：

- `mvn test` 通过，当前总计 9 个自动化测试通过
- `mvn -q -DskipTests compile` 通过
- 新增 `RuleDraftControllerWebMvcTest`，覆盖规则草稿页加载和字段保存跳转
- 新增 `SelectorCandidateGeneratorTest`，覆盖 selector 候选生成规则
- 使用 PostgreSQL + 默认 profile 启动应用后，`/admin/rules/drafts/new` 可基于预览会话正常打开
- 通过真实页面预览会话创建字段 `mobileClient` 成功，数据库中该字段已生成 4 个 selector 候选

实际手工验证路径：

1. 执行 `docker compose up -d postgres`
2. 确认已安装 Playwright Chromium 浏览器
3. 执行 `mvn spring-boot:run`
4. 访问 `http://localhost:8080/admin`，输入 `https://www.sina.com.cn` 并生成预览
5. 点击“进入可视化选区”进入规则草稿页
6. 在截图覆盖层或右侧候选列表中选择一个元素
7. 输入规则名称和字段名称，提交“保存到规则草稿”
8. 页面返回后确认“已保存字段”区域出现新字段及 selector 候选摘要
9. 在 PostgreSQL 中执行以下 SQL，确认字段至少生成 2 个候选：
   `select r.id as rule_id, f.field_name, f.field_type, count(c.id) as selector_count from crawl_rule r join crawl_rule_version v on v.rule_id = r.id join crawl_rule_field f on f.rule_version_id = v.id left join crawl_selector_candidate c on c.field_id = f.id group by r.id, f.field_name, f.field_type order by r.id desc limit 5;`

偏差说明：

- M3 采用“截图覆盖层 + 候选元素列表联动”的保守交互方案，而不是完整的动态 DOM 镜像编辑器；这样可以在最小改动下满足点击元素创建字段的 MVP 目标

### M4 抽取预览与字段校验

状态：`done`

目标：

- 基于规则草稿执行抽取预览并给出字段校验反馈

范围：

- Playwright/Jsoup 抽取执行
- 原始命中节点与抽取值展示
- selector 候选切换
- 基础字段校验
- 预览记录持久化

验收标准：

- 对同一 URL 可查看字段抽取预览结果
- 校验失败字段有明确原因
- 用户可切换候选 selector 并重新预览
- 抽取预览结果可持久化
- 里程碑结束时项目可编译通过

手工验证路径：

1. 对已保存规则执行预览抽取
2. 故意选择错误候选，观察校验失败提示
3. 切换正确候选后重新预览成功

完成后更新：

- 抽取链路说明
- 校验规则清单
- 预览结果存储方式

本次实现结果：

- 新增 `rule_preview_run` 与 `rule_preview_field_result` 两张最小预览记录表，并补充 migration `V4__create_rule_preview_tables.sql`
- 新增 `RulePreviewService`，可基于规则草稿字段和 selector 候选执行一次预览抽取
- 新增 `FieldValidationService`，按 MVP 范围支持必填、长度、URL 格式、时间格式和去空白校验
- 新增 `RulePreviewController` 和 [rule-preview.html](/D:/opencodeSpace/visual_spider2/src/main/resources/templates/admin/rule-preview.html)，用于展示字段抽取值、当前候选、校验状态和失败原因
- 在规则草稿页增加“执行抽取预览”入口，形成 M3 -> M4 的直接流转
- 支持通过查询参数切换某个字段的 selector 候选并重新预览

抽取链路说明：

- 从 `crawl_rule` 找到当前草稿版本 `crawl_rule_version`
- 读取该版本下的 `crawl_rule_field` 与 `crawl_selector_candidate`
- 按字段逐个执行候选抽取
- 抽取结果进入 `FieldValidationService` 做基础校验
- 结果同时写入 `rule_preview_run` 与 `rule_preview_field_result`

校验规则清单：

- 空值校验：字段值为空时直接失败
- 长度校验：超过 2000 字符判为失败
- URL 校验：仅接受 `http://` 或 `https://` 开头
- DATETIME 校验：支持 `ISO_DATE_TIME` 和 `yyyy-MM-dd HH:mm:ss`
- 去空白：抽取结果在校验前统一 `trim`

预览结果存储方式：

- `rule_preview_run` 保存一次预览执行主记录
- `rule_preview_field_result` 保存每个字段的命中值、命中候选、状态和校验信息

验收记录：

- `mvn test` 通过，当前总计 13 个自动化测试通过
- `mvn -q -DskipTests compile` 通过
- 新增 `RulePreviewControllerWebMvcTest` 覆盖预览页访问
- 新增 `FieldValidationServiceTest` 覆盖 URL 和 DATETIME 的基础校验逻辑
- 手工验证中，`candidateField_4=16`（`attribute` 候选）时返回 `VALID / 校验通过`
- 手工验证中，`candidateField_4=14`（`text` 候选）时返回 `INVALID / URL 格式不正确`
- 数据库中 `rule_preview_field_result` 已成功记录 `VALID` 与 `INVALID` 两种结果

实际手工验证路径：

1. 执行 `docker compose up -d postgres`
2. 确认已安装 Playwright Chromium 浏览器
3. 执行 `mvn spring-boot:run`
4. 在 `/admin` 中打开 `https://www.sina.com.cn` 预览
5. 在 M3 规则草稿页创建一个 `URL` 类型字段，例如 `mobileClient`
6. 访问 `/admin/rules/previews?previewSessionId=<id>&ruleId=<id>&candidateField_<fieldId>=<attributeCandidateId>`，确认页面出现“校验通过”，并展示有效 URL
7. 再访问 `/admin/rules/previews?previewSessionId=<id>&ruleId=<id>&candidateField_<fieldId>=<textCandidateId>`，确认页面出现“URL 格式不正确”
8. 在 PostgreSQL 中执行 `select field_id, status, validation_message from rule_preview_field_result order by id desc limit 4;`，确认已落库

偏差说明：

- 当前默认候选预览策略对 `URL` 字段仍偏保守，手工切换候选后的结果链路已可用；后续若需要可在 M4.5 或 M5 前再优化默认候选优先级

### M5 规则版本化与发布

状态：`done`

目标：

- 把规则从草稿编辑升级为版本化、可发布、可回滚

范围：

- 规则主表与规则版本表
- 草稿保存
- 发布流程
- 历史版本查看
- 重新发布历史版本
- 发布前基础校验

验收标准：

- 同一规则可保存多个版本
- 系统能区分草稿和已发布版本
- 历史版本可查看并重新发布
- 调度执行只能使用已发布版本
- 里程碑结束时项目可编译通过

手工验证路径：

1. 新建规则并保存为 v1
2. 修改字段后保存 v2 草稿
3. 发布 v2，确认任务引用切换
4. 查看历史页确认可见 v1/v2 差异摘要

完成后更新：

- 版本模型说明
- 发布状态流转说明
- 与抓取任务的绑定关系说明

本次实现结果：

- 在 `crawl_rule_version` 上补充 `published_at` 字段，并新增 migration `V5__extend_rule_version_publish.sql`
- 新增 `RuleVersionService`，负责版本发布、历史版本查询、从历史版本创建新草稿和重新发布
- 新增 `RuleVersionController` 与 [rule-versions.html](/D:/opencodeSpace/visual_spider2/src/main/resources/templates/admin/rule-versions.html)，提供历史版本页与发布入口
- 在规则草稿页增加“查看版本历史”入口，形成 M3/M4 -> M5 的直接流转
- 发布逻辑已保证同一规则任意时刻只有一个 `PUBLISHED` 版本
- 创建新草稿版本时会复制字段和 selector 候选，避免破坏历史版本

版本模型说明：

- `crawl_rule`：规则主对象
- `crawl_rule_version`：规则版本对象
- `DRAFT`：当前可编辑草稿版本
- `PUBLISHED`：当前已发布版本
- `ARCHIVED`：被新发布版本替换后的历史版本状态

发布状态流转说明：

- 初始草稿：`DRAFT`
- 发布草稿：`DRAFT -> PUBLISHED`
- 发布新版本时，旧的 `PUBLISHED` 自动变为 `ARCHIVED`
- 重新发布历史版本时，当前 `PUBLISHED` 变为 `ARCHIVED`，目标历史版本变为 `PUBLISHED`
- 从任意历史版本可复制出一个新的 `DRAFT` 版本

与抓取任务的绑定关系说明：

- 当前 M5 只完成“一个规则只有一个已发布版本”的数据语义约束
- 调度任务的实际绑定和执行限制会在 M7 正式接入
- 但从数据模型上，后续任务只需要引用当前 `PUBLISHED` 的版本即可

验收记录：

- `mvn test` 通过，当前总计 17 个自动化测试通过
- `mvn -q -DskipTests compile` 通过
- 新增 `RuleVersionControllerWebMvcTest`，覆盖版本历史页和发布入口
- 新增 `RuleVersionServiceTest`，覆盖发布和“只能发布候选充足版本”的约束
- 手工验证中，规则 `m5-sina-rule` 的 v1 发布成功
- 手工验证中，基于 v1 成功创建 v2 草稿
- 手工验证中，先发布 v2，再重新发布 v1，数据库状态成功在 `PUBLISHED` 与 `ARCHIVED` 之间切换

实际手工验证路径：

1. 执行 `docker compose up -d postgres`
2. 执行 `mvn spring-boot:run`
3. 在 `/admin` 中生成一个预览会话
4. 在规则草稿页创建至少一个字段，确保每个字段拥有不少于 2 个 selector 候选
5. 访问 `/admin/rules/{ruleId}/versions` 查看版本历史页
6. 对当前草稿版本执行发布，确认其状态变为 `PUBLISHED`
7. 对该已发布版本执行“创建新草稿”，确认生成新的 `DRAFT` 版本
8. 对新的草稿版本执行发布，确认旧 `PUBLISHED` 自动变为 `ARCHIVED`
9. 再对旧版本执行重新发布，确认状态切换为：
   - 旧版本：`PUBLISHED`
   - 新版本：`ARCHIVED`
10. 在 PostgreSQL 中执行：
    `select version_no, status from crawl_rule_version where rule_id = <ruleId> order by version_no;`
    确认同一时间只有一个 `PUBLISHED`

偏差说明：

- 当前“创建新草稿版本”采用完整复制字段与 selector 候选的保守方案，而不是做增量差异编辑；这样能在最小改动下满足版本化与回滚需求

### M6 映射到 `article` 表

状态：`done`

目标：

- 将抽取字段映射到目标业务表并完成入库

范围：

- `article` 表 migration
- 字段到 `article` 列的映射 UI
- 入库前标准化
- 基础去重策略
- 写入结果反馈

验收标准：

- 用户可将规则字段映射到 `article` 表字段
- 执行一次抓取后抽取结果可成功写入 `article`
- 重复抓取同一文章不会无限重复插入
- 入库失败时有明确错误信息和日志
- 里程碑结束时项目可编译通过

手工验证路径：

1. 配置 `title`、`content`、`published_at`、`source_url` 映射
2. 执行抓取后在数据库中查询到文章记录
3. 再执行一次，确认触发去重或更新逻辑

完成后更新：

- `article` 表结构
- 字段映射规则
- 去重策略说明

本次实现结果：

- 新增 `article` 表和 `rule_article_mapping` 表，并补充 migration `V6__create_article_and_mapping.sql`
- 新增 `ArticleMappingService`，负责读取已发布规则版本并保存字段到 `article` 列的映射关系
- 新增 `ArticleIngestionService`，负责基于已发布版本和映射关系执行一次性抽取入库
- 新增 `ArticleMappingController` 和 [article-mapping.html](/D:/opencodeSpace/visual_spider2/src/main/resources/templates/admin/article-mapping.html)，支持保存映射和执行一次入库
- 在版本历史页增加 “配置 article 映射” 入口，形成 M5 -> M6 的直接流转
- 已实现最小 `insert/update` 语义：按 `source_url` 去重，首次插入，后续重复执行走更新而不是重复插入

`article` 表结构：

- `id`
- `source_url`
- `title`
- `author`
- `published_at`
- `summary`
- `content`
- `cover_image`
- `created_at`
- `updated_at`

字段映射规则：

- 当前映射关系绑定到“已发布版本”上，而不是草稿版本
- 每个 `article` 列最多映射一个规则字段
- 当前支持的映射目标列：
  - `source_url`
  - `title`
  - `author`
  - `published_at`
  - `summary`
  - `content`
  - `cover_image`

去重策略说明：

- 当前按 `source_url` 唯一键去重
- 如果 `source_url` 已存在，则执行更新
- 如果 `source_url` 不存在，则执行插入
- 若没有配置或没有成功抽取 `source_url`，会直接报错并阻止入库

验收记录：

- `mvn test` 通过，当前总计 20 个自动化测试通过
- `mvn -q -DskipTests compile` 通过
- 新增 `ArticleMappingControllerWebMvcTest`，覆盖映射页渲染和保存映射
- 新增 `ArticleIngestionServiceTest`，覆盖首次插入 article 的服务逻辑
- 手工验证中，已发布规则版本的 article 映射保存成功
- 手工验证中，第一次执行入库后 `article` 表新增 1 条记录
- 手工验证中，再执行一次入库后 `article` 总记录数仍为 1，符合最小去重要求

实际手工验证路径：

1. 执行 `docker compose up -d postgres`
2. 执行 `mvn spring-boot:run`
3. 创建一个规则并至少发布一个版本
4. 访问 `/admin/rules/{ruleId}/article-mappings`
5. 配置至少两个映射：
   - `source_url`
   - `title`
6. 点击“保存字段映射”
7. 点击“执行一次入库”
8. 在 PostgreSQL 中执行：
   `select id, source_url, title from article order by id desc limit 5;`
   确认已插入 article 记录
9. 再点击一次“执行一次入库”
10. 在 PostgreSQL 中执行：
    `select count(*) as article_count from article;`
    确认记录数未继续增长

偏差说明：

- 当前 M6 采用最保守的 `source_url` 去重策略，并且只允许基于“已发布版本”执行正式入库；更复杂的唯一键配置和批量入库策略留到后续阶段

### M7 Quartz 定时抓取、运行日志与页面快照

状态：`planned`

目标：

- 完成调度闭环，并保存可审计的运行记录与页面快照

范围：

- 抓取任务管理页
- Cron 配置与启停
- Quartz Job 抓取执行
- 运行日志表
- 快照索引表
- HTML、截图、抽取 JSON 保存

验收标准：

- 用户可创建、启用、暂停定时任务
- Quartz 到点后能自动执行抓取
- 每次运行都有状态、耗时、错误信息、规则版本号
- 每次运行都能关联页面快照和抽取结果快照
- 里程碑结束时项目可编译通过

手工验证路径：

1. 创建一个每分钟执行一次的测试任务
2. 等待一次触发后查看运行记录
3. 打开该次运行详情，确认能看到日志和快照索引

完成后更新：

- 任务模型说明
- Quartz 配置与限制
- 快照目录结构和清理策略

### M8 MVP 收口与可用性补强

状态：`planned`

目标：

- 让 MVP 达到可演示、可排错、可继续迭代的状态

范围：

- 规则列表
- 任务列表
- 运行详情页
- 错误处理与空状态
- 最小权限控制或单用户后台入口
- 基础集成测试与冒烟验证文档
- 示例数据与演示脚本

验收标准：

- 从 URL 输入到定时抓取入库可完整走通
- 关键失败场景有可读提示
- 至少一条端到端演示链路稳定可复现
- 项目在干净环境下可编译通过
- 所有数据库变更均包含 migration SQL

手工验证路径：

1. 按演示脚本完整执行一遍
2. 依次完成 URL 输入、建规则、预览、发布、映射、调度、查看日志与快照

完成后更新：

- 演示脚本
- 已知限制
- 下一阶段建议

## 8. 建议的数据库对象最小集合

- `crawl_rule`
- `crawl_rule_version`
- `crawl_field`
- `crawl_task`
- `crawl_task_binding`
- `crawl_run_log`
- `crawl_snapshot`
- `article`

说明：

- 实际建表时可以按落地需要拆分辅助表
- 所有数据库变更都必须附带 migration SQL

## 9. 建议的实施顺序

1. 先完成 M1-M2，确保“真实页面打开”稳定
2. 再完成 M3-M5，优先打通规则系统闭环
3. 最后完成 M6-M7，接上入库和调度
4. M8 只做收口，不做大重构

## 10. 文档维护约定

后续每次完成实际工作后，除了代码和 migration，还要同步更新本文档。更新要求如下：

- 将相关里程碑的 `状态` 从 `planned` 更新为 `in_progress` 或 `done`
- 在对应里程碑下追加“本次实现结果”
- 记录新增页面、接口、表结构或关键类
- 记录是否满足验收标准
- 补充实际执行过的手工验证路径
- 若实现偏离本文档默认方案，必须补充“偏差说明”

建议追加格式：

```markdown
本次实现结果：
- ...

验收记录：
- ...

偏差说明：
- 无
```

## 11. 当前执行结论

- 本文档已作为后续开发基线
- 当前尚未开始编码实现
- 下一步默认从 M1 开始

## 12. 当前缺陷修复计划

以下问题来自最新一轮手工测试，属于当前已实现链路中的修复项。后续修复工作默认按本节执行，并在完成后补充“修复结果”和“验证记录”。

### 12.1 问题清单

1. 可视化选区粒度过大，候选元素过度停留在 `div` 层级
2. 提交字段到 `/admin/rules/drafts/fields` 时出现 `500`
3. 同一规则草稿页下单次只能稳定保存一个字段，无法连续保存多个字段

### 12.2 修复目标

- 让可视化选区优先落到更细粒度的可用节点，如 `a`、`span`、`time`、`p`、`h1-h4`
- 字段保存失败时不再出现 `500`，而是正常保存或返回可读错误
- 在同一规则草稿版本下可连续保存多个字段，不需要每次重新开始

### 12.3 修复范围

本轮修复只允许修改以下范围：

- `PlaywrightService`
- `RuleDraftService`
- `RuleDraftController`
- `rule-draft.html`
- 对应自动化测试

不扩展到以下范围：

- `article` 映射逻辑
- Quartz 调度
- 运行日志与快照归档
- 新的前端框架或复杂前端重构

### 12.4 修复方案

#### A. 选区粒度修复

- 收紧 `inspectSelectableElements` 的筛选规则
- 对文本过长、面积过大的容器型元素做过滤或降权
- 优先保留更小的叶子节点和可点击节点
- 降低大块 `div`、`section`、`article` 进入候选区的概率

预期结果：

- 候选列表应明显更多地呈现 `a`、`span`、`time`、`p`、标题标签等细粒度元素
- 不再被页面整块新闻区域的 `div` 主导

#### B. 字段保存 500 修复

- 排查 `/admin/rules/drafts/fields` 的真实异常点
- 对规则上下文、草稿版本、隐藏字段和表单必填项补充保护
- 在控制器和服务层增加更明确的错误兜底
- 页面异常时返回可读提示，而不是白页 `500`

预期结果：

- 字段保存成功时正常回到规则草稿页
- 异常场景可见明确错误信息

#### C. 连续保存多字段修复

- 确保同一规则始终落到同一个草稿版本上下文
- 保存后保留当前 `ruleId` / 草稿版本信息
- 页面返回后继续允许选择下一个元素并保存
- 已保存字段列表应累计展示，而不是被覆盖

预期结果：

- 同一规则下可连续保存 `title`、`published_at`、`content` 等多个字段
- 页面下方字段列表应持续累计

### 12.5 自动化测试要求

本轮修复完成后，至少补充并通过以下自动化测试：

- 选区粒度测试：验证候选结果不会被大块 `div` 完全主导
- 字段保存控制器测试：验证提交字段不返回 `500`
- 同一草稿连续保存多个字段测试：验证字段数可累计增长

最终要求：

- `mvn test` 通过
- `mvn -q -DskipTests compile` 通过

### 12.6 手工验证路径

1. 进入规则草稿页
2. 确认候选元素列表以细粒度节点为主，而不是整块容器
3. 选择第一个元素并保存字段
4. 再选择第二个、第三个元素并连续保存
5. 确认页面不出现 `500`
6. 确认已保存字段列表累计展示多个字段

### 12.7 完成后回写要求

修复完成后，需要在本节下追加：

- 修复结果
- 自动化测试结果
- 实际手工验证记录
- 若实现与本修复计划不一致，需要补充偏差说明

### 12.8 修复结果

- 已收紧 `PlaywrightService.inspectSelectableElements` 的候选筛选规则：
  - 过滤过长文本节点
  - 过滤面积过大的容器节点
  - 对大块 `div`/`section`/`article` 做明显降权或过滤
  - 优先保留更细粒度的 `a`、`span`、`time`、`p`、标题等元素
- 已修复 `/admin/rules/drafts/fields` 的异常处理：
  - 规则草稿保存逻辑增加选区长度保护
  - 对超大选区返回“选区过大，请选择更细粒度的元素”
  - 控制器对业务异常改为页面内可读错误，不再直接抛出 `500`
- 已修复“同一规则草稿下连续保存多个字段”的上下文保持：
  - 当 `ruleId` 已存在时，不再强制要求重复填写规则名称
  - 同一规则会复用当前 `DRAFT` 版本继续写入字段
  - 页面返回后可继续选择并累计保存多个字段

### 12.9 自动化测试结果

- `mvn test` 通过
- `mvn -q -DskipTests compile` 通过
- 新增 `RuleDraftServiceTest`，覆盖：
  - 同一草稿版本连续保存多个字段时复用已有草稿版本
  - 超大选区文本时返回友好错误
- 扩展 `RuleDraftControllerWebMvcTest`，覆盖保存失败时不再返回 `500`

### 12.10 实际验证记录

- 自动化验证已确认：
  - 保存异常从服务层转为页面可读错误
  - 同一草稿版本可连续保存多个字段
- 手工验证建议继续按 12.6 的路径执行一次，以确认页面上的候选粒度已经满足预期

### 12.11 偏差说明

- 本轮优先做了“稳定性 + 可用性”修复，候选粒度的优化采用保守规则收紧方式，而不是重写选区交互模型
