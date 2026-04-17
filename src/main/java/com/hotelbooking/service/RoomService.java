package com.hotelbooking.service;

import com.hotelbooking.entity.Room;
import com.hotelbooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final String uploadDir = "./uploads/rooms/"; // можно вынести в настройки

    public List<Room> getAllRooms() {
        return roomRepository.findAllByOrderByIdAsc();
    }

    public List<Room> getActiveRooms() {
        return roomRepository.findByIsActiveTrue();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
    }

    @Transactional
    public Room createRoom(String description, BigDecimal price, MultipartFile photo) throws IOException {
        Room room = new Room();
        room.setDescription(description);
        room.setPrice(price);
        room.setActive(true);

        // Сохранение фото
        if (photo != null && !photo.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(photo.getInputStream(), filePath);
            room.setPhotoPath("/uploads/rooms/" + fileName);
        } else {
            room.setPhotoPath("/images/default-room.jpg"); // заглушка
        }

        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long id, String description, BigDecimal price, Boolean isActive, MultipartFile photo) throws IOException {
        Room room = getRoomById(id);
        room.setDescription(description);
        room.setPrice(price);
        if (isActive != null) {
            room.setActive(isActive);
        }
        if (photo != null && !photo.isEmpty()) {
            // Удалить старый файл, если не дефолтный
            String oldPhotoPath = room.getPhotoPath();
            if (oldPhotoPath != null && oldPhotoPath.startsWith("/uploads/rooms/")) {
                Path oldFilePath = Paths.get("." + oldPhotoPath);
                Files.deleteIfExists(oldFilePath);
            }
            String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(photo.getInputStream(), filePath);
            room.setPhotoPath("/uploads/rooms/" + fileName);
        }
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        // Удаляем фото, если не дефолтное
        String photoPath = room.getPhotoPath();
        if (photoPath != null && photoPath.startsWith("/uploads/rooms/")) {
            try {
                Files.deleteIfExists(Paths.get("." + photoPath));
            } catch (IOException ignored) {}
        }
        roomRepository.delete(room);
    }
}