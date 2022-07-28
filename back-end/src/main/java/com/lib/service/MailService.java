package com.lib.service;

import com.lib.common.Result;
import org.springframework.stereotype.Service;

public interface MailService {
    public Result sendMailForChangeCode(String email);
}
