package ru.kholodov.locationcontextservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "Параметры запроса для расчёта контекста прогулки")
public class LocationContextRequest {

  @NotBlank(message = "Адрес не может быть пустым")
  @Schema(description = "Адрес в свободной форме", example = "Арбат, Москва")
  private String location;

  @NotNull(message = "Укажите время начала")
  @JsonFormat(pattern = "HH:mm")
  @Schema(description = "Время начала прогулки (HH:mm)", example = "10:00", type = "string")
  private LocalTime startTime;

  @NotNull(message = "Укажите время окончания")
  @JsonFormat(pattern = "HH:mm")
  @Schema(description = "Время окончания прогулки (HH:mm)", example = "16:00", type = "string")
  private LocalTime endTime;

  @NotNull(message = "Темп обязателен")
  @Schema(
      description = "Темп прогулки",
      example = "MEDIUM",
      allowableValues = {"SLOW", "MEDIUM", "FAST"})
  private Pace pace;

  /**
   * Cross-field валидация: время окончания строго позже времени начала.
   *
   * <p>Возвращает {@code true} при {@code null}-полях, чтобы не дублировать сообщение с уже
   * сработавшим {@code @NotNull}.
   *
   * @return {@code true}, если интервал валидный или одно из полей ещё не заполнено
   */
  @AssertTrue(message = "Время окончания должно быть позже времени начала")
  public boolean isEndTimeAfterStartTime() {
    if (startTime == null || endTime == null) {
      return true;
    }
    return endTime.isAfter(startTime);
  }
}
