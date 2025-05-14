package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Payment;

public interface IPaymentService {
    Payment update(Payment payment);

    Payment findPaymentByTnxRef(String tnxRef);

    void deleteByTnxRef(String tnxRef);
}
