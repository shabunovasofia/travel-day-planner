package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Data;
import ru.kholodov.locationcontextservice.enums.Pace;

/**
 * DTO для входящего запроса на расчёт контекста прогулки.
 *
 * <p>Содержит все параметры, необходимые для определения географических и временных рамок
 * прогулочного маршрута.
 */
@Data
public class LocationContextRequest {

  /**
   * Текстовый адрес (например, "Арбат, Москва"), который будет преобразован в координаты. Не может
   * быть пустым или состоять только из пробелов.
   */
  @NotBlank(message = "Адрес не может быть пустым")
  private String location;

  /**
   * Время начала прогулки в формате "HH:mm" (например, "10:00"). Поле обязательно для заполнения.
   */
  @NotNull(message = "Укажите время начала")
  @JsonFormat(pattern = "HH:mm")
  private LocalTime startTime;

  /**
   * Время окончания прогулки в формате "HH:mm" (например, "16:00"). Поле обязательно для
   * заполнения.
   */
  @NotNull(message = "Укажите время окончания")
  @JsonFormat(pattern = "HH:mm")
  private LocalTime endTime;

  /** Темп прогулки: SLOW, MEDIUM или FAST. Влияет на радиус поиска интересных мест. */
  @NotNull(message = "Темп обязателен")
  private Pace pace;
}
