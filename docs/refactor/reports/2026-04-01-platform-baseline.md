# 重构报告：基座/门禁/配置/测试基建（P0）

- 日期：2026-04-01
- 范围：重构步骤清单 → P0 → 基座/门禁/配置/测试基建

## 1. 本次重构对象（文件/配置）

- Maven 根聚合构建基座：[pom.xml](file:///d:/GitProgram/gulimail/pom.xml)
- gulimail-search 父 POM 统一与测试参数兼容：[gulimail-search/pom.xml](file:///d:/GitProgram/gulimail/gulimail-search/pom.xml)
- 基建验证用例（确保 Unit/IT 生命周期可运行）：[BuildBaselineTest.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineTest.java)、[BuildBaselineIT.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineIT.java)

## 2. 预期结构改进点（达成情况）

- 统一构建生命周期：在 `verify` 阶段统一产出质量与覆盖率报告（JaCoCo/Checkstyle/PMD/SpotBugs）。已完成。
- 统一测试生命周期：区分单元测试（Surefire）与集成测试（Failsafe，`*IT.java`）。已完成（并通过 IT 例子验证）。
- 统一父 POM：消除子模块自行管理插件版本导致的覆盖率注入/参数兼容问题。已完成（gulimail-search）。

## 3. 编码与规范遵循（java-dev 口径）

本次属于“工程基座”改造，涉及 Java 代码的部分仅为测试基建验证用例，遵循：
- 命名：`*Test`、`*IT`（集成测试后缀）清晰区分测试类型
- 异常与日志：本次新增测试不引入任何日志与异常吞没逻辑
- 并发与安全：本次改动不引入并发/安全逻辑；质量扫描作为后续安全与坏味道治理入口

## 4. 必须补充的测试用例（本次落实）

### 4.1 单元测试（Unit）

- 新增 `BuildBaselineTest`：验证 Surefire 生命周期可运行并被覆盖率统计：[BuildBaselineTest.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineTest.java)

### 4.2 集成测试（Integration）

- 新增 `BuildBaselineIT`：验证 Failsafe 生命周期可运行并不阻塞构建：[BuildBaselineIT.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineIT.java)

## 5. 性能基准对比指标（本次口径）

本次为构建/门禁/测试基建，不直接改变运行时性能：
- 性能基准：不适用（N/A）
- 但对“构建时间”有影响：`verify` 将额外执行静态扫描与报告生成（属于预期成本）

## 6. 执行结果（验证证据）

### 6.1 构建与测试

- 根聚合 `mvn verify`：通过（已执行）
- `gulimail-common` `mvn -pl gulimail-common -am verify`：通过（已执行）
- `gulimail-search` `mvn -pl gulimail-search -am verify`：通过（已执行）

### 6.2 覆盖率报告（JaCoCo）

- gulimail-common 报告主页：[index.html](file:///d:/GitProgram/gulimail/gulimail-common/target/site/jacoco/index.html)
- gulimail-search 报告主页：[index.html](file:///d:/GitProgram/gulimail/gulimail-search/target/site/jacoco/index.html)

覆盖率摘要（基于 gulimail-search 的 `jacoco.csv` 汇总，作为“截图替代证据”）：
- 证据文件：[jacoco.csv](file:///d:/GitProgram/gulimail/gulimail-search/target/site/jacoco/jacoco.csv)
- Instruction 覆盖率：约 6.20%（74 / 1194）
- Line 覆盖率：约 9.24%（22 / 238）

### 6.3 静态扫描（Checkstyle/PMD/SpotBugs）

说明：当前阶段以“能产出报告”为目标，暂不阻塞构建，但扫描结果已落地，可用于后续 P0/P1 技术债消减。

- 示例产物（gulimail-search）：
  - Checkstyle：[checkstyle-result.xml](file:///d:/GitProgram/gulimail/gulimail-search/target/checkstyle-result.xml)
  - PMD：[pmd.xml](file:///d:/GitProgram/gulimail/gulimail-search/target/pmd.xml)
  - SpotBugs：[spotbugsXml.xml](file:///d:/GitProgram/gulimail/gulimail-search/target/spotbugsXml.xml)
- 示例产物（gulimail-common）：
  - JaCoCo XML：[jacoco.xml](file:///d:/GitProgram/gulimail/gulimail-common/target/site/jacoco/jacoco.xml)

SpotBugs 高/中风险样例（已在本次 `verify` 输出中出现，后续需纳入治理清单）：
- `DM_DEFAULT_ENCODING`：AI 模块默认编码依赖（高）
- `NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`：潜在空指针（中）
- `EI_EXPOSE_REP/EI_EXPOSE_REP2`：可变对象暴露（中）

## 7. Sonar 扫描结果（当前状态）

- 当前仓库未配置可用的 SonarQube Server/Token，因此本次未能落数（保持“基座可运行优先”）。
- 交付建议：在阶段 0/CI 环境中补齐 SonarQube 服务与凭据后，补充本报告的 Sonar 基线截图/链接与 Quality Gate 结论。

## 8. 变更清单（摘要）

- 根聚合 POM：引入并统一配置 JaCoCo、Checkstyle、PMD、SpotBugs、Surefire/Failsafe，使 `verify` 阶段产出报告：[pom.xml](file:///d:/GitProgram/gulimail/pom.xml)
- gulimail-search：父 POM 统一到根聚合，避免插件版本漂移；兼容测试参数并确保质量/覆盖率报告可用：[gulimail-search/pom.xml](file:///d:/GitProgram/gulimail/gulimail-search/pom.xml)
- gulimail-common：新增 Unit/IT 验证用例，证明测试生命周期可运行并进入覆盖率统计（不含业务逻辑变更）：[BuildBaselineTest.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineTest.java)、[BuildBaselineIT.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineIT.java)

## 9. 风险点与缓解

- 风险：`verify` 额外执行扫描与报告生成，构建耗时会上升。缓解：后续可通过 CI 分层（PR 快速门禁 vs nightly 全量门禁）控制。
- 风险：SpotBugs 结果当前不阻塞构建，可能在未治理前“带病通过”。缓解：阶段 0 先固化基线与趋势；阶段 1 开始逐步收敛到“零高严重”门禁。
- 风险：部分测试仍依赖外部运行态（Nacos/Redis/ES），影响 CI 可重复性。缓解：在“测试基建”阶段引入 Testcontainers 或提供可控替代环境。

## 10. 回滚方案

- 仅涉及构建配置与测试用例：
  - 回滚 Maven 基座：回退根聚合 [pom.xml](file:///d:/GitProgram/gulimail/pom.xml) 中新增的 pluginManagement/plugins 配置
  - 回滚 gulimail-search 父 POM：回退 [gulimail-search/pom.xml](file:///d:/GitProgram/gulimail/gulimail-search/pom.xml) 的 parent 变更
  - 回滚测试用例：删除 [BuildBaselineTest.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineTest.java)、[BuildBaselineIT.java](file:///d:/GitProgram/gulimail/gulimail-common/src/test/java/com/lg/common/BuildBaselineIT.java)

## 11. Code Review Checklist（本次适用）

- [ ] 根聚合 POM 的插件配置不会改变运行时产物（仅影响构建阶段）
- [ ] `verify` 阶段能在本机/CI 上稳定执行（无隐藏的本地路径/硬编码）
- [ ] 单元测试与集成测试生命周期分离（`*IT.java` 仅由 Failsafe 执行）
- [ ] JaCoCo 报告可产出并可追溯（至少对关键模块可打开 HTML/读取 XML）
- [ ] 静态扫描结果可定位（XML/HTML 报告落盘路径明确）
- [ ] 未引入敏感信息到仓库（AK/SK/Token 等）

