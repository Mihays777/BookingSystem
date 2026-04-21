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

            // Номер 1: Стандарт одноместный
            Room room1 = new Room();
            room1.setRoomNumber("101");
            room1.setRoomType(Room.RoomType.STANDARD);
            room1.setCapacity(1);
            room1.setDescription("Уютный одноместный номер с видом на город. В номере: кровать, телевизор, кондиционер, бесплатный Wi-Fi.");
            room1.setPrice(new BigDecimal("3500.00"));
            room1.setPhotoPath("/uploads/rooms/default-room.jpg");
            room1.setActive(true);
            roomRepository.save(room1);

            // Номер 2: Стандарт двухместный
            Room room2 = new Room();
            room2.setRoomNumber("102");
            room2.setRoomType(Room.RoomType.STANDARD);
            room2.setCapacity(2);
            room2.setDescription("Двухместный номер с двумя односпальными кроватями. Ванная комната с душем, телевизор, бесплатный Wi-Fi.");
            room2.setPrice(new BigDecimal("4500.00"));
            room2.setPhotoPath("/uploads/rooms/default-room.jpg");
            room2.setActive(true);
            roomRepository.save(room2);

            // Номер 3: Сюит
            Room room3 = new Room();
            room3.setRoomNumber("201");
            room3.setRoomType(Room.RoomType.SUITE);
            room3.setCapacity(2);
            room3.setDescription("Просторный сюит с большой двуспальной кроватью и зоной отдыха. Ванная комната с ванной, мини-бар, сейф.");
            room3.setPrice(new BigDecimal("7500.00"));
            room3.setPhotoPath("/uploads/rooms/default-room.jpg");
            room3.setActive(true);
            roomRepository.save(room3);

            // Номер 4: Семейный
            Room room4 = new Room();
            room4.setRoomNumber("301");
            room4.setRoomType(Room.RoomType.FAMILY);
            room4.setCapacity(4);
            room4.setDescription("Семейный двухкомнатный номер: спальня с двуспальной кроватью и детская с двумя односпальными. Игровой уголок, ванна, холодильник.");
            room4.setPrice(new BigDecimal("9500.00"));
            room4.setPhotoPath("/uploads/rooms/default-room.jpg");
            room4.setActive(true);
            roomRepository.save(room4);

            // Номер 5: Люкс
            Room room5 = new Room();
            room5.setRoomNumber("401");
            room5.setRoomType(Room.RoomType.LUXURY);
            room5.setCapacity(2);
            room5.setDescription("Представительский люкс с панорамным видом на парк. Гостиная и спальня, джакузи, кофе-машина, халаты, тапочки.");
            room5.setPrice(new BigDecimal("15000.00"));
            room5.setPhotoPath("/uploads/rooms/default-room.jpg");
            room5.setActive(true);
            roomRepository.save(room5);

            // Номер 6: Люкс
            Room room6 = new Room();
            room6.setRoomNumber("402");
            room6.setRoomType(Room.RoomType.LUXURY);
            room6.setCapacity(3);
            room6.setDescription("Президентский люкс: две спальни, гостиная с камином, кухня, вид на город, обслуживание 24/7.");
            room6.setPrice(new BigDecimal("25000.00"));
            room6.setPhotoPath("/uploads/rooms/default-room.jpg");
            room6.setActive(true);
            roomRepository.save(room6);

            System.out.println("Demo rooms created with default images.");
        }
    }
}