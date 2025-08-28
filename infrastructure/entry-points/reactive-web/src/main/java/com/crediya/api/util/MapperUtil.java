package com.crediya.api.util;

import com.crediya.api.model.CreateApplicationRequest;
import com.crediya.model.creditapplication.CreditApplication;

public class MapperUtil {
    public static CreditApplication fromRequestToUserDomain(CreateApplicationRequest r) {
        CreditApplication application = new CreditApplication();
        application.setEmail(r.email());
        application.setInstallments(r.installments());
        application.setCreditAmount(r.amount());
        return application;
    }
}
