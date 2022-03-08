package de.skuld.web.api;

import javax.annotation.Generated;

@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-01-24T09:48:19.864Z[GMT]")
public class NotFoundException extends ApiException {

  private final int code;

  public NotFoundException(int code, String msg) {
    super(code, msg);
    this.code = code;
  }
}
