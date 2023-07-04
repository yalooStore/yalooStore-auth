package com.yaloostore.auth.member.service.inter;

import com.yaloostore.auth.member.dto.MemberLoginHistoryResponse;

public interface MemberLoginHistoryService {

    MemberLoginHistoryResponse saveLoginHistory(String loginId);
}
