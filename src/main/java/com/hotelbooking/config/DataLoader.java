package com.hotelbooking.config;

import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Создаём администратора, если его нет
        if (userRepository.findByLogin("admin").isEmpty()) {
            User admin = new User();
            admin.setLogin("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user created: admin / admin123");
        }

        // Если номеров нет, создадим несколько демо-номеров
        if (roomRepository.count() == 0) {
            // Убедимся, что папка для загрузок существует
            Path uploadPath = Paths.get("./uploads/rooms");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Копируем дефолтное изображение из resources в uploads (если есть)
            Path defaultImageSource = Paths.get("src/main/resources/static/images/default-room.jpg");
            Path defaultImageDest = uploadPath.resolve("default-room.jpg");
            if (Files.exists(defaultImageSource) && !Files.exists(defaultImageDest)) {
                Files.copy(defaultImageSource, defaultImageDest, StandardCopyOption.REPLACE_EXISTING);
            }

            // Создаём номера с разными описаниями
            Room room1 = new Room();
            room1.setDescription("Уютный одноместный номер с видом на город. В номере: кровать, телевизор, кондиционер, бесплатный Wi-Fi.");
            room1.setPrice(new BigDecimal("3500.00"));
            room1.setPhotoPath("/uploads/rooms/default-room.jpg");
            room1.setActive(true);
            roomRepository.save(room1);

            Room room2 = new Room();
            room2.setDescription("Двухместный номер с большой кроватью. Ванная комната с душем, мини-бар, сейф.");
            room2.setPrice(new BigDecimal("5500.00"));
            room2.setPhotoPath("/uploads/rooms/default-room.jpg");
            room2.setActive(true);
            roomRepository.save(room2);

            Room room3 = new Room();
            room3.setDescription("Люкс с панорамным видом. Двухкомнатный номер, гостиная и спальня, джакузи.");
            room3.setPrice(new BigDecimal("12000.00"));
            room3.setPhotoPath("/uploads/rooms/default-room.jpg");
            room3.setActive(true);
            roomRepository.save(room3);

            System.out.println("Demo rooms created with default images.");
        }
    }
}