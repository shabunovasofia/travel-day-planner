package ru.kholodov.locationcontextservice.exception;

public class AddressNotFoundException extends RuntimeException {
  public AddressNotFoundException(String message) {
    super(message);
  }
}
