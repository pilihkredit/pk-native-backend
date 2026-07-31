# credit_application 瘦身与授信状态职责外移设计

- 日期：2026-07-20
- 状态：approved
- 方案：方案 1（瘦身主表 + 状态职责外移）

## 1. 背景与目标

`credit_application` 当前同时承担「申请事实」与「授信状态机」（`status` / `external_status` / `next_poll_at` / `freeze_end_at` 等）。授信状态已有资方实时接口（`/v1/credit/applyStatus`）及最新结果表 `credit_lender_status_query`，主表冗余状态与轮询调度增加复杂度且语义重复。

**目标：**

1. `credit_application` 只保留授信申请事实与 `/v1/credit/apply` 审计。
2. `provider_code` 按配置 `pk.lender.config.provider-code` 查 `pk_provider` 真实写入，不再写死常量。
3. `external_credit_apply_no` 重命名为 `apply_no`；删除 `profile_version_id` 及授信路径上的 profile 快照创建。
4. 删除主表状态相关字段及轮询链路；最新状态落 `credit_lender_status_query`（upsert）；状态变更落 `credit_status_history`（仅变化时 insert）。
5. 借款侧（产品 / 试算 / 借款）去掉对授信 `APPROVED` 的判断。

## 2. 非目标

- 不改资方 OpenAPI 协议本身。
- 不改 `credit_lender_status_query` 表结构。
- 不删除授信回调 intake 入口（仍保留，落库目标调整）。
- 不做历史数据清洗以外的业务回填（migrate 仅改列；旧 status 列数据随 drop 丢弃）。
- 不在本轮为借款链路新增替代性的授信门槛。

## 3. 存储模型

### 3.1 `credit_application`（瘦身后）

| 字段 | 说明 |
|------|------|
| `id` | PK |
| `apply_id` | 服务端授信申请号（唯一） |
| `request_id` | 幂等键（唯一） |
| `provider_code` | 配置 `pk.lender.config.provider-code` → 查 `pk_provider` 校验后写入 |
| `profile_id` | 用户 |
| `mobile_no` | 手机号 |
| `apply_no` | 资方授信单号（原 `external_credit_apply_no`）；`/v1/credit/apply` 成功后写入 |
| `last_lender_request_json` | 仅存资方 `/v1/credit/apply` 请求 |
| `last_lender_response_json` | 仅存资方 `/v1/credit/apply` 响应 |
| `created_at` / `updated_at` | 时间戳 |

**删除字段：** `profile_version_id`、`status`、`external_status`、`last_error_code`、`submitted_at`、`finalized_at`、`freeze_end_at`、`next_poll_at`、`version`。

同步删除依赖索引（如 `idx_credit_application_poll`、`idx_credit_application_profile_status`）；`external_credit_apply_no` 索引改为 `apply_no`。

### 3.2 `credit_lender_status_query`

不变：按 `apply_id` upsert，始终保存该笔授信的最新资方状态与额度等字段。

### 3.3 `credit_status_history`

表结构可先不动。写入规则：

- 比较对象：该 `apply_id` / `credit_application_id` 对应的**当前已存**资方状态（以 `credit_lender_status_query.external_status`，或与现有 mapper 一致的映射业务状态）与本次新状态。
- **发生变化** → insert 一行（`from_status` / `to_status` / `external_status` / `source`）。
- **未变化** → 不写 history。
- 授信申请创建阶段不写 INIT/SUBMITTING 类历史（主表已无内部状态机）。

## 4. 主流程

### 4.1 授信申请 `apply`

1. 读取配置 `pk.lender.config.provider-code`，查 `pk_provider`（须存在且可用），得到 `provider_code`。
2. `request_id` 幂等：已存在则返回已有 `apply_id` / `apply_no`。
3. 插入 `credit_application`（无 status / profile_version）。
4. 调用资方 `/v1/credit/apply`：成功后写 `apply_no`，并更新主表 `last_lender_*` 为该次 apply 的请求/响应。
5. 不再写 INIT/SUBMITTING/PROCESSING；申请阶段不写 `credit_status_history`。
6. 对外 API 字段与现有对齐：保留 `status` 响应字段时，申请成功可对外固定为处理中语义，或 defer 到查状态接口（实现时与现有 DTO 兼容，避免无意义地写主表）。

### 4.2 查状态（`getStatus`）及任何「需要最新授信状态」的实时拉取

1. 实时调资方 applyStatus。
2. **始终** upsert `credit_lender_status_query`。
3. 与已存状态比较：变化则写 `credit_status_history`，否则跳过。
4. 对外返回以最新查询表（及必要映射）为准。

### 4.3 资方回调

- 解析后走同一 applier：upsert 最新表；仅状态变化写 history。
- **不**写入 `credit_application.last_lender_*`（该字段仅属于 apply 接口）。

### 4.4 删除的能力

- 授信状态轮询 scheduler / `next_poll_at` / `findDueForPoll` / `scheduleNextPoll` 整条链路。
- 授信路径上的 `profileVersionRepository.createSnapshot`。
- 主表 `updateStatus` / `updateFreezeEndAt` / `markSubmitted` 中与 status/poll 耦合的逻辑（`apply_no` 写入保留为独立更新）。

### 4.5 借款侧

以下位置**删除**对 `credit_application.status == APPROVED` 的判断，本轮不替换为读 `credit_lender_status_query`：

- `LoanProductFacade.requireApprovedCredit`
- `LoanTrialFacade`
- `LoanApplyFacade`

## 5. 代码改动范围

| 区域 | 改动 |
|------|------|
| SQL | `create_pk_schema.sql` + 新 `migrate_credit_application_slim.sql` |
| Credit 仓储/Mapper | Record 瘦身；`apply_no`；去掉 status/poll 相关方法 |
| `CreditApplyFacade` / `CreditApplyHandler` | provider 查表；去 profile_version 与状态流转 |
| `CreditLenderStatusApplier` | 只 upsert + 条件写 history |
| Poll | 删除 scheduler / handler 轮询调度入口 |
| Loan 三处 Facade | 去掉授信 APPROVED 校验 |
| 单测 | 同步更新 |

## 6. 错误处理

- `pk_provider` 查无或不可用：申请失败，返回明确错误（参数/配置类），不落半成品或与现有 ApiCode 对齐。
- 资方 apply 失败：按现有错误传播；审计 JSON 在有请求/响应体时仍尽量落库（与现有 apply handler 行为对齐，实现时以「仅 apply 接口」为准）。
- 资方 applyStatus 失败：查状态接口按现有 upstream 错误处理；不伪造 history。

## 7. 测试要点

- apply 幂等、`provider_code` 来自 `pk_provider`、审计 JSON 仅来自 apply。
- getStatus：实时拉资方 → upsert；状态变才写 history，不变不写。
- 回调：同上；不更新主表 `last_lender_*`。
- 主表无 status / poll 字段更新路径。
- 借款三处无授信状态校验后流程可走通（单测相应调整断言）。

## 8. 开放实现细节（计划阶段钉死）

- History 比较用 `external_status` 原文还是映射后的业务状态：默认以**映射后业务状态**比较，history 同时存 `external_status` 原文（与现表字段一致）。
- 申请 API 响应中的 `status` 字段：无主表状态时对外固定 `PROCESSING`，直至客户端调 getStatus。
