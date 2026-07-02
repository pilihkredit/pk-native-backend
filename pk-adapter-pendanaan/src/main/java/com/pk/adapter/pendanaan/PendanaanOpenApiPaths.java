package com.pk.adapter.pendanaan;

/**
 * Pendanaan OpenAPI paths (relative to {@code pk_provider.base_url}, e.g. {@code .../ktaid}).
 *
 * @see <a href="https://ek8l1y505u.feishu.cn/wiki/ExC1wwGVWiQ8CqkrRdEcc2ZinKb">开放平台 API 接口文档</a>
 */
final class PendanaanOpenApiPaths {
    static final String PREFIX = "/api/open/v1";

    static final String OAUTH_TOKEN = PREFIX + "/oauth/token";
    static final String BANK_LIST = PREFIX + "/bank/list";
    static final String AREA_LIST = PREFIX + "/area/list";
    static final String USER_INFO_UPSERT = PREFIX + "/user/info/upsert";
    static final String USER_STATUS = PREFIX + "/user/status";
    static final String CREDIT_APPLY = PREFIX + "/credit/apply";
    static final String CREDIT_APPLY_STATUS = PREFIX + "/credit/applyStatus";
    static final String PRODUCT_LIST = PREFIX + "/product/list";
    static final String LOAN_TRIAL = PREFIX + "/loan/trial";
    static final String LOAN_APPLY = PREFIX + "/loan/apply";
    static final String LOAN_APPLY_STATUS = PREFIX + "/loan/applyStatus";
    static final String LOAN_CONTRACT_LIST = PREFIX + "/loan/contract/list";
    static final String REPAY_PLAN = PREFIX + "/repay/plan";
    static final String REPAY_VA_LIST = PREFIX + "/repay/va/list";
    static final String REPAY_VA_DEFAULT = PREFIX + "/repay/va/default";
    static final String REPAY_TRIAL = PREFIX + "/repay/trial";
    static final String REPAY_TRIAL_BATCH = PREFIX + "/repay/trial/batch";
    static final String REPAY_CURRENT_ORDER = PREFIX + "/repay/current/order";

    private PendanaanOpenApiPaths() {
    }
}
