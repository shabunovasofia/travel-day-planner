package ru.kholodov.locationcontextservice.dto.planner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ от planner-service.
 *
 * <p>Обёртка вида {@code {"data": {...}}}, где полезная нагрузка лежит в {@link PlanData}. Этот же
 * DTO используется в Swagger-схеме контроллера в качестве описания 200-ответа.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Обёртка ответа planner-service")
public class PlanBuildResponse {

  @Schema(description = "Полезная нагрузка с готовым планом")
  private PlanData data;

  /** Готовый план прогулки с расписанием посещения мест. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Schema(description = "Готовый план прогулки")
  public static class PlanData {

    @Schema(description = "Упорядоченный список посещений в формате расписания")
    private List<PlanItem> items;

    @Schema(description = "Количество мест в плане", example = "5")
    private int totalPlaces;

    @Schema(description = "Общая продолжительность прогулки в часах", example = "5.5")
    private double totalHours;

    @Schema(
        description =
            "Предупреждения от планировщика (например, «место не вошло из-за графика работы»)")
    private List<String> warnings;

    @Schema(
        description = "Сколько перестановок порядка посещения было проверено алгоритмом",
        example = "120")
    private int evaluatedOrderings;
  }

  /** Единичный элемент расписания: место и временное окно его посещения. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Schema(description = "Одно посещение в плане")
  public static class PlanItem {

    @Schema(description = "ID места из places-service", example = "way/123456")
    private String placeId;

    @Schema(description = "Название места", example = "Третьяковская галерея")
    private String placeName;

    @Schema(description = "Время прибытия в формате HH:mm", example = "11:30")
    private String arrivalTime;

    @Schema(description = "Время ухода в формате HH:mm", example = "13:00")
    private String departureTime;

    @Schema(description = "Категория места", example = "museum")
    private String category;

    @Schema(description = "Сколько минут заняла дорога от предыдущей точки", example = "15")
    private int travelTimeMinutes;
  }
}
