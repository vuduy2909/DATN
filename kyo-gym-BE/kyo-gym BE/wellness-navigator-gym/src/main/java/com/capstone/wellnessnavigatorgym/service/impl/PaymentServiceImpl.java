package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Payment;
import com.capstone.wellnessnavigatorgym.repository.IPaymentRepository;
import com.capstone.wellnessnavigatorgym.service.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements IPaymentService {

    @Autowired
    private IPaymentRepository paymentRepository;

    @Override
    public Payment update(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findPaymentByTnxRef(String tnxRef) {
        return paymentRepository.findPaymentByTnxRef(tnxRef).orElse(null);
    }

    @Override
    public void deleteByTnxRef(String tnxRef) {
        Payment payment = paymentRepository.findPaymentByTnxRef(tnxRef).orElse(null);
        if (payment != null) {
            paymentRepository.deleteDetailsByPaymentId(payment.getId());
            paymentRepository.deleteById(payment.getId());
        }
    }
}
