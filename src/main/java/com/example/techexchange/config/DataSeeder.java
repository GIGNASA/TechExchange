package com.example.techexchange.config;

import com.example.techexchange.entity.Device;
import com.example.techexchange.entity.User;
import com.example.techexchange.entity.enums.DeviceCategory;
import com.example.techexchange.entity.enums.DeviceCondition;
import com.example.techexchange.entity.enums.UserRole;
import com.example.techexchange.repository.DeviceRepository;
import com.example.techexchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    public static final String DEMO_EMAIL = "dev1@techexchange.local";
    public static final String DEMO_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled || deviceRepository.count() > 0) {
            return;
        }

        User dev1 = userRepository.findByEmail(DEMO_EMAIL)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(DEMO_EMAIL)
                        .fullName("dev1")
                        .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                        .active(true)
                        .build()));

        User dev2 = userRepository.findByEmail("dev2@techexchange.local")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("dev2@techexchange.local")
                        .fullName("dev2")
                        .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                        .active(true)
                        .build()));

        User dev3 = userRepository.findByEmail("dev3@techexchange.local")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("dev3@techexchange.local")
                        .fullName("dev3")
                        .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                        .active(true)
                        .build()));

        userRepository.findByEmail("dev-admin@techexchange.local")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("dev-admin@techexchange.local")
                        .fullName("dev-admin")
                        .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                        .role(UserRole.ADMIN)
                        .active(true)
                        .build()));

        saveDevice(dev1, "MacBook Air M1 13", DeviceCategory.LAPTOP, DeviceCondition.EXCELLENT,
                "Apple", "Air M1 8/256", "Київ", "iPad Pro або компактний ігровий ноутбук",
                "Легкий ноутбук для навчання та роботи. Акумулятор тримає добре, корпус без серйозних слідів.",
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=80");
        saveDevice(dev1, "Sony WH-1000XM4", DeviceCategory.AUDIO, DeviceCondition.GOOD,
                "Sony", "WH-1000XM4", "Львів", "Механічна клавіатура або смартфон з доплатою",
                "Навушники з активним шумозаглушенням. Є невеликі потертості на кейсі.",
                "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=1200&q=80");
        saveDevice(dev2, "iPhone 13 128GB", DeviceCategory.SMARTPHONE, DeviceCondition.EXCELLENT,
                "Apple", "iPhone 13", "Одеса", "Android-флагман або ноутбук для дизайну",
                "Телефон у гарному стані, Face ID працює, комплект з кабелем та чохлом.",
                "https://images.unsplash.com/photo-1632633173522-6496ff620f5f?auto=format&fit=crop&w=1200&q=80");
        saveDevice(dev2, "Canon EOS M50", DeviceCategory.CAMERA, DeviceCondition.GOOD,
                "Canon", "EOS M50", "Київ", "Планшет або портативна консоль",
                "Камера для відео та фото, об'єктив 15-45 мм, зарядний пристрій у комплекті.",
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?auto=format&fit=crop&w=1200&q=80");
        saveDevice(dev3, "Nintendo Switch OLED", DeviceCategory.GAMING, DeviceCondition.EXCELLENT,
                "Nintendo", "Switch OLED", "Харків", "Смартфон або навушники з ANC",
                "Консоль майже без слідів використання. Два джойкони, док-станція, коробка.",
                "https://images.unsplash.com/photo-1612036782180-6f0b6cd846fe?auto=format&fit=crop&w=1200&q=80");
        saveDevice(dev3, "iPad 9th Gen", DeviceCategory.TABLET, DeviceCondition.FAIR,
                "Apple", "iPad 10.2 64GB", "Дніпро", "Смартфон або ноутбук з моєю доплатою",
                "Планшет для навчання, є подряпини на корпусі, екран без тріщин.",
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=1200&q=80");
    }

    private void saveDevice(User owner, String title, DeviceCategory category, DeviceCondition condition,
                            String brand, String model, String city, String desiredExchange,
                            String description, String imageUrl) {
        deviceRepository.save(Device.builder()
                .owner(owner)
                .title(title)
                .category(category)
                .condition(condition)
                .brand(brand)
                .model(model)
                .city(city)
                .desiredExchange(desiredExchange)
                .description(description)
                .imageUrl(imageUrl)
                .active(true)
                .build());
    }
}
