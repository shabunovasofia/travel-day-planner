package ru.kholodov.locationcontextservice.enums;

/**
 * Темп прогулки.
 *
 * <p>Используется для масштабирования скорости при расчёте изохроны:
 * <ul>
 *   <li>{@code SLOW} — медленный (~3.5 км/ч)</li>
 *   <li>{@code MEDIUM} — средний (~5.0 км/ч)</li>
 *   <li>{@code FAST} — быстрый (~6.5 км/ч)</li>
 * </ul>
 */
public enum Pace {
    SLOW,
    MEDIUM,
    FAST
}
