## Why

可视化选区功能中，很多页面元素无法被选中进行规则提取。原因在于 `inspectSelectableElements` 方法的筛选阈值过于严格，导致新闻网站（如新浪首页）的很多有效元素被过滤掉。

## What Changes

- 降低 `widthPercent` 阈值：0.6 → 0.2，让侧边栏元素也能通过
- 降低 `heightPercent` 阈值：0.18 → 0.08，让短标题也能通过
- 放宽 `area` 上限：260000 → 400000，允许更大的新闻块
- 放宽 div 的 `text.length` 限制：90 → 200，允许长新闻标题
- 放宽 div 的 `childCount` 限制：5 → 12，允许更复杂的容器

## Capabilities

### New Capabilities
- `visual-selection-threshold`: 可视化选区元素筛选阈值配置（待参数化）

### Modified Capabilities
- 无（仅为实现细节调整，不涉及需求变更）

## Impact

- 修改文件：`src/main/java/com/visualspider/runtime/PlaywrightService.java`
- 预期影响：候选元素数量增加（新浪首页约 30-50 → 60-80）
- 风险：可能增加噪音元素，可通过后续调参优化
