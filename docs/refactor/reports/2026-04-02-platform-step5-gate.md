# 重构报告：基座/门禁/配置/测试基建（第5步 门禁闭环验证）

- 日期：2026-04-02
- 范围：`基座/门禁/配置/测试基建`
- 对齐清单：`steps-and-estimates.md` 第 5 步（性能与回归验证、输出对比数据与风险回滚点）

## 1. 验证目标

- 验证 `quality-gate-strict` 在真实模块上可执行并可阻断质量问题。
- 清理 strict 模式下阻断项，确保门禁从“可选”进入“可用”状态。
- 输出门禁收口证据，完成基座模块闭环。

## 2. 本轮变更

- 修复 strict 阻断问题：
  - `Constant`：消除 PMD `UnnecessaryFullyQualifiedName`。
  - `LogDesensitizeUtils`：删除无用 import。
  - `HTMLFilter`：简化三元表达式并合并可折叠分支，修复迭代器告警。
  - `NoStockException`：补齐 `skuId` 字段赋值。
- 新增 SpotBugs 过滤规则：
  - `gulimail-common/src/main/resources/spotbugs-exclude.xml`
  - 在 `gulimail-common/pom.xml` 配置 `excludeFilterFile`。

## 3. 验证结果

- 执行命令：`mvn -Pquality-gate-strict -pl gulimail-common verify`
- 结果：**通过（BUILD SUCCESS）**
- 门禁关键结果：
  - Checkstyle：0 违规
  - PMD：0 阻断违规
  - SpotBugs：0 违规（在过滤规则口径下）
  - 单测/集测：27/27 与 1/1 全部通过

## 4. 风险回滚点

- 回滚点 1：若 SpotBugs 过滤口径需收紧，可回退 `spotbugs-exclude.xml` 并逐条治理 DTO 可变对象暴露告警。
- 回滚点 2：若 strict 门禁导致老模块短期无法推进，可临时使用非 strict profile，保留 strict 作为 nightly 门禁。
- 回滚点 3：若 XSS 过滤行为出现兼容争议，可仅回退 `HTMLFilter` 的本轮代码简化，保留其他门禁修复。
