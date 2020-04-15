package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netease.cloud.nsf.service.impl.WhiteListV2ServiceImpl;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author wengyanghui | wengyanghui@corp.netease.com | 2020/03/23
 **/
public class WhiteListV2AuthRuleDto {

    @JsonProperty("RuleName")
    @NotNull(message = "rule name")
    private String ruleName;

    @JsonProperty("MatchType")
    @NotNull(message = "match type")
    private String matchType;

    @JsonProperty("MatchApis")
    @NotNull(message = "match apis")
    private String matchApis;

    @JsonProperty("MatchConditions")
    @NotNull(message = "match conditions")
    private String matchConditions;

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getMatchApis() {
        return matchApis;
    }

    public void setMatchApis(String matchApis) {
        this.matchApis = matchApis;
    }

    public String getMatchConditions() {
        return matchConditions;
    }

    public void setMatchConditions(String matchConditions) {
        this.matchConditions = matchConditions;
    }
}
