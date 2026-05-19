package ru.kholodov.locationcontextservice.dto;

/**
 * Географические координаты (широта и долгота).
 *
 * @param lat широта
 * @param lon долгота
 */
public record Coordinates(double lat, double lon) {
}
