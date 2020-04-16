package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.WhiteList;
import com.netease.cloud.nsf.meta.dto.WhiteListV2AuthRuleDto;
import com.netease.cloud.nsf.service.impl.WhiteListV2ServiceImpl;

import java.util.List;

public interface WhiteListV2Service {

    void updateServiceAuth(String service, Boolean authOn, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList);

    void createOrUpdateAuthRule(String service, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList);

    void deleteAuthRule(String service, String ruleName, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList);
}
