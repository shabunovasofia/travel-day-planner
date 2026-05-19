package ru.kholodov.locationcontextservice.exception;

/**
 * Исключение, выбрасываемое когда геокодирование не может определить координаты
 * по заданному адресу.
 *
 * <p>Приводит к HTTP-ответу со статусом 404.
 */

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(String message) {
        super(message);
    }
}
