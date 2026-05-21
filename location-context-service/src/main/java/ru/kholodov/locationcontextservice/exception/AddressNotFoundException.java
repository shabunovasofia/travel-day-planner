package ru.kholodov.locationcontextservice.exception;

/**
 * Исключение, выбрасываемое когда геокодирование не может определить координаты
 * по заданному адресу.
 *
 * <p>Перехватывается {@link GlobalExceptionHandler} и преобразуется в HTTP-ответ со
 * статусом 404.
 */
public class AddressNotFoundException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением для пользователя.
     *
     * @param message человекочитаемое сообщение (попадает в тело ответа в поле {@code error})
     */
    public AddressNotFoundException(String message) {
        super(message);
    }
}
