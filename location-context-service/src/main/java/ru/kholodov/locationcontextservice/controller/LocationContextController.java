package ru.kholodov.locationcontextservice.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kholodov.locationcontextservice.dto.LocationContextRequest;
import ru.kholodov.locationcontextservice.dto.LocationContextResponse;
import ru.kholodov.locationcontextservice.exception.AddressNotFoundException;
import ru.kholodov.locationcontextservice.services.LocationContextService;

/**
 * REST-контроллер для получения контекста прогулки по заданной локации.
 *
 * <p>Принимает запрос с адресом, временным интервалом и темпом движения, возвращает уточнённые
 * координаты, радиус поиска и доступное время.
 *
 * @author Stepan Kholodov
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/context")
public class LocationContextController {

  private static final Logger logger = LoggerFactory.getLogger(LocationContextController.class);
  private final LocationContextService locationContextService;

  public LocationContextController(LocationContextService locationContextService) {
    this.locationContextService = locationContextService;
  }

  /**
   * Обрабатывает запрос на анализ контекста локации.
   *
   * <p>Выполняет валидацию входящих данных, логирует вызов, делегирует бизнес-логику сервису и
   * возвращает результат клиенту.
   *
   * @param request объект, содержащий адрес, время начала/окончания прогулки и темп движения;
   *     обязательные поля должны быть заполнены согласно аннотациям валидации
   * @return {@link ResponseEntity} с телом {@link LocationContextResponse} и статусом 200 OK
   */
  @PostMapping("/analyze")
  public ResponseEntity<?> getLocation(@Valid @RequestBody LocationContextRequest request) {
    logger.info("Вызван метод getLocation");
    try {
      LocationContextResponse response = locationContextService.getLocation(request);
      return ResponseEntity.ok(response);
    } catch (AddressNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      logger.error("Внутренняя ошибка сервера", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Произошла ошибка при обработке запроса");
    }
  }
}
