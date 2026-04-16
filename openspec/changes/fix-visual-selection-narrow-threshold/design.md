## Context

可视化选区功能通过 Playwright 采集页面元素，然后根据一系列筛选条件过滤出可选择的元素。问题是当前阈值设置过于严格，导致很多有效元素被过滤。

**当前筛选逻辑** (`PlaywrightService.java`):
```javascript
.filter(item => item.widthPercent > 0.6 && item.heightPercent > 0.18)
.filter(item => item.area <= 260000)
.filter(item => item.tagName !== 'div' || (item.text.length >= 2 && item.text.length <= 90 && item.childCount <= 5))
```

## Goals / Non-Goals

**Goals:**
- 降低误过滤率，让更多有效元素可被选择
- 保持对噪音元素的过滤能力
- 不破坏现有的排序评分逻辑

**Non-Goals:**
- 不涉及抽取规则生成逻辑的修改
- 不修改前端展示逻辑
- 不做阈值的完全参数化（那是后续工作）

## Decisions

### Decision 1: 调整阈值而非重构架构

**选择**：直接调整 JavaScript 中的硬编码阈值
**理由**：改动最小，风险可控，符合"最小改动"原则

| 阈值 | 原值 | 新值 |
|------|------|------|
| widthPercent > | 0.6 | 0.2 |
| heightPercent > | 0.18 | 0.08 |
| area <= | 260000 | 400000 |
| div text.length <= | 90 | 200 |
| div childCount <= | 5 | 12 |

### Decision 2: 保持 Top 80 限制

**选择**：不改变返回元素上限（仍为 80 个）
**理由**：评分排序逻辑已能保证最相关的元素排在前面

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 噪音元素增加 | 通过评分排序确保相关内容优先；后续可调参优化 |
| 性能轻微下降 | 过滤条件减少约 3-5%，影响可忽略 |

## Open Questions

- 是否需要将阈值参数化到配置文件？（建议后续实现）
- 是否有更多标签需要加入白名单？
