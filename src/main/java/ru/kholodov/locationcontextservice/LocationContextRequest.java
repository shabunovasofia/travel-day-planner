package ru.kholodov.locationcontextservice;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.kholodov.locationcontextservice.enums.Pace;

import java.time.LocalTime;

@Data
public class LocationContextRequest {
    @NotBlank(message = "Адрес не может быть пустым")
    private String location;

    @NotNull(message = "Укажите время начала")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Укажите время окончания")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "Темп обязателен")
    private Pace pace;


}