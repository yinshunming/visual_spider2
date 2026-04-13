# Visual Spider MVP 演示脚本

## 准备

1. 启动 PostgreSQL：
   - `docker compose up -d postgres`
2. 启动应用：
   - `mvn spring-boot:run`
3. 打开后台：
   - `http://localhost:8080/admin`

## 演示链路

1. 输入 `https://www.sina.com.cn`，生成预览
2. 进入可视化选区，创建至少两个字段
3. 执行抽取预览，确认可看到字段值和校验结果
4. 打开版本历史，发布当前草稿版本
5. 进入 article 映射页，配置 `source_url` 和 `title`
6. 执行一次 article 入库
7. 进入任务列表，创建一个 Cron 任务
8. 等待一次 Quartz 触发
9. 打开运行记录和运行详情，确认：
   - 运行状态
   - HTML 快照
   - PNG 快照
   - 抽取结果 JSON

## 收尾

1. 停止应用
2. 停止 PostgreSQL：
   - `docker compose stop postgres`
