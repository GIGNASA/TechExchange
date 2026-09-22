package com.example.techexchange.repository;

import com.example.techexchange.entity.Device;
import com.example.techexchange.entity.ExchangeRequest;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findByRequesterOrOwnerOrderByUpdatedAtDesc(User requester, User owner);

    long countByOwnerOrRequester(User owner, User requester);

    List<ExchangeRequest> findByTargetDeviceAndStatus(Device targetDevice, ExchangeStatus status);
}
