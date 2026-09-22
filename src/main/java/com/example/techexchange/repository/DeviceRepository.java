package com.example.techexchange.repository;

import com.example.techexchange.entity.Device;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.DeviceCategory;
import com.example.techexchange.entity.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findAllByOrderByUpdatedAtDesc();

    List<Device> findByOwnerOrderByUpdatedAtDesc(User owner);

    List<Device> findByOwnerNotOrderByUpdatedAtDesc(User owner);

    long countByOwner(User owner);

    long countByCategory(DeviceCategory category);

    long countByStatus(DeviceStatus status);

    long countByActiveTrue();

    long countByCategoryAndActiveTrue(DeviceCategory category);

    long countByStatusAndActiveTrue(DeviceStatus status);
}
